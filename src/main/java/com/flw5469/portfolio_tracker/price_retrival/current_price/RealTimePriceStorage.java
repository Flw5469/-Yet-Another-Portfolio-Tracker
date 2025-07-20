package com.flw5469.portfolio_tracker.price_retrival.current_price;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RealTimePriceStorage {
   
    private Map<String, Double> map = new HashMap<>();
    private int findNextQuoteWithCharAt(String payload, int startIndex) {
        for (int i = startIndex; i < Math.min(startIndex + 20, payload.length()); i++) {
            if (payload.charAt(i) == '"') {
                return i;
            }
        }
        return -1;
    }


    private boolean hardcodeMatchSymbol(String payload, int startIndex){
      return (payload.charAt(startIndex) == '"' && payload.charAt(startIndex+1) == 's' && 
            payload.charAt(startIndex+2) == '"' && payload.charAt(startIndex+3) == ':' && 
            payload.charAt(startIndex+4) == '"');
    }
    
    private boolean hardcodeMatchPrice(String payload, int startIndex){
      return (payload.charAt(startIndex) == '"' && payload.charAt(startIndex+1) == 'c' && 
            payload.charAt(startIndex+2) == '"' && payload.charAt(startIndex+3) == ':' && 
            payload.charAt(startIndex+4) == '"');
    }


    private void parsePayload(String payload) {

      // log.info(payload);
        String symbolPattern = "\"s\":\"";
        String pricePattern =  "\"c\":\"";
        String symbol = "";

        for (int i = 0; i <= payload.length() - (Math.max(symbolPattern.length(), pricePattern.length())); i++) {
            if (hardcodeMatchSymbol(payload, i)) {
                // Found the pattern, now find the symbol
                int startIndex = i + symbolPattern.length();
                int endIndex = findNextQuoteWithCharAt(payload, startIndex);
                
                if (endIndex != -1) {
                    symbol = payload.substring(startIndex, endIndex);
                }
            }
            if (hardcodeMatchPrice(payload, i)) {
                // Found the pattern, now find the symbol
                int startIndex = i + symbolPattern.length();
                int endIndex = findNextQuoteWithCharAt(payload, startIndex);
                
                if (endIndex != -1) {
                    String price = payload.substring(startIndex, endIndex);
                    //log.info("symbol: {} price: {}", symbol, price);
                    if (!map.containsKey(symbol)) {
                      log.info("adding new key: {}", symbol);
                    }
                    updateMap(symbol, Double.valueOf(price));
                }
            }
        }
    }
    
    public void updateWholeMapByPayload(String payload) {
      parsePayload(payload);
      log.info("The map built contains: {}", this.map.size());
    }

    public void updateMap(String symbol, Double price){
      this.map.put(symbol, price);
    }

    public void populateMap(String[][] allPrices){
      for (String[] pair : allPrices) {
          String symbol = pair[0];
          double price = Double.parseDouble(pair[1]);
          this.map.put(symbol, price);
      }
    }
}