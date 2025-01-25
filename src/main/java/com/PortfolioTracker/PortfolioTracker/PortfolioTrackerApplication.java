package com.PortfolioTracker.PortfolioTracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement
@SpringBootApplication
public class PortfolioTrackerApplication {

	public static void main(String[] args) {
		SpringApplication.run(PortfolioTrackerApplication.class, args);
	}

}
