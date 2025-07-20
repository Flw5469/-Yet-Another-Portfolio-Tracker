package com.flw5469.portfolio_tracker.price_retrival.historical_price;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.flw5469.portfolio_tracker.critical_caluclation.CustomThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import lombok.extern.slf4j.Slf4j;
import java.util.stream.Stream;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.apache.commons.lang3.tuple.Pair;

@Slf4j
@Service
public class HistoricalPriceHandler {

  private HistoricalPriceStorage historicalPriceStorage;
  private CustomThreadPool customThreadPool;
  private HistoricalPriceGetter historicalPriceGetter;

  public HistoricalPriceHandler(HistoricalPriceStorage historicalPriceStorage, CustomThreadPool customThreadPool, HistoricalPriceGetter historicalPriceGetter){
    this.historicalPriceStorage = historicalPriceStorage;
    this.customThreadPool = customThreadPool;
    this.historicalPriceGetter = historicalPriceGetter;
  }

  /**
   * Retrieve price for a symbol at a specific time using cache, then database, then api
   * @param time timestamp in milliseconds
   * @param symbol crypto symbol (e.g., "BTCUSDT")
   * @return price as String if found, null if not found
   */
  public Double getHourlyData(String symbol, long startTime){

    Double priceInDatabase = historicalPriceStorage.getPrice(startTime, symbol); 
    if (priceInDatabase==null){
      log.info("get data of {} and {}, database cache miss!", startTime, symbol);      
      List<KlineData> historicalKlineResults = historicalPriceGetter.getData(symbol, startTime);
      historicalPriceStorage.storePriceFromKlineBulk(historicalKlineResults);
      // TODO: check if api result's time matches with request's time
      if (true) {
        return Double.parseDouble(historicalKlineResults.get(0).getOpenPrice());
      } else {
        return null;
      }

    } else {
      log.info("get data of {} and {}, database cache hit!", startTime, symbol);
      return priceInDatabase;
    }
  }

  ///
  /// 
  ///
  public ArrayList<Pair<Double, Long>> getTimedPrice(String symbol, long startTime, long endTime, String interval){

    log.info("entered getTimedPrice!");
    Stream<Pair<Double, Long>> priceInDatabase = historicalPriceStorage.getTimedPriceStream(startTime, endTime, symbol, interval);
    log.info("exited getTimedPrice");
    ArrayList<Pair<Double, Long>> priceList = new ArrayList<>();
    AtomicReference<Long> previousTime = new AtomicReference<>(null);
    long timeRange = intervalToLong(interval);
    ArrayList<Integer> indexList = new ArrayList<>();
    int fetchRange = 500; // each fetch takes 500 hourly data in binance api, so it become less when the unit is bigger (daily).
    List<CompletableFuture<Void>> futures = new ArrayList<>();
    long threshold = 5000; // in ms

    /*
    To prevent wasting the bandwidth of 500 hourly records per api fetch,
    after getting existing data from database
     1. skip records before desire time, 
     2. calculate and reserve slots for records after desired time
     3. Greedily check the latest missing within range = 500/unit (since retrieve by hourly. eg 500/1 day = 500/24 = 20) 
         records next from the current missing. 
     eg 51 _ _ 32 _ 3, range = 4, index 4 will be fetched since 1 2 3 4 will be returned
     */   
    priceInDatabase.forEach(pair->{

      //log.info("dealing with pair: ({} , {})", pair.getLeft(), pair.getRight());

      if ((previousTime.get()==null) ||  (pair.getRight()-previousTime.get()) >= (timeRange - threshold)) {

        long timeDistance;
        if (previousTime.get()==null) {
          timeDistance = 0;
        } else {
          timeDistance = pair.getRight()-previousTime.get();
        }
        
        // for each non-existing record, store the index in an array, 
        // submit a task to fetch data every 500/unit record, or launch one final in the end.
        if (timeDistance/timeRange > 1) {
          log.info("the #reserved slots is : {}", timeDistance/timeRange);
        }
        for (int i=1; i< timeDistance / timeRange ; i++){
          priceList.add(null);
          
          // the empty record is more than what one api fetch can handle -> submit the index list, add current into list.
          if (!indexList.isEmpty()) log.info("the future gap of index list and current is: {}", (priceList.size()-1) - indexList.getLast());
          if (!indexList.isEmpty() && ((priceList.size()-1) - indexList.getLast() >= fetchRange)){
            log.info("fetching {}", indexList.getLast());
            log.info("submitting: {}", indexList.toString());
            ArrayList<Integer> cloneArray = (ArrayList<Integer>)indexList.clone();
            futures.add(customThreadPool.submitTask(()->fetchAccordingToIndex(symbol, priceList, cloneArray, timeRange, startTime, threshold)));
            indexList.clear();
          }
          indexList.add(priceList.size()-1);
          log.info("adding index {}", priceList.size()-1);
        }

        previousTime.set(pair.getRight());
      }

      // deal with existing record.
      priceList.add(pair);
    });

    if (!indexList.isEmpty()) {
      log.info("fetching {}", indexList.getLast());
      log.info("submitting: {}", indexList.toString());

      ArrayList<Integer> cloneArray = (ArrayList<Integer>)indexList.clone();
      futures.add(customThreadPool.submitTask(()->fetchAccordingToIndex(symbol, priceList, cloneArray , timeRange, startTime, threshold)));
    }

    System.out.println("Futures list size: " + futures.size());
    for (int i = 0; i < futures.size(); i++) {
        CompletableFuture<?> future = futures.get(i);
        System.out.println("Future " + i + ": " + (future == null ? "NULL OBJECT" : "Valid CompletableFuture"));
    }

    if (!futures.isEmpty()) {
      CompletableFuture<Void> allTasks = CompletableFuture.allOf(
          futures.toArray(new CompletableFuture[0])
      );
      allTasks.join();  // Blocks until all tasks are done
    }

    System.out.println("All tasks completed!");

    return priceList;

    // if (priceInDatabase==null){
    //   log.info("get data of {} and {}, database cache miss!", startTime, symbol);      
    //   List<KlineData> historicalKlineResults = HistoricalPriceGetter.getData(symbol, startTime);
    //   historicalPriceStorage.storePriceFromKlineBulk(historicalKlineResults);
    //   // TODO: check if api result's time matches with request's time
    //   if (true) {
    //     return Double.parseDouble(historicalKlineResults.get(0).getOpenPrice());
    //   } else {
    //     return null;
    //   }

    // } else {
    //   log.info("get data of {} and {}, database cache hit!", startTime, symbol);
    //   return priceInDatabase;
    // }
  }

