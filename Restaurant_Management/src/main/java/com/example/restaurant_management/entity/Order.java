package com.example.restaurant_management.entity;

import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private Integer khId; // Customer ID (nullable)
    private Integer nvId; // Staff ID (nullable)
    private Integer banId; // Table ID (nullable)
    private LocalDateTime thoiGian;
    private double tongTien;
    private String trangThai; // MOI, DANG_PHUC_VU, DA_THANH_TOAN, DA_HUY

    public Order() {
    }

    public Order(int orderId, Integer khId, Integer nvId, Integer banId, LocalDateTime thoiGian, double tongTien, String trangThai) {
        this.orderId = orderId;
        this.khId = khId;
        this.nvId = nvId;
        this.banId = banId;
        this.thoiGian = thoiGian;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Integer getKhId() {
        return khId;
    }

    public void setKhId(Integer khId) {
        this.khId = khId;
    }

    public Integer getNvId() {
        return nvId;
    }

    public void setNvId(Integer nvId) {
        this.nvId = nvId;
    }

    public Integer getBanId() {
        return banId;
    }

    public void setBanId(Integer banId) {
        this.banId = banId;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}

