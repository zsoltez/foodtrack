package com.example.foodtrack.model;

public class UserProfile {
    private static final int DEFAULT_CALORIES = 2000;
    private static final int DEFAULT_PROTEIN = 100;
    private static final int DEFAULT_CARBS = 250;
    private static final int DEFAULT_FAT = 67;
    public boolean isCustom;
    public String sex;
    public int age;
    public double height;
    public double weight;
    public String goal;
    public String activityLevel;
    public int calories;
    public int protein;
    public int carbs;
    public int fat;

    public static UserProfile createDefaultProfile() {
        return new UserProfile();
    }

    public static UserProfile createCustomProfile(String sex, int age, double height, double weight, String goal, String activityLevel, int calories, int protein, int carbs, int fat) {
        return new UserProfile(sex, age, height, weight, goal, activityLevel, calories, protein, carbs, fat);
    }

    public UserProfile() {
        this.isCustom = false;
        this.calories = DEFAULT_CALORIES;
        this.protein = DEFAULT_PROTEIN;
        this.carbs = DEFAULT_CARBS;
        this.fat = DEFAULT_FAT;
    }

    private UserProfile(String sex, int age, double height, double weight, String goal, String activityLevel, int calories, int protein, int carbs, int fat) {
        this.isCustom = true;
        this.sex = sex;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.goal = goal;
        this.activityLevel = activityLevel;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
}

