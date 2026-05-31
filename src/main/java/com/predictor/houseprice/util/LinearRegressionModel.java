package com.predictor.houseprice.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

/**
 * LinearRegressionModel - Multiple Linear Regression engine built from scratch.
 * 
 * Implements the Normal Equation method for training:
 *     β = (XᵀX)⁻¹ · Xᵀy
 * 
 * where:
 *   X = design matrix (features with bias column)
 *   y = target vector (prices)
 *   β = coefficient vector (weights)
 * 
 * Features are standardized using Z-score normalization before training
 * to improve numerical stability. The same normalization is applied
 * during prediction to ensure consistency.
 * 
 * This class performs all matrix operations (transpose, multiply, invert)
 * from scratch without any external linear algebra library.
 * 
 * @author House Price Predictor Team
 * @version 1.0.0
 */
@Component
public class LinearRegressionModel {

    private static final Logger logger = LoggerFactory.getLogger(LinearRegressionModel.class);

    private final CsvDataLoader dataLoader;

    /** Model coefficients (weights): β₀ (intercept), β₁, β₂, ..., βₙ */
    private double[] coefficients;

    /** Feature means for Z-score normalization */
    private double[] featureMeans;

    /** Feature standard deviations for Z-score normalization */
    private double[] featureStds;

    /** R² score (coefficient of determination) — model accuracy metric */
    private double rSquared;

    /** Whether the model has been successfully trained */
    private boolean trained = false;

