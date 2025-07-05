package com.flw5469.portfolio_tracker.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController {
    @GetMapping("/testing")
    public String testing() {
        return "Welcome to Our Real-Time Portfolio Tracking App!";
    }
}
