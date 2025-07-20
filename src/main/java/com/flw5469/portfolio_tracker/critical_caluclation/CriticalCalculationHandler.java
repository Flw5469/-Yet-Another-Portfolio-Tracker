package com.flw5469.portfolio_tracker.critical_caluclation;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flw5469.portfolio_tracker.price_retrival.current_price.*;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.*;

import lombok.extern.slf4j.Slf4j;
//owns the threadPool, call functions to submit jobs to threadpool and allocate data for each calculations.
//contains a reference to the currentprice and  historical price service
@Slf4j
@Service
public class CriticalCalculationHandler{

  @Autowired
  private CustomThreadPool customThreadPool;
  // @Autowired
  // private RealTimePriceHandler realTimePriceHandler;
  @Autowired
  private HistoricalPriceHandler historicalPriceHandler;

  public Double getPriceGap(String symbol, long newTimestamp, long oldTimestamp ){
    CompletableFuture<Double> future = customThreadPool.submitTask(() -> {
        return historicalPriceHandler.getHourlyData(symbol, newTimestamp) - 
              historicalPriceHandler.getHourlyData(symbol, oldTimestamp);
    });

    // To get the result:
    try {
        Double priceDifference = future.get();
        return priceDifference;
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt(); // Restore interrupted status
        log.error("Thread was interrupted while waiting for price calculation", e);
        return null; // or throw a custom exception
    } catch (ExecutionException e) {
        log.error("Error occurred during price calculation", e.getCause());
        return null; // or throw a custom exception
    }
  }


  public ArrayList<Double> getVolatilty(String symbol, long newTimestamp, long oldTimestamp){
    
    return new ArrayList<Double>();
  }


}