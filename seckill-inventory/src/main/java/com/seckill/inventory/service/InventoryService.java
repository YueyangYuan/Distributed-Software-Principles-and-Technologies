package com.seckill.inventory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.dto.Result;
import com.seckill.common.entity.Inventory;
import com.seckill.common.exception.BizException;
import com.seckill.inventory.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String SECKILL_USER_KEY = "seckill:user:";

    /**
     * 预热库存到Redis
     */
    public void preloadStock(Long productId) {
        Inventory inventory = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getProductId, productId));
        if (inventory != null) {
            redisTemplate.opsForValue().set(STOCK_KEY + productId,
                    String.valueOf(inventory.getAvailableStock()));
            log.info("库存预热完成: productId={}, stock={}", productId, inventory.getAvailableStock());
        }
    }

    /**
     * Redis预扣减库存（Lua脚本保证原子性）+ 幂等性检查
     * 返回: >0 扣减成功(剩余库存), -1 库存不足, -2 重复购买
     */
    public long preDeductStock(Long productId, Long userId) {
        // Lua脚本: 幂等检查 + 原子扣减
        String luaScript = """
            -- 幂等性检查：同一用户同一商品只能秒杀一次
            local userKey = KEYS[2]
            if redis.call('sismember', userKey, ARGV[2]) == 1 then
                return -2
            end
            -- 库存扣减
            local stock = tonumber(redis.call('get', KEYS[1]))
            if stock == nil or stock <= 0 then
                return -1
            end
            redis.call('decr', KEYS[1])
            redis.call('sadd', userKey, ARGV[2])
            return stock - 1
            """;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>(luaScript, Long.class);
        Long result = redisTemplate.execute(script,
                java.util.List.of(STOCK_KEY + productId, SECKILL_USER_KEY + productId),
                String.valueOf(productId), String.valueOf(userId));
        return result != null ? result : -1;
    }

    /**
     * 数据库扣减库存（乐观锁）
     */
    @Transactional
    public boolean deductStockInDB(Long productId, int count) {
        int rows = inventoryMapper.deductStock(productId, count);
        return rows > 0;
    }

    /**
     * 确认扣减（支付成功后）
     */
    @Transactional
    public boolean confirmDeduct(Long productId, int count) {
        return inventoryMapper.confirmDeduct(productId, count) > 0;
    }

    /**
     * 回滚库存（订单取消/超时）
     */
    @Transactional
    public boolean rollbackStock(Long productId, int count) {
        int rows = inventoryMapper.rollbackStock(productId, count);
        if (rows > 0) {
            // 回滚Redis库存
            redisTemplate.opsForValue().increment(STOCK_KEY + productId);
        }
        return rows > 0;
    }

    public Result<Inventory> getStock(Long productId) {
        Inventory inv = inventoryMapper.selectOne(
                new LambdaQueryWrapper<Inventory>().eq(Inventory::getProductId, productId));
        return inv != null ? Result.ok(inv) : Result.fail("库存记录不存在");
    }

    public Result<Inventory> initStock(Inventory inventory) {
        inventoryMapper.insert(inventory);
        preloadStock(inventory.getProductId());
        return Result.ok(inventory);
    }
}
