package com.predictor.houseprice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * House Price Prediction System - Main Application Entry Point
 * 
 * This application uses a Multiple Linear Regression model (trained from scratch
 * using the Normal Equation method) to predict house prices based on features
 * like area, bedrooms, bathrooms, age, and location rating.
 * 
 * No external database is required — all data is loaded from a CSV file
 * and predictions are stored in-memory using ArrayList.
 * 
 * @author House Price Predictor Team
 * @version 1.0.0
 */
@SpringBootApplication
public class HousePriceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HousePriceApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  House Price Predictor is RUNNING!");
        System.out.println("  Open: http://localhost:8080");
        System.out.println("========================================\n");
    }
}
