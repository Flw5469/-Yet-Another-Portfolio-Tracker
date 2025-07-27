package com.flw5469.portfolio_tracker.price_retrival.historical_price;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;


@Repository
public class HistoricalPriceStorage {
    
    private static final Logger logger = LoggerFactory.getLogger(HistoricalPriceStorage.class);
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Store a single price entry
     */
    public void storePrice(long time, String symbol, String price) {
        try {
            long priceLong = convertPriceStringToLong(price);
            Timestamp timestamp = new Timestamp(time);
            
            logger.info("Trying to store: {} {} at {}", symbol, price, timestamp);

            String sql = "INSERT INTO crypto_prices (time, symbol, price) VALUES (?, ?, ?) ON CONFLICT (time, symbol) " + //
                            "DO UPDATE SET price = EXCLUDED.price;" ;
            jdbcTemplate.update(sql, timestamp, symbol, priceLong);
            
            logger.info("Stored: {} {} at {}", symbol, price, timestamp);
        } catch (Exception e) {
            logger.error("Failed to store price: {}", e.getMessage());
        }
    }

    public void storePriceFromKline(KlineData k){
      storePrice(k.getOpenTime(), k.getSymbol(), k.getOpenPrice());
    }
    
    public void storePriceFromKlineBulk(List<KlineData> klineList) {
        if (klineList == null || klineList.isEmpty()) {
            logger.warn("Empty or null kline list provided for bulk storage");
            return;
        }
        
        try {
            String sql = "INSERT INTO crypto_prices (time, symbol, price) VALUES (?, ?, ?) ON CONFLICT (time, symbol) " + //
                            "DO UPDATE SET price = EXCLUDED.price;";
            
            List<Object[]> batchArgs = klineList.stream()
                .map(k -> new Object[]{
                    new Timestamp(k.getOpenTime()),
                    k.getSymbol(),
                    convertPriceStringToLong(k.getOpenPrice())
                })
                .toList();
            
            logger.info("Attempting to bulk store {} price entries", klineList.size());
            
            int[] updateCounts = jdbcTemplate.batchUpdate(sql, batchArgs);
            
            int successCount = 0;
            for (int count : updateCounts) {
                if (count > 0) successCount++;
            }
            
            logger.info("Successfully stored {} out of {} price entries", successCount, klineList.size());
            
        } catch (Exception e) {
            logger.error("Failed to bulk store prices: {}", e.getMessage());
            // Fallback to individual inserts
            logger.info("Falling back to individual inserts");
            for (KlineData k : klineList) {
                storePriceFromKline(k);
            }
        }
    }

    /**
     * From Database, Retrieve price for a symbol at a specific time
     * @param time timestamp in milliseconds
     * @param symbol crypto symbol (e.g., "BTCUSDT")
     * @return price as String if found, null if not found
     */
    public Double getPrice(long time, String symbol) {
        try {
            Timestamp timestamp = new Timestamp(time);
            
            String sql = "SELECT price FROM crypto_prices WHERE time = ? AND symbol = ?";
            
            List<Long> results = jdbcTemplate.queryForList(sql, Long.class, timestamp, symbol);
            
            if (results.isEmpty()) {
                logger.debug("No price found for {} at {}", symbol, timestamp);
                return null;
            }
            
            long priceLong = results.get(0);
            Double priceDouble = convertLongToDouble(priceLong);
            
            logger.debug("Retrieved price for {}: {} at {}", symbol, priceDouble, timestamp);
            return priceDouble;
            
        } catch (Exception e) {
            logger.error("Failed to retrieve price for {} at {}: {}", symbol, new Timestamp(time), e.getMessage());
            return null;
        }
    }

public Stream<Pair<Double, Long>> getTimedPriceStream(long startingTime, long endingTime, String symbol, String interval) {
    try {
        // Build SQL with interval as literal, not parameter
        String sql = String.format(
            "SELECT time_bucket('%s', time) as bucket, symbol, first(price, time) as price " +
            "FROM crypto_prices " + 
            "WHERE time >= to_timestamp(?) AND time <= to_timestamp(?) AND symbol = '%s' " +
            "GROUP BY bucket, symbol " +
            "ORDER BY bucket",
            interval,  // Insert interval directly into SQL
            symbol
        );

        logger.info("SQL Template: {}", sql);
        logger.info("Parameters: startTime={}, endTime={}, symbol='{}'", 
                    startingTime , endingTime, symbol);

        return jdbcTemplate.queryForStream(
            sql,
            (rs, rowNum) -> Pair.of(
                rs.getDouble("price"),
                rs.getTimestamp("bucket").getTime()
            ),
            startingTime,      // Only 3 parameters now
            endingTime
        );

    } catch (Exception e) {
        logger.error("Failed to retrieve price stream for symbol {}: {}", symbol, e.getMessage());
        return Stream.empty();
    }
}


    // Remove the separate priceExists() method - it's redundant!
    private long convertPriceStringToLong(String priceStr) {
        double price = Double.parseDouble(priceStr);
        return Math.round(price * 100_000_000); // 8 decimal precision
    }

    private String convertLongToPriceString(long priceLong) {
        double price = priceLong / 100_000_000.0; // Reverse the 8 decimal precision
        return String.format("%.8f", price);
    }

    private Double convertLongToDouble(long priceLong) {
        return priceLong / 100_000_000.0; // Reverse the 8 decimal precision
    }
    private long intervalToLong(String interval) {
        return switch (interval.toLowerCase()) {
            case "1 day" -> 86400000L;
            case "7 day", "7 days" -> 604800000L;
            case "30 day", "30 days" -> 2592000000L;
            case "1 month" -> 2592000000L;
            case "1 year" -> 31536000000L;
            default -> throw new IllegalArgumentException("Unsupported interval: " + interval);
        };
    }
}