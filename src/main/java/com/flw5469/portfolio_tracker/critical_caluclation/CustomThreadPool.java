package com.flw5469.portfolio_tracker.critical_caluclation;

import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Callable;

@Slf4j
@Service
public class CustomThreadPool {
    
    private ThreadPoolExecutor computationPool;
    
    public CustomThreadPool() {
        // Empty constructor
    }
    
    @PostConstruct
    public void initializeThreadPool() {
        int coreCount = Runtime.getRuntime().availableProcessors();
        coreCount = 1;
        log.info("available cores: {}", coreCount);
        // For CPU-intensive tasks: cores or cores + 1
        int poolSize = coreCount;
        
        this.computationPool = new ThreadPoolExecutor(
            poolSize,                                    // corePoolSize
            poolSize,                                    // maximumPoolSize (same as core for CPU tasks)
            60L,                                         // keepAliveTime (changed from 0L to 60L)
            TimeUnit.SECONDS,                            // changed from MILLISECONDS to SECONDS
            new LinkedBlockingQueue<>(1000),             // bounded queue
            new ThreadFactory() {                        // custom thread factory
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "computation-thread-" + threadNumber.getAndIncrement());
                    t.setDaemon(false);
                    t.setPriority(Thread.NORM_PRIORITY);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()    // backpressure policy
        );
        
        // Now this is valid because keepAliveTime > 0
        computationPool.allowCoreThreadTimeOut(true);
    }
    
    public CompletableFuture<Void> submitTask(Runnable task) {
        if (computationPool == null) {
            throw new IllegalStateException("ThreadPool not initialized yet");
        }
        return CompletableFuture.runAsync(task, computationPool);
    }
    
    public <T> CompletableFuture<T> submitTask(Callable<T> task) {
        if (computationPool == null) {
            throw new IllegalStateException("ThreadPool not initialized yet");
        }
        return CompletableFuture.supplyAsync(() -> {
            try {
                return task.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, computationPool);
    }
    
    @PreDestroy
    public void shutdown() {
        if (computationPool != null) {
            computationPool.shutdown();
            try {
                if (!computationPool.awaitTermination(60, TimeUnit.SECONDS)) {
                    computationPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                computationPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }
}