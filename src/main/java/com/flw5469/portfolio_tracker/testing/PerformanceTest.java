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
import lombok.extern.slf4j.Slf4j;

import com.flw5469.portfolio_tracker.critical_caluclation.CriticalCalculationHandler;
import com.flw5469.portfolio_tracker.utils.dateUtils;

import java.util.concurrent.TimeUnit;


public abstract class PerformanceTest {
    
    private final String metricPrefix = this.getClass().getSimpleName().toLowerCase();
    protected Counter successCounter;
    protected Counter errorCounter;
    protected Timer operationTimer;
    protected Timer batchTimer;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected CriticalCalculationHandler criticalCalculationHandler;
    
    @Autowired
    protected MeterRegistry meterRegistry;
    
    protected void performanceTestContent(){
    }

    public void runPerformanceTest() {        
        log.info("Starting performance test batch...");
        
        Timer.Sample batchSample = Timer.start(meterRegistry);
        
        // Fix: Add proper dots and use consistent naming
        successCounter = Counter.builder(metricPrefix + ".success")
            .description("Successful operations")
            .register(meterRegistry);
        errorCounter = Counter.builder(metricPrefix + ".errors")
            .description("Failed operations")
            .register(meterRegistry);
        operationTimer = Timer.builder(metricPrefix + ".operation")
            .description("Individual operation timing")
            .register(meterRegistry);
        // Fix: Use metricPrefix for batch timer too
        batchTimer = Timer.builder(metricPrefix + ".batch")
            .description("Complete batch timing")
            .register(meterRegistry);


        performanceTestContent();
        batchSample.stop(batchTimer);
        
        // Extract metrics and log them simply
        long successCount = (long) successCounter.count();
        long errorCount = (long) errorCounter.count();
        double totalTimeMs = batchTimer.totalTime(TimeUnit.MILLISECONDS);
        double avgTimeMs = operationTimer.mean(TimeUnit.MILLISECONDS);
        double maxTimeMs = operationTimer.max(TimeUnit.MILLISECONDS);
        double operationsPerSecond = successCount / batchTimer.totalTime(TimeUnit.SECONDS);
        
        log.info("=== PERFORMANCE SUMMARY ===");
        log.info("Total operations: {}", successCount + errorCount);
        log.info("Successful: {} | Failed: {}", successCount, errorCount);
        log.info("Total time: {}ms", String.format("%.2f", totalTimeMs));
        log.info("Average time per operation: {}ms", String.format("%.2f", avgTimeMs));
        log.info("Max time per operation: {}ms", String.format("%.2f", maxTimeMs));
        log.info("Operations per second: {}", String.format("%.2f", operationsPerSecond));
        log.info("==========================");
        
        // Bonus: The metrics are also available via /actuator/metrics endpoints!
        log.info("Metrics also available at: /actuator/metrics/{}.batch", metricPrefix);
    }
}