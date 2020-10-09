package cn.xyz327.generator.core.snowflake;

import cn.xyz327.generator.core.IdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.wildfly.common.Assert;

import java.util.Random;

/**
 * <a href="https://github.com/twitter-archive/snowflake/tree/snowflake-2010">雪花算法</a>实现 <br/>
 * 雪花算法生成的 id 长度为 64 位。主要分为四段，<br/>
 * 1. 第一位不使用, 固定 0,在有符号数二进制中第一位用于表示正负.1:负数,0:正数.所以固定 0 表示正数 <br/>
 * 2. 41 位长度的时间戳, 毫秒级。从起始时间点生产的 id 可以使用 69 年 <br/>
 * 3. 10 位长度的机器 id, 用于支持集群部署.最大可以支持 1023 台机器 <br/>
 * 4. 12 位长度的自增序列, 最大 4096. 最大支持 1ms 内生成 4096 个 id. 超过最大值时等待下一毫秒数 <br/>
 *
 * @author <a href="mailto:xyz327@outlook.com">xizhou</a>
 * @since 2020/9/15 7:53 下午
 */
@Slf4j
@RequiredArgsConstructor
public class SnowflakeIdGenerator implements IdGenerator {
    /**
     * 起始时间 2019-11-11 11:11:11
     */
    private final long twepoch = 1547176271000L;
    /**
     * 时间戳在整个 id 中需要左移的偏移量
     */
    private final long timestampLeftShift;
    /**
     * workId 所占的位数
     */
    private final long workerIdBits;
    /**
     * 最大的 workId : 2 的 workerIdBits 次方
     */
    private final long maxWorkerId;
    /**
     * workId 在整个 id 中的需要左移偏移量
     */
    private final long workerIdShift;
    /**
     * 自增序列的位数
     */
    private final long sequenceBits;
    /**
     * 自增序列的最大值,防止超出最大值 :2 的 sequenceBits 次方
     */
    private final long sequenceMask;
    /**
     * 自身的 workId
     */
    private long workerId;
    /**
     * 上一次的序列号
     */
    private long sequence = 0L;
    /**
     * 上次生成的时间戳
     */
    private long lastTimestamp = -1L;
    public boolean initFlag = false;
    private static final Random RANDOM = new Random();
    private int port;
    /**
     * 由于时钟回拨导致的时间延后毫秒数。最大支持延后 5 毫秒
     */
    private final int OFFSET_MAX = 5;

    private final int HUNDRED_K = 100_000;

    public SnowflakeIdGenerator(long workerId, long workerIdBits, long sequenceBits) {
        this.workerId = workerId;
        this.workerIdBits = workerIdBits;
        this.sequenceBits = sequenceBits;
        Assert.assertTrue(workerIdBits+sequenceBits==22);
        this.timestampLeftShift = this.workerIdBits+this.sequenceBits;
        this.workerIdShift = this.sequenceBits;
        // 最大的 workId 从 0 开始
        this.maxWorkerId = ~(-1L << workerIdBits);
        if(this.workerId > maxWorkerId){
            throw new IllegalArgumentException("workId 不能超过最大值:" + maxWorkerId);
        }
        // 最大的自增序列 id
        this.sequenceMask = ~(-1L << sequenceBits);
    }

    public SnowflakeIdGenerator(long workerId) {
        this(workerId, 10, 12);
    }

    @Override
    public long generate(@Nullable String key) {
        return nextId();
    }

    @Override
    public long[] batchGenerate(@Nullable String key, long batchSize) {
        return new long[0];
    }

    private synchronized long nextId(){
        long currentTime = currentTime();
        if(currentTime < lastTimestamp){
            // 当前时间小于最后生成的时间.可能是 ntp 导致机器时钟回拨导致的. 等待到最后生成时间点
            long offset = lastTimestamp - currentTime;
            if (offset <= OFFSET_MAX) {
                try {
                    wait(offset << 1);
                    currentTime = currentTime();
                    if (currentTime < lastTimestamp) {
                        return -1;
                    }
                } catch (InterruptedException e) {
                    log.error("wait interrupted");
                    return -2;
                }
            } else {
                return -3;
            }
        }
        if (lastTimestamp == currentTime) {
            sequence = (sequence + 1) & sequenceMask;
            if (sequence == 0) {
                //seq 为0的时候表示当前毫秒生成的序列已达到最大值, 是下一毫秒时间开始对seq做随机
                sequence = RANDOM.nextInt(100);
                currentTime = tilNextMillis(lastTimestamp);
            }
        } else {
            //如果是新的ms开始
            sequence = RANDOM.nextInt(100);
        }
        lastTimestamp = currentTime;
        // 时间戳左移 + 机器号左移 + 自增号
        return ((currentTime - twepoch) << timestampLeftShift) | (workerId << workerIdShift) | sequence;
    }
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = currentTime();
        while (timestamp <= lastTimestamp) {
            timestamp = currentTime();
        }
        return timestamp;
    }
    private long currentTime() {
        return System.currentTimeMillis();
    }
}
