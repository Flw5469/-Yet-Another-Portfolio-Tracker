package com.flw5469.portfolio_tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.flw5469.portfolio_tracker.price_retrival.HistoricalPrice;

@SpringBootApplication
public class PortfolioTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioTrackerApplication.class, args);
    HistoricalPrice.getData();
	}

}
