package com.flw5469.portfolio_tracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import com.flw5469.portfolio_tracker.critical_caluclation.CustomThreadPool;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.HistoricalPriceGetter;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.HistoricalPriceHandler;
import com.flw5469.portfolio_tracker.price_retrival.historical_price.HistoricalPriceStorage;
import com.flw5469.portfolio_tracker.utils.dateUtils;

@SpringBootTest
class HistoricalPriceHandlerTest {

    @MockBean
    private HistoricalPriceStorage historicalPriceStorage;

    @Autowired
    private CustomThreadPool threadPool; // Spring manages lifecycle (@PostConstruct will be called)

    @Autowired
    private HistoricalPriceHandler historicalPriceHandler;

    @Autowired
    private HistoricalPriceGetter historicalPriceGetter;

    @Test
    void testHandleHistoricalPrice() {
        // Arrange: Set up the mock behavior
        long interval_long = historicalPriceHandler.intervalToLong("1 hour");
        long start_time = dateUtils.dateTimeToTimestamp(2020, 12, 14, 0, 0);
        List<Integer> excludeList = Arrays.asList(10, 20, 30, 400, 1000, 1600);
        ArrayList<Pair<Double, Long>> mockResult = new ArrayList<>();
        for (int i = 0; i < 2000; i++) {
            if (!excludeList.contains(i)) {
                mockResult.add(Pair.of(1000f + 0.1 * i, start_time + interval_long * i));
            }
        }

        // Mock the storage method
        when(historicalPriceStorage.getTimedPriceStream(anyLong(), anyLong(), anyString(), anyString()))
            .thenAnswer(invocation -> {
                System.out.println("Mock called with: " + Arrays.toString(invocation.getArguments()));
                return mockResult.stream();
            });

        // Act: Call the method under test
        ArrayList<Pair<Double, Long>> result = historicalPriceHandler.getTimedPrice("BTCUSDT", start_time, start_time+interval_long*2000, "1 day");

        // Verify that the mock method was called with the correct parameters
        ArgumentCaptor<Long> startTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<Long> endTimeCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> symbolCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> intervalCaptor = ArgumentCaptor.forClass(String.class);

        // not recommended to check internal implementation
        // verify(historicalPriceGetter, times(3)).getData(anyString(), anyLong());// Assert no nulls in the list itself

        assertNotNull(result);
        for (int i=0;i<result.size();i++){
          if (result.get(i)==null) System.out.print("null at "+i+"\n");
        }


        assertFalse(result.contains(null), "Result list should not contain null elements");
        
        // Assert no null values within the Pairs
        result.forEach(pair -> {
            assertNotNull(pair, "Pair should not be null");
            assertNotNull(pair.getLeft(), "Price should not be null");
            assertNotNull(pair.getRight(), "Timestamp should not be null");
        });
        }
}