package com.predictor.houseprice.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HouseFeaturesTest {

    @Test
    void toArrayAndToStringWorks() {
        HouseFeatures hf = new HouseFeatures(1200.0, 3, 2, 10, 6.5);

        double[] arr = hf.toArray();
        assertEquals(5, arr.length);
        assertEquals(1200.0, arr[0]);
        assertEquals(3.0, arr[1]);
        assertEquals(2.0, arr[2]);
        assertEquals(10.0, arr[3]);
        assertEquals(6.5, arr[4]);

        String s = hf.toString();
        assertTrue(s.contains("HouseFeatures"));
        assertTrue(s.contains("beds=3"));
    }
}
