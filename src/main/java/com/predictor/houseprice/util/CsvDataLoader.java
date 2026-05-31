package com.predictor.houseprice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * CsvDataLoader - Loads training data from the CSV file at startup.
 * 
 * Reads the house_prices.csv from the classpath resources and parses it
 * into feature arrays and price arrays that the ML model uses for training.
 * 
 * CSV Format: area,bedrooms,bathrooms,age,locationRating,price
 * 
 * Features:
 * - Handles header row automatically
 * - Trims whitespace from values
 * - Skips malformed rows with error logging
 * - Thread-safe (data loaded once at startup via @PostConstruct)
 */
@Component
public class CsvDataLoader {

    private static final Logger logger = LoggerFactory.getLogger(CsvDataLoader.class);

    /** Path to the CSV file in classpath */
    private static final String CSV_PATH = "data/house_prices.csv";

    /** Number of feature columns (area, bedrooms, bathrooms, age, locationRating) */
    private static final int FEATURE_COUNT = 5;

    /** Loaded feature data: each row is [area, bedrooms, bathrooms, age, locationRating] */
    private double[][] features;

    /** Loaded target prices corresponding to each feature row */
    private double[] prices;

    /** Total number of valid data rows loaded */
    private int dataSize;

    /**
     * Loads the CSV data from classpath on application startup.
     * Called automatically by Spring after bean construction.
     */
    @PostConstruct
    public void loadData() {
        logger.info("Loading training data from: {}", CSV_PATH);

        List<double[]> featureList = new ArrayList<>();
        List<Double> priceList = new ArrayList<>();

        try {
            ClassPathResource resource = new ClassPathResource(CSV_PATH);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));

            // Skip the header row
            String header = reader.readLine();
            if (header != null) {
                logger.info("CSV Header: {}", header.trim());
            }

            String line;
            int lineNumber = 1;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                line = line.trim();

                // Skip empty lines
                if (line.isEmpty()) {
                    continue;
                }

                try {
                    String[] values = line.split(",");

                    if (values.length < FEATURE_COUNT + 1) {
                        logger.warn("Line {}: Insufficient columns (expected {}, got {}). Skipping.",
                                lineNumber, FEATURE_COUNT + 1, values.length);
                        continue;
                    }

                    // Parse features: area, bedrooms, bathrooms, age, locationRating
                    double[] row = new double[FEATURE_COUNT];
                    row[0] = Double.parseDouble(values[0].trim()); // area
                    row[1] = Double.parseDouble(values[1].trim()); // bedrooms
                    row[2] = Double.parseDouble(values[2].trim()); // bathrooms
                    row[3] = Double.parseDouble(values[3].trim()); // age
                    row[4] = Double.parseDouble(values[4].trim()); // locationRating

                    // Parse target price
                    double price = Double.parseDouble(values[5].trim());

                    featureList.add(row);
                    priceList.add(price);

                } catch (NumberFormatException e) {
                    logger.warn("Line {}: Invalid number format. Skipping. Content: {}", lineNumber, line);
                }
            }

            reader.close();

        } catch (Exception e) {
            logger.error("Failed to load CSV data: {}", e.getMessage());
            throw new RuntimeException("Cannot load training data from " + CSV_PATH, e);
        }

        // Convert lists to arrays
        this.dataSize = featureList.size();
        this.features = new double[dataSize][FEATURE_COUNT];
        this.prices = new double[dataSize];

        for (int i = 0; i < dataSize; i++) {
            this.features[i] = featureList.get(i);
            this.prices[i] = priceList.get(i);
        }

        logger.info("Successfully loaded {} training samples with {} features each.",
                dataSize, FEATURE_COUNT);
    }

    // ===================== Getters =====================

    public double[][] getFeatures() {
        return features;
    }

    public double[] getPrices() {
        return prices;
    }

    public int getDataSize() {
        return dataSize;
    }

    public int getFeatureCount() {
        return FEATURE_COUNT;
    }
}
