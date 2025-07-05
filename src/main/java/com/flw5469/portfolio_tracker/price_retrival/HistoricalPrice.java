package com.flw5469.portfolio_tracker.price_retrival;

import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoricalPrice {
  
  private static final Logger logger = LoggerFactory.getLogger(HistoricalPrice.class);

  // to prevent class construction
  private HistoricalPrice() {
    throw new UnsupportedOperationException("Utility class");
  }

  public static void getData(){
    String market = "ETHEUR";
    String tickInterval = "1d";
    
    String url = "https://api.binance.com/api/v3/klines?symbol=" + market + "&interval=" + tickInterval;
    logger.info("the url is: {} ", url);
    
    // Create RestTemplate (Spring's HTTP client)
    RestTemplate restTemplate = new RestTemplate();
    
    // Make the GET request and get JSON response
    JsonNode data = restTemplate.getForObject(url, JsonNode.class);
    
    // Check if data is null
    if (data == null) {
        logger.error("null data is received!\n");
        return;
    }
    
    logger.info("the data size is {}", data.size());
    
    // Convert JSON arrays to KlineData objects
    List<KlineData> klineDataList = new ArrayList<>();
    
    for (JsonNode klineNode : data) {
        try {
            KlineData klineData = new KlineData(
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
            logger.error("Error parsing kline data at index {}: {}", klineDataList.size(), e.getMessage());
        }
    }
    
    logger.info("Successfully parsed {} kline data objects", klineDataList.size());
    
    // Log first few entries to verify the data
    for (int i = 0; i < Math.min(3, klineDataList.size()); i++) {
        KlineData kline = klineDataList.get(i);
        logger.info("Kline {}: Open={}, High={}, Low={}, Close={}, Volume={}", 
                   i, kline.getOpenPrice(), kline.getHighPrice(), 
                   kline.getLowPrice(), kline.getClosePrice(), kline.getVolume());
    }
  }
}

