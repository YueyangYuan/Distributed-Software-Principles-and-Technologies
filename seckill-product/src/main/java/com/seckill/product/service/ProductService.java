package com.seckill.product.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.Result;
import com.seckill.common.entity.Product;
import com.seckill.common.exception.BizException;
import com.seckill.product.mapper.ProductMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PRODUCT_CACHE_KEY = "product:detail:";
    private static final String PRODUCT_NULL_KEY = "product:null:"; // 缓存穿透 - 空值缓存
    private static final long CACHE_TTL = 30; // 30分钟
    private static final long NULL_TTL = 2; // 空值缓存2分钟

    /**
     * 获取商品详情（带缓存 - 解决穿透、击穿、雪崩）
     */
    public Result<Product> getProductById(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY + id;

        // 1. 查缓存
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cached)) {
            try {
                return Result.ok(objectMapper.readValue(cached, Product.class));
            } catch (Exception e) {
                log.error("反序列化缓存失败", e);
            }
        }

        // 2. 缓存穿透检测 - 查空值标记
        String nullFlag = redisTemplate.opsForValue().get(PRODUCT_NULL_KEY + id);
        if ("1".equals(nullFlag)) {
            return Result.fail("商品不存在");
        }

        // 3. 缓存击穿防护 - 互斥锁
        String lockKey = "lock:product:" + id;
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                try {
                    // 双重检查
                    cached = redisTemplate.opsForValue().get(cacheKey);
                    if (StrUtil.isNotBlank(cached)) {
                        return Result.ok(objectMapper.readValue(cached, Product.class));
                    }

                    Product product = productMapper.selectById(id);
                    if (product == null) {
                        // 缓存穿透 - 缓存空值
                        redisTemplate.opsForValue().set(PRODUCT_NULL_KEY + id, "1", NULL_TTL, TimeUnit.MINUTES);
                        return Result.fail("商品不存在");
                    }

                    // 缓存雪崩 - 随机过期时间
                    long randomTtl = CACHE_TTL + (long) (Math.random() * 10);
                    redisTemplate.opsForValue().set(cacheKey,
                            objectMapper.writeValueAsString(product), randomTtl, TimeUnit.MINUTES);
                    return Result.ok(product);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            } else {
                // 未获取到锁，短暂休眠后重试
                Thread.sleep(50);
                return getProductById(id);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("获取商品信息失败");
        } catch (Exception e) {
            throw new BizException("获取商品信息失败: " + e.getMessage());
        }
    }

    public Result<List<Product>> listProducts(int page, int size) {
        Page<Product> pageResult = productMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreateTime));
        return Result.ok(pageResult.getRecords());
    }

    public Result<Product> createProduct(Product product) {
        product.setDeleted(0);
        productMapper.insert(product);
        return Result.ok(product);
    }

    /**
     * 清除商品缓存
     */
    public void evictCache(Long productId) {
        redisTemplate.delete(PRODUCT_CACHE_KEY + productId);
        redisTemplate.delete(PRODUCT_NULL_KEY + productId);
    }
}
