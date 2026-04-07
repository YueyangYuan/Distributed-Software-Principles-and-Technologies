package com.seckill.common.util;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisDistributedLock {
    private final StringRedisTemplate redisTemplate;
    private final String lockKey;
    private final String lockValue;
    private static final long DEFAULT_EXPIRE = 30;

    public RedisDistributedLock(StringRedisTemplate redisTemplate, String lockKey) {
        this.redisTemplate = redisTemplate;
        this.lockKey = "lock:" + lockKey;
        this.lockValue = UUID.randomUUID().toString();
    }

    public boolean tryLock(long timeout, TimeUnit unit) {
        Boolean result = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, timeout, unit);
        return Boolean.TRUE.equals(result);
    }

    public boolean tryLock() {
        return tryLock(DEFAULT_EXPIRE, TimeUnit.SECONDS);
    }

    public void unlock() {
        String script = "if redis.call('get',KEYS[1]) == ARGV[1] then return redis.call('del',KEYS[1]) else return 0 end";
        redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Collections.singletonList(lockKey), lockValue);
    }
}
