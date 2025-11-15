package com.example.restaurant_management.entity;

import javafx.beans.property.*;

public class Food {
    private final IntegerProperty foodId = new SimpleIntegerProperty(this, "foodId");
    private final StringProperty foodName = new SimpleStringProperty(this, "foodName");
    private final StringProperty foodCategory = new SimpleStringProperty(this, "foodCategory");
    private final DoubleProperty price = new SimpleDoubleProperty(this, "price");
    private final StringProperty status = new SimpleStringProperty(this, "status");
    private final StringProperty description = new SimpleStringProperty(this, "description");

    // Constructor
    public Food(int foodId, String foodName, String foodCategory, double price, String status, String description) {
        this.foodId.set(foodId);
        this.foodName.set(foodName);
        this.foodCategory.set(foodCategory);
        this.price.set(price);
        this.status.set(status);
        this.description.set(description);
    }

    // === JavaFX Properties ===
    public IntegerProperty foodIdProperty() { return foodId; }
    public StringProperty foodNameProperty() { return foodName; }
    public StringProperty foodCategoryProperty() { return foodCategory; }
    public DoubleProperty priceProperty() { return price; }
    public StringProperty statusProperty() { return status; }
    public StringProperty descriptionProperty() { return description; }

    // === Getters & Setters ===
    public int getFoodId() { return foodId.get(); }
    public void setFoodId(int foodId) { this.foodId.set(foodId); }

    public String getFoodName() { return foodName.get(); }
    public void setFoodName(String foodName) { this.foodName.set(foodName); }

    public String getFoodCategory() { return foodCategory.get(); }
    public void setFoodCategory(String foodCategory) { this.foodCategory.set(foodCategory); }

    public double getPrice() { return price.get(); }
    public void setPrice(double price) { this.price.set(price); }

    public String getStatus() { return status.get(); }
    public void setStatus(String status) { this.status.set(status); }

    public String getDescription() { return description.get(); }
    public void setDescription(String description) { this.description.set(description); }

    @Override
    public String toString() {
        return foodName.get();
    }
}