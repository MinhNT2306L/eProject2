package com.example.restaurant_management.entity;

import java.time.LocalDate;

public class Ingredient {
    private int ingredientId;
    private String ingredientName;
    private double quantity;
    private String unit; // kg, g, cai
    private String supplier;
    private LocalDate importDate;
    private LocalDate expiryDate;
    private String status; // CON_HANG, HET_HANG, HET_HAN

    public Ingredient() {
    }

    public Ingredient(int ingredientId, String ingredientName, double quantity, String unit, 
                     String supplier, LocalDate importDate, LocalDate expiryDate, String status) {
        this.ingredientId = ingredientId;
        this.ingredientName = ingredientName;
        this.quantity = quantity;
        this.unit = unit;
        this.supplier = supplier;
        this.importDate = importDate;
        this.expiryDate = expiryDate;
        this.status = status;
    }

    public int getIngredientId() {
        return ingredientId;
    }

    public void setIngredientId(int ingredientId) {
        this.ingredientId = ingredientId;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public LocalDate getImportDate() {
        return importDate;
    }

    public void setImportDate(LocalDate importDate) {
        this.importDate = importDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}

