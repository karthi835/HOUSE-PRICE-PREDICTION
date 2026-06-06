package com.predictor.houseprice.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinearRegressionModelTest {

    @Test
    void trainAndPredictWithSimpleData() {
        // Prepare a tiny synthetic dataset with 3 samples and 5 features
        CsvDataLoader loader = new CsvDataLoader() {
            @Override
            public double[][] getFeatures() {
                return new double[][]{
                        {1000, 3, 2, 10, 5},
                        {1500, 4, 3, 5, 7},
                        {800, 2, 1, 20, 4}
                };
            }

            @Override
            public double[] getPrices() {
                return new double[]{200000, 300000, 120000};
            }

            @Override
            public int getDataSize() {
                return 3;
            }

            @Override
            public int getFeatureCount() {
                return 5;
            }
        };

        LinearRegressionModel model = new LinearRegressionModel(loader);
        // call train directly (simulates @PostConstruct)
        model.train();

        assertTrue(model.isTrained());
        assertNotNull(model.getCoefficients());
        assertEquals(6, model.getCoefficients().length);

        double[] sample = new double[]{1100,3,2,8,5};
        double pred = model.predict(sample);
        assertTrue(pred > 0);
        assertTrue(model.getRSquared() >= 0 && model.getRSquared() <= 1);
    }
}
