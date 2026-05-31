package com.predictor.houseprice.service;

import com.predictor.houseprice.model.PredictionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * PredictionHistoryService - In-memory storage for prediction history.
 * 
 * Stores all predictions made during the application session in an ArrayList.
 * Data is NOT persisted across restarts — this fulfills the "no database" requirement.
 * 
 * Thread-safe via Collections.synchronizedList wrapper.
 * 
 * Features:
 * - Add new predictions
 * - Retrieve all predictions (newest first)
 * - Get recent N predictions
 * - Calculate statistics (average, min, max, total count)
 * - Clear history
 */
@Service
public class PredictionHistoryService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionHistoryService.class);

    /** Thread-safe in-memory storage for prediction history */
    private final List<PredictionResult> history = Collections.synchronizedList(new ArrayList<>());

    /**
     * Adds a new prediction result to the history.
     * 
     * @param result The prediction result to store
     */
    public void addPrediction(PredictionResult result) {
        history.add(result);
        logger.info("Prediction added to history. Total predictions: {}", history.size());
    }

    /**
     * Returns all predictions, sorted by timestamp (newest first).
     * 
     * @return Unmodifiable list of all predictions
     */
    public List<PredictionResult> getAllPredictions() {
        List<PredictionResult> sorted = new ArrayList<>(history);
        sorted.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return Collections.unmodifiableList(sorted);
    }

    /**
     * Returns the most recent N predictions.
     * 
     * @param n Number of recent predictions to return
     * @return List of recent predictions (newest first)
     */
    public List<PredictionResult> getRecentPredictions(int n) {
        List<PredictionResult> all = getAllPredictions();
        return all.subList(0, Math.min(n, all.size()));
    }

    /**
     * Finds a prediction by its unique ID.
     * 
     * @param id The prediction ID to search for
     * @return Optional containing the prediction if found
     */
    public Optional<PredictionResult> findById(String id) {
        return history.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    /**
     * Returns the total number of predictions made.
     */
    public int getTotalCount() {
        return history.size();
    }

    /**
     * Calculates the average predicted price across all predictions.
     * Returns 0 if no predictions exist.
     */
    public double getAveragePrice() {
        if (history.isEmpty()) return 0;
        return history.stream()
                .mapToDouble(PredictionResult::getPredictedPrice)
                .average()
                .orElse(0);
    }

    /**
     * Returns the highest predicted price, or 0 if no predictions exist.
     */
    public double getHighestPrice() {
        return history.stream()
                .mapToDouble(PredictionResult::getPredictedPrice)
                .max()
                .orElse(0);
    }

    /**
     * Returns the lowest predicted price, or 0 if no predictions exist.
     */
    public double getLowestPrice() {
        return history.stream()
                .mapToDouble(PredictionResult::getPredictedPrice)
                .min()
                .orElse(0);
    }

    /**
     * Clears all prediction history.
     */
    public void clearHistory() {
        int count = history.size();
        history.clear();
        logger.info("Prediction history cleared. Removed {} records.", count);
    }
}
