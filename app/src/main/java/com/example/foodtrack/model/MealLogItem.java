package com.example.foodtrack.model;

public class MealLogItem {
    public String logId;
    public String foodName;
    public String brand;
    public int amount; // gramm
    public int calories;
    public int protein;
    public int carbs;
    public int fat;

    public MealLogItem(String logId, String foodName, String brand, int amount, int calories, int protein, int carbs, int fat) {
        this.logId = logId;
        this.foodName = foodName;
        this.brand = brand;
        this.amount = amount;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
}

