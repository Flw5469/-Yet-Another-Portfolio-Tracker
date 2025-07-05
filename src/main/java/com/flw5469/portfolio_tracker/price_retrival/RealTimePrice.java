package com.flw5469.portfolio_tracker.price_retrival;

import org.springframework.beans.factory.annotation.Autowired;
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
public class RealTimePrice {
    
    private final WebSocketConnectionManager connectionManager;

    public RealTimePrice(PriceWebSocketHandler webSocketHandler) {

        // Binance WebSocket URL
        //"wss://stream.binance.com:9443/ws/btcusdt@ticker";
        String wsUrl = "wss://fstream.binance.com/ws/miniTicker@arr";//"wss://stream.binance.com:9443/ws/";
        
        WebSocketClient client = new StandardWebSocketClient();
        this.connectionManager = new WebSocketConnectionManager(client, webSocketHandler, wsUrl);
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
