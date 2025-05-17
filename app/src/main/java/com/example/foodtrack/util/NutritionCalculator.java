package com.example.foodtrack.util;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class NutritionCalculator {

    public static Map<String, Integer> calculateDaily(String sex, int age, double height, double weight, String goal, String activityLevel) {

        double activityMultiplier;
        switch (activityLevel) {
            case "sedentary":
                activityMultiplier = 1.2;
                break;
            case "easy":
                activityMultiplier = 1.375;
                break;
            case "moderate":
                activityMultiplier = 1.55;
                break;
            case "very":
                activityMultiplier = 1.725;
                break;
            case "extremely":
                activityMultiplier = 1.9;
                break;
            default:
                activityMultiplier = 1.5;
                break;
        }

        int goalAdjustment;
        switch (goal) {
            case "lose_weight":
                goalAdjustment = -750;
                break;
            case "gain_weight":
                goalAdjustment = 750;
                break;
            default:
                goalAdjustment = 0;
                break;
        }

        double bmr;
        if (sex.equalsIgnoreCase("male")) {
            bmr = 66.47 + (13.75 * weight) + (5.003 * height) - (6.755 * age);
        } else {
            bmr = 655.1 + (9.563 * weight) + (1.850 * height) - (4.676 * age);
        }

        double tdee = bmr * activityMultiplier;
        int calories = (int) Math.round(tdee + goalAdjustment);

        // 50-20-30 a standard arány
        // carb 50% - 4kcal/g
        // protein 20% - 4kcal/g
        // fat 30% - 9kcal/g
        int carbs = (int) Math.round((calories * 0.5) / 4);
        int protein = (int) Math.round((calories * 0.2) / 4);
        int fat = (int) Math.round((calories * 0.3) / 9);

        Map<String, Integer> result = new HashMap<>();
        result.put("daily_calories", calories);
        result.put("daily_carbs", carbs);
        result.put("daily_protein", protein);
        result.put("daily_fats", fat);

        return result;
    }
}
