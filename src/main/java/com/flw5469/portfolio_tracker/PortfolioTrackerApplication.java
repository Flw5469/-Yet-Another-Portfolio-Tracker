package com.flw5469.portfolio_tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;

import com.flw5469.portfolio_tracker.price_retrival.current_price.RealTimePriceHandler;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.HistoricalPriceGetter;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.HistoricalPriceHandler;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.HistoricalPriceStorage;
import com.flw5469.portfolio_tracker.utils.dateUtils;
import com.flw5469.portfolio_tracker.critical_caluclation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = "com.flw5469.portfolio_tracker")
public class PortfolioTrackerApplication implements ApplicationRunner {

    @Autowired
    private HistoricalPriceHandler historicalPriceHandler;
    @Autowired
    private CriticalCalculationHandler criticalCalculationHandler;

    public static void main(String[] args) {
        SpringApplication.run(PortfolioTrackerApplication.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
      long timeWanted = dateUtils.dateTimeToTimestamp(2024, 12, 14, 0, 0);
      long timeWantedOld = dateUtils.dateTimeToTimestamp(2024, 11, 14, 0, 0);
      // log.info("the calculation is: {}", criticalCalculationHandler.getPriceGap("ETHEUR", timeWanted, timeWantedOld));
      // log.info("The Timestamp's string is: {}",dateUtils.timestampToIsoString(timeWanted));
      // log.info("The retrieved result: {}", historicalPriceHandler.getHourlyData("ETHEUR",timeWanted));
      System.out.println(criticalCalculationHandler.getVolatilty("ETHEUR", timeWanted, timeWantedOld, "1 hour"));

    }
}
