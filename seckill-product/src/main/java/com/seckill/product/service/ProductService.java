package com.seckill.product.service;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.common.dto.Result;
import com.seckill.common.entity.Product;
import com.seckill.common.exception.BizException;
import com.seckill.product.datasource.DataSourceContextHolder;
import com.seckill.product.datasource.ReadOnlyRoute;
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

    private static final String PRODUCT_CACHE_KEY = "product:detail:";
    private static final String PRODUCT_NULL_KEY = "product:null:";
    private static final long CACHE_TTL = 30;
    private static final long NULL_TTL = 2;

    private final ProductMapper productMapper;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @ReadOnlyRoute
    public Result<Product> getProductById(Long id) {
        String cacheKey = PRODUCT_CACHE_KEY + id;

        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cached)) {
            try {
                return Result.ok(objectMapper.readValue(cached, Product.class));
            } catch (Exception e) {
                log.error("Failed to deserialize product cache", e);
            }
        }

        String nullFlag = redisTemplate.opsForValue().get(PRODUCT_NULL_KEY + id);
        if ("1".equals(nullFlag)) {
            return Result.fail("Product not found");
        }

        String lockKey = "lock:product:" + id;
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
            if (Boolean.TRUE.equals(locked)) {
                try {
                    cached = redisTemplate.opsForValue().get(cacheKey);
                    if (StrUtil.isNotBlank(cached)) {
                        return Result.ok(objectMapper.readValue(cached, Product.class));
                    }

                    Product product = productMapper.selectById(id);
                    if (product == null) {
                        redisTemplate.opsForValue().set(PRODUCT_NULL_KEY + id, "1", NULL_TTL, TimeUnit.MINUTES);
                        return Result.fail("Product not found");
                    }

                    long randomTtl = CACHE_TTL + (long) (Math.random() * 10);
                    redisTemplate.opsForValue().set(
                            cacheKey,
                            objectMapper.writeValueAsString(product),
                            randomTtl,
                            TimeUnit.MINUTES
                    );
                    return Result.ok(product);
                } finally {
                    redisTemplate.delete(lockKey);
                }
            }

            Thread.sleep(50);
            return getProductById(id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BizException("Failed to query product");
        } catch (Exception e) {
            throw new BizException("Failed to query product: " + e.getMessage());
        }
    }

    @ReadOnlyRoute
    public Result<List<Product>> listProducts(int page, int size) {
        Page<Product> pageResult = productMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<Product>().orderByDesc(Product::getCreateTime)
        );
        return Result.ok(pageResult.getRecords());
    }

    public Result<Product> createProduct(Product product) {
        product.setDeleted(0);
        productMapper.insert(product);
        return Result.ok(product);
    }

    @ReadOnlyRoute
    public Result<String> inspectReadRoute() {
        return Result.ok(DataSourceContextHolder.get().name());
    }

    public Result<String> inspectWriteRoute() {
        return Result.ok(DataSourceContextHolder.get().name());
    }

    public void evictCache(Long productId) {
        redisTemplate.delete(PRODUCT_CACHE_KEY + productId);
        redisTemplate.delete(PRODUCT_NULL_KEY + productId);
    }
}
