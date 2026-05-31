package com.predictor.houseprice.service;

import com.predictor.houseprice.model.HouseFeatures;
import com.predictor.houseprice.model.PredictionResult;
import com.predictor.houseprice.util.LinearRegressionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * PredictionService - Orchestrates the house price prediction workflow.
 * 
 * This service sits between the controller and the ML model, handling:
 * - Feature extraction from the input DTO
 * - Calling the Linear Regression model for prediction
 * - Clamping negative predictions to a minimum floor value
 * - Determining prediction confidence based on feature ranges
 * - Building the PredictionResult response DTO
 */
@Service
public class PredictionService {

    private static final Logger logger = LoggerFactory.getLogger(PredictionService.class);

    /** Minimum price floor — predictions below this are clamped */
    private static final double MIN_PRICE = 25000.0;

    private final LinearRegressionModel model;

    public PredictionService(LinearRegressionModel model) {
        this.model = model;
    }

    /**
     * Predicts the house price based on the given features.
     * 
     * @param features Validated house features from the form
     * @return PredictionResult containing the predicted price and metadata
     */
    public PredictionResult predict(HouseFeatures features) {
        logger.info("Predicting price for: {}", features);

        // Convert features to array for model input
        double[] featureArray = features.toArray();

        // Get raw prediction from the ML model
        double rawPrediction = model.predict(featureArray);
        logger.info("Raw model prediction: ₹{}", String.format("%.2f", rawPrediction));

        // Clamp negative or unrealistically low predictions
        double finalPrice = Math.max(rawPrediction, MIN_PRICE);

        // Round to nearest hundred for cleaner display
        finalPrice = Math.round(finalPrice / 100.0) * 100.0;

        // Determine confidence level
        String confidence = determineConfidence(features);

        // Build the result
        PredictionResult result = new PredictionResult(features, finalPrice, confidence);
        logger.info("Final prediction: {}", result);

        return result;
    }

    /**
     * Determines the confidence level of a prediction based on whether
     * the input features fall within typical training data ranges.
     * 
     * @param features The input features to evaluate
     * @return "High", "Medium", or "Low" confidence string
     */
    private String determineConfidence(HouseFeatures features) {
        int score = 0;

        // Check if features are within typical ranges from training data
        if (features.getArea() >= 800 && features.getArea() <= 5000) score++;
        if (features.getBedrooms() >= 2 && features.getBedrooms() <= 6) score++;
        if (features.getBathrooms() >= 1 && features.getBathrooms() <= 4) score++;
        if (features.getAge() >= 0 && features.getAge() <= 40) score++;
        if (features.getLocationRating() >= 3.0 && features.getLocationRating() <= 9.0) score++;

        if (score >= 4) return "High";
        if (score >= 2) return "Medium";
        return "Low";
    }

    /**
     * Returns the model's R² score for display on the dashboard.
     */
    public double getModelAccuracy() {
        return model.getRSquared();
    }

    /**
     * Returns whether the model is trained and ready for predictions.
     */
    public boolean isModelReady() {
        return model.isTrained();
    }
}
