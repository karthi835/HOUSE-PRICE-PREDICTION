package com.predictor.houseprice.model;

import jakarta.validation.constraints.*;

/**
 * HouseFeatures - Data Transfer Object (DTO) for house prediction input.
 * 
 * Contains all the features used by the ML model to predict house prices.
 * Each field is validated using Jakarta Bean Validation annotations to ensure
 * data integrity before reaching the prediction engine.
 * 
 * Features:
 * - area: Total area in square feet (500 - 10,000 sq.ft)
 * - bedrooms: Number of bedrooms (1 - 10)
 * - bathrooms: Number of bathrooms (1 - 8)
 * - age: Age of the house in years (0 - 100)
 * - locationRating: Location desirability score (1.0 - 10.0)
 */
public class HouseFeatures {

    @NotNull(message = "Area is required")
    @Min(value = 500, message = "Area must be at least 500 sq.ft")
    @Max(value = 10000, message = "Area cannot exceed 10,000 sq.ft")
    private Double area;

    @NotNull(message = "Number of bedrooms is required")
    @Min(value = 1, message = "Must have at least 1 bedroom")
    @Max(value = 10, message = "Bedrooms cannot exceed 10")
    private Integer bedrooms;

    @NotNull(message = "Number of bathrooms is required")
    @Min(value = 1, message = "Must have at least 1 bathroom")
    @Max(value = 8, message = "Bathrooms cannot exceed 8")
    private Integer bathrooms;

    @NotNull(message = "Age of house is required")
    @Min(value = 0, message = "Age cannot be negative")
    @Max(value = 100, message = "Age cannot exceed 100 years")
    private Integer age;

    @NotNull(message = "Location rating is required")
    @DecimalMin(value = "1.0", message = "Location rating must be at least 1.0")
    @DecimalMax(value = "10.0", message = "Location rating cannot exceed 10.0")
    private Double locationRating;

    // ===================== Constructors =====================

    /** Default constructor required by Spring MVC form binding */
    public HouseFeatures() {
    }

    /** Parameterized constructor for programmatic creation */
    public HouseFeatures(Double area, Integer bedrooms, Integer bathrooms,
                         Integer age, Double locationRating) {
        this.area = area;
        this.bedrooms = bedrooms;
        this.bathrooms = bathrooms;
        this.age = age;
        this.locationRating = locationRating;
    }

    // ===================== Getters & Setters =====================

    public Double getArea() {
        return area;
    }

    public void setArea(Double area) {
        this.area = area;
    }

    public Integer getBedrooms() {
        return bedrooms;
    }

    public void setBedrooms(Integer bedrooms) {
        this.bedrooms = bedrooms;
    }

    public Integer getBathrooms() {
        return bathrooms;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getLocationRating() {
        return locationRating;
    }

    public void setLocationRating(Double locationRating) {
        this.locationRating = locationRating;
    }

    /**
     * Converts features to a double array for ML model input.
     * Order: [area, bedrooms, bathrooms, age, locationRating]
     */
    public double[] toArray() {
        return new double[]{
            area != null ? area : 0,
            bedrooms != null ? bedrooms : 0,
            bathrooms != null ? bathrooms : 0,
            age != null ? age : 0,
            locationRating != null ? locationRating : 0
        };
    }

    @Override
    public String toString() {
        return String.format("HouseFeatures[area=%.0f, beds=%d, baths=%d, age=%d, locRating=%.1f]",
                area, bedrooms, bathrooms, age, locationRating);
    }
}
