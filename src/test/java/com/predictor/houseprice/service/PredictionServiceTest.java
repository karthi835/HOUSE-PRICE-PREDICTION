package com.predictor.houseprice.service;

import com.predictor.houseprice.model.HouseFeatures;
import com.predictor.houseprice.model.PredictionResult;
import com.predictor.houseprice.util.LinearRegressionModel;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class PredictionServiceTest {

    @Test
    void predictUsesModelAndClampsAndRounds() {
        LinearRegressionModel mockModel = Mockito.mock(LinearRegressionModel.class);
        Mockito.when(mockModel.predict(Mockito.any(double[].class))).thenReturn(52345.67);
        Mockito.when(mockModel.getRSquared()).thenReturn(0.85);
        Mockito.when(mockModel.isTrained()).thenReturn(true);

        PredictionService svc = new PredictionService(mockModel);

        HouseFeatures hf = new HouseFeatures(1000.0,3,2,10,5.0);
        PredictionResult res = svc.predict(hf);

        assertNotNull(res);
        // rounded to nearest 100
        assertEquals(52300.0, res.getPredictedPrice(), 0.1);
        assertEquals("High", res.getConfidence());

        assertEquals(0.85, svc.getModelAccuracy(), 1e-6);
        assertTrue(svc.isModelReady());
    }
}
