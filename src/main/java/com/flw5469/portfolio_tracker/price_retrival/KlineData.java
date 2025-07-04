package com.flw5469.portfolio_tracker.price_retrival;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KlineData {
    private long openTime;
    private String openPrice;
    private String highPrice;
    private String lowPrice;
    private String closePrice;
    private String volume;
    private long closeTime;
    private String quoteAssetVolume;
    private int numberOfTrades;
    private String takerBuyBaseAssetVolume;
    private String takerBuyQuoteAssetVolume;
    
    // Convenience methods for working with crypto data
    public LocalDateTime getOpenDateTime() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(openTime), ZoneId.systemDefault());
    }
    
    public BigDecimal getOpenPriceDecimal() {
        return new BigDecimal(openPrice);
    }
    
    public BigDecimal getClosePriceDecimal() {
        return new BigDecimal(closePrice);
    }
    
    public BigDecimal getHighPriceDecimal() {
        return new BigDecimal(highPrice);
    }
    
    public BigDecimal getLowPriceDecimal() {
        return new BigDecimal(lowPrice);
    }
}