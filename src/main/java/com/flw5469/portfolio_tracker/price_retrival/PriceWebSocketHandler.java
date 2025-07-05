package com.flw5469.portfolio_tracker.price_retrival;

import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.client.WebSocketConnectionManager;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@Slf4j
public class PriceWebSocketHandler extends TextWebSocketHandler {
    

    private void printPayloadSymbol(String payload) {

        log.info("print symbol:\n");
        // log.info(payload);
        String pattern = "\"s\":\"";
        
        for (int i = 0; i <= payload.length() - pattern.length(); i++) {
            if (payload.startsWith(pattern, i)) {
                // Found the pattern, now find the symbol
                int startIndex = i + pattern.length();
                int endIndex = payload.indexOf("\"", startIndex);
                
                if (endIndex != -1) {
                    String symbol = payload.substring(startIndex, endIndex);
                    log.info(symbol);
                }
            }
        }
    }


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        session.setBinaryMessageSizeLimit(64 * 1024 * 1024);
        session.setTextMessageSizeLimit(64 * 1024 * 1024);

        log.info("WebSocket connected to Binance");
        
        // Subscribe to all price updates
        String subscribeMessage = """
        {
            "method": "SUBSCRIBE",
            "params": ["!miniTicker@arr"],
            "id": 1
        }
        """;
        
        session.sendMessage(new TextMessage(subscribeMessage));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        // Just log the received data for now
        //log.info("Received price data: {}", payload);
        log.info("The data size: {}", payload.length());
        // printPayloadSymbol(payload);
        // // Parse and extract price if needed
        // try {
        //     ObjectMapper mapper = new ObjectMapper();
        //     JsonNode data = mapper.readTree(payload);
            
        //     if (data.has("data")) {
        //         JsonNode tickerData = data.get("data");
        //         String symbol = tickerData.get("s").asText();
        //         String price = tickerData.get("c").asText();
                
        //         log.info("Price Update - {}: ${}", symbol, price);
        //     }
        // } catch (Exception e) {
        //     log.debug("Non-ticker message: {}", payload);
        // }
    }
    
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket error", exception);
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {}", status);
    }
}