package com.flw5469.portfolio_tracker.price_retrival;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RealTimePriceArray {
    // Fixed: Use List<Object> or Object[] instead of Array<Object>
    private java.util.List<Object> realTimePriceArray;
    private int mapping;

    public RealTimePriceArray() {
        // Fixed: Initialize the list in constructor
        this.realTimePriceArray = new java.util.ArrayList<>();
        this.mapping = 0; 
    }

    private java.util.List<Object> ProcessRawStream(java.util.List<Object> input){
      return input;
    }

    private int CalculateMap(){
      return 0;
    }

    public void UpdateArray(java.util.List<Object> input){
        this.realTimePriceArray = ProcessRawStream(input);
        this.mapping = CalculateMap();
    }
}