package cn.xyz327;

import cn.xyz327.generator.core.snowflake.SnowflakeIdGenerator;
import com.google.common.base.Stopwatch;
import io.smallrye.common.constraint.Assert;
import io.smallrye.mutiny.unchecked.Unchecked;
import io.vertx.core.impl.ConcurrentHashSet;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * @author <a href="mailto:xyz327@outlook.com">xizhou</a>
 * @since 2020/9/15 8:16 下午
 */
@Slf4j
public class SnowflakeIdGeneratorTest {
    @Test
    void name() {
        long l = -1L ^ (-1L << 14);
        System.out.println(l);
        System.out.println(-1L << 12);
        System.out.println(~1L);
    }

    @Test
    @SneakyThrows
    void snowflake() {
        SnowflakeIdGenerator snowflakeIdGenerator = new SnowflakeIdGenerator(1);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        int singleMaxRequest = 10000;
        int totalRound = 10;
        int totalSize = singleMaxRequest * totalRound;
        int threadPoolSize = 1024;
        CountDownLatch downLatch = new CountDownLatch(singleMaxRequest * totalRound);
        CountDownLatch start = new CountDownLatch(1);
        Set<Long> ids = new ConcurrentHashSet<>();
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);
        for (int i = 0; i < totalRound; i++) {
            for (int i1 = 0; i1 < singleMaxRequest; i1++) {
                executorService.submit(() -> {
                    Unchecked.supplier(() -> {
                        start.await();
                        ids.add(snowflakeIdGenerator.generate(null));
                        downLatch.countDown();
                        return null;
                    }).get();
                });
            }
            start.countDown();
        }
        downLatch.await();
        stopWatch.stop();
        System.out.println(stopWatch.toString());
        System.out.printf("gen id size:%d, totalSize:%d", ids.size(), totalSize);
        Assert.assertTrue(ids.size() == totalSize);

    }
}
