package com.example.restaurant_management.entity;

public class Table {
    private int tableId;
    private int tableNumber;
    private String status;

    public Table(int tableId, int tableNumber, String status) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.status = status;
    }

    public int getTableId() {
        return tableId;
    }

    public void setTableId(int tableId) {
        this.tableId = tableId;
    }

    public int getTableNumber() {
        return tableNumber;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
