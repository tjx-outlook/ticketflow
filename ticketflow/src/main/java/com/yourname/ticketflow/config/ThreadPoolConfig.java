package com.yourname.ticketflow.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * 异步线程池配置 - 用于订单异步落库
 */
@Configuration
public class ThreadPoolConfig {

    @Value("${async.executor.core-pool-size:10}")
    private int corePoolSize;

    @Value("${async.executor.max-pool-size:50}")
    private int maxPoolSize;

    @Value("${async.executor.queue-capacity:1000}")
    private int queueCapacity;

    @Value("${async.executor.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Value("${async.executor.thread-name-prefix:async-order-}")
    private String threadNamePrefix;

    @Bean("orderAsyncExecutor")
    public ExecutorService orderAsyncExecutor() {
        return new ThreadPoolExecutor(
                corePoolSize,
                maxPoolSize,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(queueCapacity),
                new ThreadFactory() {
                    private int count = 0;
                    @Override
                    public Thread newThread(Runnable r) {
                        Thread t = new Thread(r, threadNamePrefix + (++count));
                        t.setDaemon(true);
                        return t;
                    }
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }
}
