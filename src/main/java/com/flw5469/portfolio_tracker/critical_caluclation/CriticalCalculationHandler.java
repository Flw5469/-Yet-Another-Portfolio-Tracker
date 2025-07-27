package com.flw5469.portfolio_tracker.critical_caluclation;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.flw5469.portfolio_tracker.price_retrival.current_price.*;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.*;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.commons.math3.stat.correlation.Covariance;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.commons.lang3.tuple.Pair;

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


  public Double getVolatilty(String symbol, long newTimestamp, long oldTimestamp, String interval){
    ArrayList<Pair<Double, Long>> prices = historicalPriceHandler.getTimedPrice(symbol, oldTimestamp, newTimestamp, interval);
    if (prices.size()<2) {
      throw new IllegalArgumentException("Parameter lenth less than 2, cannot get volatility");
    }
    for (int i=0;i<prices.size();i++) {
      Pair<Double,Long> ele = prices.get(i);
      if (ele==null) {
        throw new NullPointerException("price pair contains null" + i);
      }
      if (ele.getLeft() == null) {
          throw new NullPointerException("price pair left value is null" + i);
      }

      if (ele.getRight() == null) {
          throw new NullPointerException("price pair right value is null" + i);
      }
    }
    // Calculate returns
    DescriptiveStatistics returns = new DescriptiveStatistics();
    for (int i = 1; i < prices.size(); i++) {
        double dailyReturn = Math.log(prices.get(i).getLeft() / prices.get(i - 1).getLeft());
        returns.addValue(dailyReturn);
    }
    
    // Get standard deviation and annualize
    double dailyVolatility = returns.getStandardDeviation();
    double annualizedVolatility = dailyVolatility * Math.sqrt(365);
    
    return annualizedVolatility * 100; // Return as percentage
  }


}