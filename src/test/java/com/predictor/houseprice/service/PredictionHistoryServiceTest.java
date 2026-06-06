package com.predictor.houseprice.service;

import com.predictor.houseprice.model.HouseFeatures;
import com.predictor.houseprice.model.PredictionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PredictionHistoryServiceTest {

    private PredictionHistoryService historyService;

    @BeforeEach
    void setUp() {
        historyService = new PredictionHistoryService();
    }

    @Test
    void addAndRetrieveAndStats() {
        PredictionResult p1 = new PredictionResult(new HouseFeatures(800.0,2,1,5,5.0), 100000.0, "High");
        p1.setTimestamp(LocalDateTime.now().minusMinutes(5));
        p1.setId("A1");

        PredictionResult p2 = new PredictionResult(new HouseFeatures(1200.0,3,2,10,6.0), 200000.0, "High");
        p2.setTimestamp(LocalDateTime.now());
        p2.setId("B2");

        historyService.addPrediction(p1);
        historyService.addPrediction(p2);

        List<PredictionResult> all = historyService.getAllPredictions();
        assertEquals(2, all.size());
        // newest first
        assertEquals("B2", all.get(0).getId());

        assertEquals(2, historyService.getTotalCount());
        assertEquals(150000.0, historyService.getAveragePrice(), 0.1);
        assertEquals(200000.0, historyService.getHighestPrice(), 0.1);
        assertEquals(100000.0, historyService.getLowestPrice(), 0.1);

        historyService.clearHistory();
        assertEquals(0, historyService.getTotalCount());
    }
}
