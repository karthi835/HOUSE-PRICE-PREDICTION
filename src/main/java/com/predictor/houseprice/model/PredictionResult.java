package com.predictor.houseprice.model;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

/**
 * PredictionResult - Data Transfer Object for prediction output.
 * 
 * Stores the result of a house price prediction along with the input features,
 * a unique ID, timestamp, and formatted price for display purposes.
 * Each prediction is stored in the in-memory history for dashboard display.
 */
public class PredictionResult {

    /** Unique identifier for this prediction */
    private String id;

    /** The input features used for this prediction */
    private HouseFeatures features;

    /** The predicted price in USD */
    private double predictedPrice;

    /** Timestamp when the prediction was made */
    private LocalDateTime timestamp;

    /** Confidence level of the prediction (Low / Medium / High) */
    private String confidence;

    // ===================== Constructors =====================

    public PredictionResult() {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.timestamp = LocalDateTime.now();
    }

    public PredictionResult(HouseFeatures features, double predictedPrice, String confidence) {
        this();
        this.features = features;
        this.predictedPrice = predictedPrice;
        this.confidence = confidence;
    }

    // ===================== Getters & Setters =====================

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HouseFeatures getFeatures() {
        return features;
    }

    public void setFeatures(HouseFeatures features) {
        this.features = features;
    }

    public double getPredictedPrice() {
        return predictedPrice;
    }

    public void setPredictedPrice(double predictedPrice) {
        this.predictedPrice = predictedPrice;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getConfidence() {
        return confidence;
    }

    public void setConfidence(String confidence) {
        this.confidence = confidence;
    }

    // ===================== Formatted Getters =====================

    /**
     * Returns the predicted price formatted as Indian currency (e.g., "₹4,25,000.00")
     */
    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        return formatter.format(predictedPrice);
    }

    /**
     * Returns the timestamp formatted for display (e.g., "May 31, 2026 07:30 AM")
     */
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");
        return timestamp.format(formatter);
    }

    /**
     * Returns the raw predicted price rounded to nearest integer (for charts)
     */
    public long getRoundedPrice() {
        return Math.round(predictedPrice);
    }

    @Override
    public String toString() {
        return String.format("PredictionResult[id=%s, price=%s, confidence=%s, time=%s]",
                id, getFormattedPrice(), confidence, getFormattedTimestamp());
    }
}
