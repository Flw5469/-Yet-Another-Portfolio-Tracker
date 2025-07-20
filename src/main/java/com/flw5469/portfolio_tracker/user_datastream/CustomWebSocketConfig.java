package com.flw5469.portfolio_tracker.user_datastream;

import com.flw5469.portfolio_tracker.user_datastream.UserStreamHandler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@Service
public class CustomWebSocketConfig implements WebSocketConfigurer {
    
    private UserStreamHandler userStreamHandler;
    
    public CustomWebSocketConfig(UserStreamHandler userStreamHandler) {
        this.userStreamHandler = userStreamHandler;
    }
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(userStreamHandler, "/portfolio-stream")
                .setAllowedOrigins("*"); // For development only
    }
}