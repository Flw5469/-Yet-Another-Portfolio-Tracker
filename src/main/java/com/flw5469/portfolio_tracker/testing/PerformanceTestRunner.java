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

@Slf4j
@Component
@ConditionalOnProperty(name = "app.test.performance.enabled", havingValue = "true")
public class PerformanceTestRunner {
    
    @Autowired(required = false)
    private SimplePerformanceTest simpleTest;
    
    @Autowired(required = false) 
    private ConcurrentPerformanceTest concurrentTest;
    
    @EventListener(ApplicationReadyEvent.class)
    public void runAllTests() {
        if (simpleTest != null) {
            log.info("Running Simple Test...");
            simpleTest.runPerformanceTest();
        }
        
        if (concurrentTest != null) {
            log.info("Running Concurrent Test...");
            concurrentTest.runPerformanceTest();
        }
    }
}