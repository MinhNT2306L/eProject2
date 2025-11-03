package com.example.restaurant_management.entity;

public class Food {
    private int foodId;
    private String foodName;
    private String foodCategory;
    private double price;
    private String description;

    public Food(int foodId, String foodName, String foodCategory, double price, String description) {
        this.foodId = foodId;
        this.foodName = foodName;
        this.foodCategory = foodCategory;
        this.price = price;
        this.description = description;
    }

    public int getFoodId() {
        return foodId;
    }

    public void setFoodId(int foodId) {
        this.foodId = foodId;
    }

    public String getFoodName() {
        return foodName;
    }

    public void setFoodName(String foodName) {
        this.foodName = foodName;
    }

    public String getFoodCategory() {
        return foodCategory;
    }

    public void setFoodCategory(String foodCategory) {
        this.foodCategory = foodCategory;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
