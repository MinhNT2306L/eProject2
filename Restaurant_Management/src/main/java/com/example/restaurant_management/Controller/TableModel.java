package com.example.restaurant_management.Controller;

public class TableModel {
    private int id;
    private String name;
    private String status;

    public TableModel(int id, String name, String status) {
        this.id = id;
        this.name = name;
        this.status = status;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getStatus() { return status; }
}
