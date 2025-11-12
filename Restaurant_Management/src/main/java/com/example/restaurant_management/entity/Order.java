package com.example.restaurant_management.entity;

import java.time.LocalDateTime;

public class Order {
    private int orderId;
    private Integer khId;
    private Integer nvId;
    private Integer banId;
    private LocalDateTime thoiGian;
    private double tongTien;
    private String trangThai; // MOI, DANG_PHUC_VU, DA_THANH_TOAN, DA_HUY

    public Order(int orderId, Integer khId, Integer nvId, Integer banId,
                 LocalDateTime thoiGian, double tongTien, String trangThai) {
        this.orderId = orderId;
        this.khId = khId;
        this.nvId = nvId;
        this.banId = banId;
        this.thoiGian = thoiGian;
        this.tongTien = tongTien;
        this.trangThai = trangThai;
    }

    public int getOrderId() { return orderId; }
    public Integer getBanId() { return banId; }
    public double getTongTien() { return tongTien; }
    public String getTrangThai() { return trangThai; }
    public void setTongTien(double t) { this.tongTien = t; }
    public void setTrangThai(String s) { this.trangThai = s; }
}