    public LinearRegressionModel(CsvDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    /**
     * Trains the model on startup using data loaded by CsvDataLoader.
     * Called automatically by Spring after all dependencies are injected.
     */
    @PostConstruct
    public void train() {
        logger.info("Training Linear Regression model...");

        double[][] rawFeatures = dataLoader.getFeatures();
        double[] prices = dataLoader.getPrices();
        int n = dataLoader.getDataSize();
        int featureCount = dataLoader.getFeatureCount();

        if (n == 0) {
            logger.error("No training data available. Model cannot be trained.");
            return;
        }

        // Step 1: Compute feature means and standard deviations
        computeNormalizationParams(rawFeatures, n, featureCount);

        // Step 2: Normalize features using Z-score standardization
        double[][] normalizedFeatures = normalizeFeatures(rawFeatures, n, featureCount);

        // Step 3: Build the design matrix X with bias column (column of 1s)
        // X has dimensions [n × (featureCount + 1)]
        double[][] X = new double[n][featureCount + 1];
        for (int i = 0; i < n; i++) {
            X[i][0] = 1.0; // Bias term (intercept)
            for (int j = 0; j < featureCount; j++) {
                X[i][j + 1] = normalizedFeatures[i][j];
            }
        }

        // Step 4: Apply Normal Equation: β = (XᵀX)⁻¹ · Xᵀy
        double[][] Xt = transpose(X);           // Xᵀ [p × n]
        double[][] XtX = multiply(Xt, X);       // XᵀX [p × p]
        double[][] XtX_inv = invert(XtX);       // (XᵀX)⁻¹ [p × p]
        double[][] Xty = multiplyVector(Xt, prices); // Xᵀy [p × 1]

        // β = (XᵀX)⁻¹ · Xᵀy
        this.coefficients = new double[featureCount + 1];
        for (int i = 0; i < featureCount + 1; i++) {
            double sum = 0.0;
            for (int j = 0; j < featureCount + 1; j++) {
                sum += XtX_inv[i][j] * Xty[j][0];
            }
            this.coefficients[i] = sum;
        }

        // Step 5: Calculate R² score
        this.rSquared = calculateRSquared(X, prices);
        this.trained = true;

        // Log model summary
        logger.info("Model trained successfully!");
        logger.info("R² Score: {}", String.format("%.4f", rSquared));
        logger.info("Intercept (β₀): {}", String.format("%.2f", coefficients[0]));
        String[] featureNames = {"Area", "Bedrooms", "Bathrooms", "Age", "LocationRating"};
        for (int i = 0; i < featureCount; i++) {
            logger.info("Coefficient for {} (β{}): {}", featureNames[i], i + 1,
                    String.format("%.2f", coefficients[i + 1]));
        }
    }

    /**
     * Predicts the house price for given features.
     * 
     * @param features Raw feature values: [area, bedrooms, bathrooms, age, locationRating]
     * @return Predicted price in USD
     * @throws IllegalStateException if model has not been trained
     */
    public double predict(double[] features) {
        if (!trained) {
            throw new IllegalStateException("Model has not been trained yet.");
        }

        // Normalize input features using stored means and stds
        double[] normalized = new double[features.length];
        for (int i = 0; i < features.length; i++) {
            if (featureStds[i] != 0) {
                normalized[i] = (features[i] - featureMeans[i]) / featureStds[i];
            } else {
                normalized[i] = 0;
            }
        }

        // Calculate prediction: price = β₀ + β₁·x₁ + β₂·x₂ + ... + βₙ·xₙ
        double prediction = coefficients[0]; // Intercept
        for (int i = 0; i < features.length; i++) {
            prediction += coefficients[i + 1] * normalized[i];
        }

        return prediction;
    }

    // ===================== Normalization =====================

    /**
     * Computes mean and standard deviation for each feature column.
     */
    private void computeNormalizationParams(double[][] features, int n, int featureCount) {
        featureMeans = new double[featureCount];
        featureStds = new double[featureCount];

        // Calculate means
        for (int j = 0; j < featureCount; j++) {
            double sum = 0;
            for (int i = 0; i < n; i++) {
                sum += features[i][j];
            }
            featureMeans[j] = sum / n;
        }

        // Calculate standard deviations
        for (int j = 0; j < featureCount; j++) {
            double sumSqDiff = 0;
            for (int i = 0; i < n; i++) {
                double diff = features[i][j] - featureMeans[j];
                sumSqDiff += diff * diff;
            }
            featureStds[j] = Math.sqrt(sumSqDiff / n);
        }
    }

    /**
     * Normalizes features using Z-score: z = (x - mean) / std
     */
    private double[][] normalizeFeatures(double[][] features, int n, int featureCount) {
        double[][] normalized = new double[n][featureCount];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < featureCount; j++) {
                if (featureStds[j] != 0) {
                    normalized[i][j] = (features[i][j] - featureMeans[j]) / featureStds[j];
                } else {
                    normalized[i][j] = 0;
                }
            }
        }
        return normalized;
    }

    // ===================== Matrix Operations =====================

    /**
     * Transposes a matrix: rows become columns and vice versa.
     * Input: [m × n] → Output: [n × m]
     */
    private double[][] transpose(double[][] matrix) {
        int rows = matrix.length;
        int cols = matrix[0].length;
        double[][] result = new double[cols][rows];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result[j][i] = matrix[i][j];
            }
        }
        return result;
    }

    /**
     * Multiplies two matrices: A[m×n] × B[n×p] = C[m×p]
     */
    private double[][] multiply(double[][] A, double[][] B) {
        int m = A.length;
        int n = A[0].length;
        int p = B[0].length;
        double[][] result = new double[m][p];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < p; j++) {
                double sum = 0;
                for (int k = 0; k < n; k++) {
                    sum += A[i][k] * B[k][j];
                }
                result[i][j] = sum;
            }
        }
        return result;
    }

    /**
     * Multiplies a matrix by a vector: A[m×n] × v[n] = result[m×1]
     */
    private double[][] multiplyVector(double[][] matrix, double[] vector) {
        int m = matrix.length;
        int n = matrix[0].length;
        double[][] result = new double[m][1];
        for (int i = 0; i < m; i++) {
            double sum = 0;
            for (int j = 0; j < n; j++) {
                sum += matrix[i][j] * vector[j];
            }
            result[i][0] = sum;
        }
        return result;
    }

    /**
     * Inverts a square matrix using Gauss-Jordan Elimination.
     * 
     * Creates an augmented matrix [A | I], then performs row operations
     * to transform A into I, which transforms I into A⁻¹.
     * 
     * @param matrix Square matrix to invert
     * @return The inverse matrix
     */
    private double[][] invert(double[][] matrix) {
        int n = matrix.length;
        double[][] augmented = new double[n][2 * n];

        // Build augmented matrix [A | I]
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                augmented[i][j] = matrix[i][j];
            }
            augmented[i][i + n] = 1.0; // Identity matrix
        }

        // Forward elimination with partial pivoting
        for (int col = 0; col < n; col++) {
            // Find pivot row (row with max absolute value in current column)
            int maxRow = col;
            double maxVal = Math.abs(augmented[col][col]);
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(augmented[row][col]) > maxVal) {
                    maxVal = Math.abs(augmented[row][col]);
                    maxRow = row;
                }
            }

            // Swap rows if necessary
            if (maxRow != col) {
                double[] temp = augmented[col];
                augmented[col] = augmented[maxRow];
                augmented[maxRow] = temp;
            }

            // Check for singular matrix
            double pivot = augmented[col][col];
            if (Math.abs(pivot) < 1e-10) {
                logger.warn("Matrix is nearly singular. Adding regularization.");
                pivot = 1e-10;
                augmented[col][col] = pivot;
            }

            // Scale pivot row
            for (int j = 0; j < 2 * n; j++) {
                augmented[col][j] /= pivot;
            }

            // Eliminate column entries in other rows
            for (int row = 0; row < n; row++) {
                if (row != col) {
                    double factor = augmented[row][col];
                    for (int j = 0; j < 2 * n; j++) {
                        augmented[row][j] -= factor * augmented[col][j];
                    }
                }
            }
        }

        // Extract the inverse from the right half of augmented matrix
        double[][] inverse = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                inverse[i][j] = augmented[i][j + n];
            }
        }

        return inverse;
    }

    // ===================== Model Evaluation =====================

    /**
     * Calculates the R² (coefficient of determination) score.
     * 
     * R² = 1 - (SS_res / SS_tot)
     * where:
     *   SS_res = Σ(yᵢ - ŷᵢ)² (residual sum of squares)
     *   SS_tot = Σ(yᵢ - ȳ)²  (total sum of squares)
     * 
     * R² ∈ [0, 1], where 1 means perfect fit.
     */
    private double calculateRSquared(double[][] X, double[] y) {
        int n = y.length;

        // Calculate mean of y
        double yMean = 0;
        for (double v : y) {
            yMean += v;
        }
        yMean /= n;

        double ssRes = 0; // Residual sum of squares
        double ssTot = 0; // Total sum of squares

        for (int i = 0; i < n; i++) {
            // Predicted value: ŷ = X[i] · β
            double yPred = 0;
            for (int j = 0; j < coefficients.length; j++) {
                yPred += X[i][j] * coefficients[j];
            }
            ssRes += Math.pow(y[i] - yPred, 2);
            ssTot += Math.pow(y[i] - yMean, 2);
        }

        return (ssTot == 0) ? 0 : 1.0 - (ssRes / ssTot);
    }

    // ===================== Getters =====================

    public double getRSquared() {
        return rSquared;
    }

    public double[] getCoefficients() {
        return coefficients;
    }

    public boolean isTrained() {
        return trained;
    }
}
