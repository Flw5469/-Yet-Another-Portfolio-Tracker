package com.flw5469.portfolio_tracker.testing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import com.flw5469.portfolio_tracker.critical_caluclation.CriticalCalculationHandler;
import com.flw5469.portfolio_tracker.utils.dateUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@EnableScheduling
@ConditionalOnProperty(
    name = "app.test.ConcurrentPerformanceTest.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class ConcurrentPerformanceTest extends PerformanceTest{
  @Override  
  protected void performanceTestContent() {

        log.info("Concurrent Performance Test");

        long timeWanted = dateUtils.dateTimeToTimestamp(2023, 12, 14, 0, 0);
        long timeWantedOld = dateUtils.dateTimeToTimestamp(2023, 11, 14, 0, 0);

        // Create thread pool
        ExecutorService executor = Executors.newFixedThreadPool(10);
        Timer.Sample batchSample = Timer.start(meterRegistry);

        try {
        List<CompletableFuture<Void>> futures = IntStream.range(0, 10)
            .mapToObj(threadId -> 
                CompletableFuture.runAsync(() -> {
                    IntStream.range(0, 100)
                        .forEach(i -> executeTestIteration(threadId, i, timeWanted, timeWantedOld));
                    
                    log.info("Thread {} completed", threadId + 1);
                }, executor))
            .collect(Collectors.toList());

        // Wait for all threads to complete
        futures.forEach(CompletableFuture::join);
        
    } finally {
        // Stop batch timing
        batchSample.stop(batchTimer);
        executor.shutdown();
    }
}

private void executeTestIteration(int threadId, int iteration, long timeWanted, long timeWantedOld) {
    try {
        // Start operation timing
        Timer.Sample operationSample = Timer.start(meterRegistry);
        
        Double result = criticalCalculationHandler.getPriceGap("ETHEUR", timeWanted, timeWantedOld);
        
        // Stop operation timing
        operationSample.stop(operationTimer);
        
        successCounter.increment();
        
        if ((iteration % 20) == 0) {
            log.info("Thread {}, Test {}: result={}", threadId + 1, iteration + 1, result);
        }
    } catch (Exception e) {
        errorCounter.increment();
        log.error("Thread {}, Test {} failed: {}", threadId + 1, iteration + 1, e.getMessage());
    }
}

}