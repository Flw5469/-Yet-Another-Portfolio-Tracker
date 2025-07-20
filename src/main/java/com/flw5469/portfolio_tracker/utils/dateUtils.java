package com.flw5469.portfolio_tracker.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class dateUtils {
  
    public static final long ONE_SECOND_MS = 1000L;
    public static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
    public static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
    public static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
    // Default formatter for readable timestamps
    private static final DateTimeFormatter DEFAULT_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    // ISO format for APIs
    private static final DateTimeFormatter ISO_FORMATTER = 
        DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  private dateUtils(){

  }
    /**
     * Convert timestamp to string with custom formatter and timezone
     * @param timestamp Unix timestamp in milliseconds
     * @param formatter DateTimeFormatter to use
     * @param zoneOffset Timezone offset
     * @return Formatted date string
     */
    public static String timestampToString(long timestamp, DateTimeFormatter formatter, ZoneOffset zoneOffset) {
        Instant instant = Instant.ofEpochMilli(timestamp);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, zoneOffset);
        return dateTime.format(formatter);
    }
    public static String timestampToIsoString(long timestamp) {
        return timestampToString(timestamp, ISO_FORMATTER, ZoneOffset.UTC);
    }
    /**
     * Convert year, month, day, hour, and minute to timestamp
     * @param year Year (e.g., 2025)
     * @param month Month (1-12)
     * @param day Day of month (1-31)
     * @param hour Hour (0-23)
     * @param minute Minute (0-59)
     * @return Unix timestamp in milliseconds
     */
    public static long dateTimeToTimestamp(int year, int month, int day, int hour, int minute) {
        LocalDateTime dateTime = LocalDateTime.of(year, month, day, hour, minute, 0);
        return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
    }

}
