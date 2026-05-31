package com.predictor.houseprice.controller;

import com.predictor.houseprice.model.HouseFeatures;
import com.predictor.houseprice.model.PredictionResult;
import com.predictor.houseprice.service.PredictionHistoryService;
import com.predictor.houseprice.service.PredictionService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * PredictionController - Handles all web requests for the application.
 * 
 * Routes:
 *   GET  /              → Home page
 *   GET  /predict        → Show prediction form
 *   POST /predict        → Process prediction (with validation)
 *   GET  /result/{id}    → Show a specific prediction result
 *   GET  /dashboard      → Show prediction history & charts
 *   GET  /api/history    → JSON API for chart data (AJAX)
 *   POST /dashboard/clear → Clear prediction history
 */
@Controller
public class PredictionController {

    private static final Logger logger = LoggerFactory.getLogger(PredictionController.class);

    private final PredictionService predictionService;
    private final PredictionHistoryService historyService;

    public PredictionController(PredictionService predictionService,
                                PredictionHistoryService historyService) {
        this.predictionService = predictionService;
        this.historyService = historyService;
    }

    // ===================== HOME PAGE =====================

    /**
     * Displays the home page with hero section and feature cards.
     */
    @GetMapping("/")
    public String homePage(Model model) {
        model.addAttribute("totalPredictions", historyService.getTotalCount());
        model.addAttribute("modelReady", predictionService.isModelReady());
        return "index";
    }

    // ===================== PREDICTION FORM =====================

    /**
     * Displays the prediction form with an empty HouseFeatures object.
     */
    @GetMapping("/predict")
    public String showPredictionForm(Model model) {
        model.addAttribute("houseFeatures", new HouseFeatures());
        return "predict";
    }

    /**
     * Processes the prediction form submission.
     * Validates input, runs the ML model, stores the result in history,
     * and redirects to the result page.
     */
    @PostMapping("/predict")
    public String processPrediction(@Valid @ModelAttribute("houseFeatures") HouseFeatures features,
                                    BindingResult bindingResult,
                                    Model model,
                                    RedirectAttributes redirectAttributes) {
        // If validation errors exist, re-show the form with error messages
        if (bindingResult.hasErrors()) {
            logger.warn("Validation errors: {}", bindingResult.getAllErrors());
            return "predict";
        }

        try {
            // Run the prediction
            PredictionResult result = predictionService.predict(features);

            // Store in history
            historyService.addPrediction(result);

            // Redirect to result page
            redirectAttributes.addFlashAttribute("predictionResult", result);
            return "redirect:/result/" + result.getId();

        } catch (Exception e) {
            logger.error("Prediction failed: {}", e.getMessage(), e);
            model.addAttribute("errorMessage", "Prediction failed: " + e.getMessage());
            return "predict";
        }
    }

    // ===================== RESULT PAGE =====================

    /**
     * Displays the prediction result for a specific prediction ID.
     */
    @GetMapping("/result/{id}")
    public String showResult(@PathVariable String id, Model model) {
        // Try to get result from flash attributes first (redirect scenario)
        if (model.containsAttribute("predictionResult")) {
            return "result";
        }

        // Otherwise, look up from history
        Optional<PredictionResult> result = historyService.findById(id);
        if (result.isPresent()) {
            model.addAttribute("predictionResult", result.get());
            return "result";
        }

        // Prediction not found — redirect to form
        return "redirect:/predict";
    }

    // ===================== DASHBOARD =====================

    /**
     * Displays the dashboard with prediction history, stats, and charts.
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        List<PredictionResult> predictions = historyService.getAllPredictions();
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));

        model.addAttribute("predictions", predictions);
        model.addAttribute("totalPredictions", historyService.getTotalCount());
        model.addAttribute("averagePrice", formatter.format(historyService.getAveragePrice()));
        model.addAttribute("highestPrice", formatter.format(historyService.getHighestPrice()));
        model.addAttribute("lowestPrice", formatter.format(historyService.getLowestPrice()));
        model.addAttribute("modelAccuracy", String.format("%.1f%%", predictionService.getModelAccuracy() * 100));

        // Prepare chart data as JSON-safe lists
        List<String> chartLabels = predictions.stream()
                .map(p -> "#" + p.getId())
                .collect(Collectors.toList());
        Collections.reverse(chartLabels);

        List<Long> chartPrices = predictions.stream()
                .map(PredictionResult::getRoundedPrice)
                .collect(Collectors.toList());
        Collections.reverse(chartPrices);

        List<Double> chartAreas = predictions.stream()
                .map(p -> p.getFeatures().getArea())
                .collect(Collectors.toList());
        Collections.reverse(chartAreas);

        model.addAttribute("chartLabels", chartLabels);
        model.addAttribute("chartPrices", chartPrices);
        model.addAttribute("chartAreas", chartAreas);

        return "dashboard";
    }

    /**
     * REST endpoint returning prediction history as JSON (for AJAX chart updates).
     */
    @GetMapping("/api/history")
    @ResponseBody
    public Map<String, Object> getHistoryApi() {
        List<PredictionResult> predictions = historyService.getAllPredictions();

        Map<String, Object> response = new HashMap<>();
        response.put("totalPredictions", historyService.getTotalCount());
        response.put("averagePrice", historyService.getAveragePrice());
        response.put("highestPrice", historyService.getHighestPrice());
        response.put("lowestPrice", historyService.getLowestPrice());

        List<Map<String, Object>> predictionData = predictions.stream()
                .map(p -> {
                    Map<String, Object> entry = new HashMap<>();
                    entry.put("id", p.getId());
                    entry.put("price", p.getRoundedPrice());
                    entry.put("area", p.getFeatures().getArea());
                    entry.put("bedrooms", p.getFeatures().getBedrooms());
                    entry.put("bathrooms", p.getFeatures().getBathrooms());
                    entry.put("age", p.getFeatures().getAge());
                    entry.put("locationRating", p.getFeatures().getLocationRating());
                    entry.put("confidence", p.getConfidence());
                    entry.put("timestamp", p.getFormattedTimestamp());
                    return entry;
                })
                .collect(Collectors.toList());

        response.put("predictions", predictionData);
        return response;
    }

    /**
     * Clears all prediction history and redirects back to the dashboard.
     */
    @PostMapping("/dashboard/clear")
    public String clearHistory(RedirectAttributes redirectAttributes) {
        historyService.clearHistory();
        redirectAttributes.addFlashAttribute("successMessage", "Prediction history cleared successfully!");
        return "redirect:/dashboard";
    }
}
