package com.flw5469.portfolio_tracker.price_retrival.historical_price;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;


@Service
@Slf4j
public class HistoricalPriceGetter {
  
  // // to prevent class construction
  // private HistoricalPriceGetter() {
  //   throw new UnsupportedOperationException("Utility class");
  // }

  public List<KlineData> getData(String symbol, long startTime){
    String tickInterval = "1h";
    
    String url = "https://api.binance.com/api/v3/klines?symbol=" + symbol + "&interval=" + tickInterval +"&startTime=" + startTime;
    log.info("the url is: {} ", url);
    
    // Create RestTemplate (Spring's HTTP client)
    RestTemplate restTemplate = new RestTemplate();
    
    // Make the GET request and get JSON response
    JsonNode data = restTemplate.getForObject(url, JsonNode.class);
    
    // Check if data is null
    if (data == null) {
        log.error("null data is received!\n");
        return null;
    }
    
    log.info("the data size is {}", data.size());
    
    // Convert JSON arrays to KlineData objects
    List<KlineData> klineDataList = new ArrayList<>();
    
    for (JsonNode klineNode : data) {
        try {
            KlineData klineData = new KlineData(
                symbol,
                klineNode.get(0).asLong(),      // Open time
                klineNode.get(1).asText(),      // Open price
                klineNode.get(2).asText(),      // High price
                klineNode.get(3).asText(),      // Low price
                klineNode.get(4).asText(),      // Close price
                klineNode.get(5).asText(),      // Volume
                klineNode.get(6).asLong(),      // Close time
                klineNode.get(7).asText(),      // Quote asset volume
                klineNode.get(8).asInt(),       // Number of trades
                klineNode.get(9).asText(),      // Taker buy base asset volume
                klineNode.get(10).asText()      // Taker buy quote asset volume
                // klineNode.get(11) is unused field, so we skip it
            );
            
            klineDataList.add(klineData);
            
        } catch (Exception e) {
            log.error("Error parsing kline data at index {}: {}", klineDataList.size(), e.getMessage());
        }
    }
    
    log.info("Successfully parsed {} kline data objects", klineDataList.size());
    
    // Log first few entries to verify the data
    for (int i = 0; i < Math.min(10, klineDataList.size()); i++) {
        KlineData kline = klineDataList.get(i);
        // log.info("Kline {}: Open={}, High={}, Low={}, Close={}, Volume={}, OpenTime={}, CloseTime={}", 
        //            i, kline.getOpenPrice(), kline.getHighPrice(), 
        //            kline.getLowPrice(), kline.getClosePrice(), kline.getVolume(), kline.getOpenDateTime(), kline.getCloseDateTime());
        log.info("Kline {}: Open={}, OpenTime={}, CloseTime={},  Close={}", 
                   i, kline.getOpenPrice(),  kline.getOpenDateTime(), 
                   kline.getClosePrice(), kline.getCloseDateTime());
    }

    return klineDataList;
  }
}

