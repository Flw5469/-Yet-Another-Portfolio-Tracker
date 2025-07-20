package com.flw5469.portfolio_tracker.price_retrival.current_price;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Slf4j
@ConditionalOnProperty(
    name = "crypto.price-service.enabled", 
    havingValue = "true", 
    matchIfMissing = false
)
public class RealTimePriceHandler {

    private final CurrentPriceGetter currentPriceGetter;
    
    private final WebSocketConnectionManager connectionManager;

    public RealTimePriceHandler(PriceWebSocketHandler webSocketHandler, RealTimePriceStorage realTimePriceStorage,
     CurrentPriceGetter currentPriceGetter) {
     
        String[][] allPrices = currentPriceGetter.getAllPricesAsArray();
        realTimePriceStorage.populateMap(allPrices);

        // Binance WebSocket URL
        //"wss://stream.binance.com:9443/ws/btcusdt@ticker";
        //"wss://stream.binance.com:9443/ws/";
        String wsUrl = "wss://stream.binance.com:9443/ws/!miniTicker@arr"; // Spot WebSocket
        WebSocketClient client = new StandardWebSocketClient();
        this.connectionManager = new WebSocketConnectionManager(client, webSocketHandler, wsUrl);
        this.currentPriceGetter = currentPriceGetter;
    }
    
    @PostConstruct
    public void connect() {
        connectionManager.setAutoStartup(true);
        connectionManager.start();
        log.info("WebSocket client started");
    }
    
    @PreDestroy
    public void disconnect() {
        if (connectionManager.isRunning()) {
            connectionManager.stop();
            log.info("WebSocket client stopped");
        }
    }
    
    public boolean isConnected() {
        return connectionManager.isRunning();
    }

}
