package com.flw5469.portfolio_tracker.user_datastream;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.Random;

@Service
public class UserStreamHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> authenticatedSessions = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Random random = new Random();
    
    // Fake API keys for demonstration
    private final String[] validApiKeys = {"test-key-123", "demo-key-456", "fake-key-789"};
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        
        // Check if this is an API key authentication message
        if (payload.startsWith("AUTH:")) {
            String apiKey = payload.substring(5);
            
            if (isValidApiKey(apiKey)) {
                authenticatedSessions.put(session.getId(), session);
                session.sendMessage(new TextMessage("AUTH_SUCCESS"));
                startStreamingData(session);
            } else {
                session.sendMessage(new TextMessage("AUTH_FAILED"));
                session.close();
            }
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        authenticatedSessions.remove(session.getId());
        System.out.println("WebSocket connection closed: " + session.getId());
    }
    
    private boolean isValidApiKey(String apiKey) {
      for (String validKey : validApiKeys) {
            if (validKey.equals(apiKey)) {
                return true;
            }
        }
        return false;
    }
    
    private void startStreamingData(WebSocketSession session) {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (session.isOpen() && authenticatedSessions.containsKey(session.getId())) {
                    String data = generateFakePortfolioData();
                    session.sendMessage(new TextMessage(data));
                }
            } catch (Exception e) {
                System.err.println("Error sending data: " + e.getMessage());
                authenticatedSessions.remove(session.getId());
            }
        }, 0, 2, TimeUnit.SECONDS); // Send data every 2 seconds
    }
    
    private String generateFakePortfolioData() {
        try {
            Map<String, Object> data = Map.of(
                "timestamp", System.currentTimeMillis(),
                "portfolio_value", 10000 + (random.nextDouble() * 5000),
                "stocks", Map.of(
                    "AAPL", 150 + (random.nextDouble() * 20),
                    "GOOGL", 2500 + (random.nextDouble() * 200),
                    "TSLA", 800 + (random.nextDouble() * 100)
                ),
                "daily_change", (random.nextDouble() - 0.5) * 1000
            );
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            return "{\"error\":\"Failed to generate data\"}";
        }
    }
}