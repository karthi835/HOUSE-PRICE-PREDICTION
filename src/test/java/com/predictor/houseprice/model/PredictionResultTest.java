package com.predictor.houseprice.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class PredictionResultTest {

    @Test
    void formattedAndRoundedPriceAndTimestamp() {
        HouseFeatures hf = new HouseFeatures(1000.0,2,1,5,5.0);
        PredictionResult pr = new PredictionResult(hf, 425000.75, "High");

        assertEquals(425001L, pr.getRoundedPrice());
        String formatted = pr.getFormattedPrice();
        assertNotNull(formatted);
        assertTrue(formatted.contains("₹") || formatted.contains("INR"));

        // Ensure timestamp formatting doesn't throw
        String ts = pr.getFormattedTimestamp();
        assertNotNull(ts);
    }
}
