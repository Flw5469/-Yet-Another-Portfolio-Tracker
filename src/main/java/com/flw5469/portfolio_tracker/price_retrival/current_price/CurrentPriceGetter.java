package com.flw5469.portfolio_tracker.price_retrival.current_price;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.KlineData;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CurrentPriceGetter {
  
  private static final Logger logger = LoggerFactory.getLogger(CurrentPriceGetter.class);

  // // to prevent class construction
  // private CurrentPriceGetter() {
  //   throw new UnsupportedOperationException("Utility class");
  // }

  public String[][] getAllPricesAsArray() {
      String url = "https://api.binance.com/api/v3/ticker/price";
      RestTemplate restTemplate = new RestTemplate();
      JsonNode data = restTemplate.getForObject(url, JsonNode.class);
      
      if (data == null) {
          logger.error("null data is received!");
          return new String[0][0];
      }
      
      List<String[]> pricesList = new ArrayList<>();
      
      if (data.isArray()) {
          for (JsonNode item : data) {
              String symbol = item.get("symbol").asText();
              String price = item.get("price").asText();
              pricesList.add(new String[]{symbol, price});
          }
      }
      
      return pricesList.toArray(new String[0][]);
  }
}

