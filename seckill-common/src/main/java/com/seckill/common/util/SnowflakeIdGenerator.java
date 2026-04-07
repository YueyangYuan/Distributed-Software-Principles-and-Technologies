package com.seckill.common.util;

/**
 * 雪花算法ID生成器
 * 结构: 1bit符号位 + 41bit时间戳 + 10bit机器ID + 12bit序列号
 */
public class SnowflakeIdGenerator {
    private final long epoch = 1704067200000L; // 2024-01-01
    private final long workerIdBits = 5L;
    private final long datacenterIdBits = 5L;
    private final long sequenceBits = 12L;
    private final long maxWorkerId = ~(-1L << workerIdBits);
    private final long maxDatacenterId = ~(-1L << datacenterIdBits);
    private final long workerIdShift = sequenceBits;
    private final long datacenterIdShift = sequenceBits + workerIdBits;
    private final long timestampShift = sequenceBits + workerIdBits + datacenterIdBits;
    private final long sequenceMask = ~(-1L << sequenceBits);

    private final long workerId;
    private final long datacenterId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public SnowflakeIdGenerator(long workerId, long datacenterId) {
        if (workerId > maxWorkerId || workerId < 0) throw new IllegalArgumentException("Worker ID out of range");
        if (datacenterId > maxDatacenterId || datacenterId < 0) throw new IllegalArgumentException("Datacenter ID out of range");
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) throw new RuntimeException("Clock moved backwards");
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                while (timestamp <= lastTimestamp) timestamp = System.currentTimeMillis();
            }
        } else {
            sequence = 0L;
        }
        lastTimestamp = timestamp;
        return ((timestamp - epoch) << timestampShift) | (datacenterId << datacenterIdShift) | (workerId << workerIdShift) | sequence;
    }

    /**
     * 基因法：将userId的低位嵌入orderId，便于按userId分库查询
     */
    public long nextIdWithGene(long userId) {
        long id = nextId();
        long gene = userId & 0xF; // 取userId低4位
        return (id & ~0xFL) | gene; // 替换orderId低4位
    }
}
