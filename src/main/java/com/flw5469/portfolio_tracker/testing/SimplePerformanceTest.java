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

import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
@ConditionalOnProperty(
    name = "app.test.SimplePerformanceTest.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class SimplePerformanceTest extends PerformanceTest{
  @Override  
  protected void performanceTestContent() {
    long timeWanted = dateUtils.dateTimeToTimestamp(2023, 12, 14, 0, 0);
    long timeWantedOld = dateUtils.dateTimeToTimestamp(2023, 11, 14, 0, 0);
    for (int i = 0; i < 1000; i++) {
        Timer.Sample operationSample = Timer.start(meterRegistry);
        try {
            Double result = criticalCalculationHandler.getPriceGap("ETHEUR", timeWanted, timeWantedOld);
            successCounter.increment();
            operationSample.stop(Timer.builder("performance.test.operation").register(meterRegistry));
            if ((i%100)==0) log.info("Test {}: the calculation is: {}", 
                i + 1, 
                result);
        } catch (Exception e) {
            errorCounter.increment();
        }
    }
  }
}