  /*
  input: indexList: a list of index that is the #element is smaller than how much 1 api call can give. 
  process: submit tasks to threadpool that calls api (api call so #threads can be larger), wait here and edit the priceList accordingly
  output: a future so caller can wait on it (in getTimedPrice caller wait on them after fetching of the existing priceList complete)
   */
  private void fetchAccordingToIndex(String symbol, ArrayList<Pair<Double, Long>> priceList, ArrayList<Integer> indexList, long timeRange,long startTime, long threshold) {
    List<KlineData> result = historicalPriceGetter.getData(symbol, startTime+indexList.getFirst()*timeRange);
    log.info("fetching data from time: {}",startTime+indexList.getFirst()*timeRange);

    int KlineIndex = 0;
    log.info("contains: {}", indexList.toString());
    for (Integer index : indexList) {
        log.info("dealing with index: {}", index);
        // sacrifice O(N) time in some cases to prevent accidentally skipping the whole array
        if (KlineIndex > result.size()) {
          KlineIndex = 0;
        }
        long timeRequired = startTime + index * timeRange;
        long timeCurrentRecord = result.get(KlineIndex).getOpenTime();
        log.info("the time distance = {}, time for record: {}, time expected: {}", timeCurrentRecord - timeRequired, timeCurrentRecord, timeRequired);
        if (Math.abs(timeCurrentRecord - timeRequired) < threshold) {
          priceList.set(index, Pair.of(Double.parseDouble(result.get(KlineIndex).getOpenPrice()), timeCurrentRecord));
          log.info("setting for index: {}, price: {} , time: {}", index, result.get(KlineIndex).getOpenPrice(), timeCurrentRecord);
        }
        KlineIndex++;
    }

  }
  
  //TODO: check for correctness
  public long intervalToLong(String interval) {
      return switch (interval.toLowerCase()) {
          case "1 hour" -> 3600000L;
          case "1 day" -> 86400000L;
          case "7 day", "7 days" -> 604800000L;
          case "30 day", "30 days" -> 2592000000L;
          case "1 month" -> 2592000000L;
          case "1 year" -> 31536000000L;
          default -> throw new IllegalArgumentException("Unsupported interval: " + interval);
      };
  }

}