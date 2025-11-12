package com.example.restaurant_management.entity;

public class Table {
    private int tableId;
    private String tableNumber; // ví dụ "Bàn 5"
    private String status;      // TRONG | PHUC_VU | DAT_TRUOC

    public Table(int tableId, String tableNumber, String status) {
        this.tableId = tableId;
        this.tableNumber = tableNumber;
        this.status = status;
    }

    public int getTableId() { return tableId; }
    public String getTableNumber() { return tableNumber; }
    public String getStatus() { return status; }
}
