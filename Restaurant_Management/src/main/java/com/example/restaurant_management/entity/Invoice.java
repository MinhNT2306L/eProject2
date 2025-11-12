package com.example.restaurant_management.entity;

import java.time.LocalDateTime;

public class Invoice {
    private int hoadonId;
    private Integer banId;
    private double tongTien;
    private String phuongThuc; // Payment method: Cash, Bank Transfer
    private LocalDateTime thoiGian;

    public Invoice() {
    }

    public Invoice(int hoadonId, Integer banId, double tongTien, String phuongThuc, LocalDateTime thoiGian) {
        this.hoadonId = hoadonId;
        this.banId = banId;
        this.tongTien = tongTien;
        this.phuongThuc = phuongThuc;
        this.thoiGian = thoiGian;
    }

    public int getHoadonId() {
        return hoadonId;
    }

    public void setHoadonId(int hoadonId) {
        this.hoadonId = hoadonId;
    }

    public Integer getBanId() {
        return banId;
    }

    public void setBanId(Integer banId) {
        this.banId = banId;
    }

    public double getTongTien() {
        return tongTien;
    }

    public void setTongTien(double tongTien) {
        this.tongTien = tongTien;
    }

    public String getPhuongThuc() {
        return phuongThuc;
    }

    public void setPhuongThuc(String phuongThuc) {
        this.phuongThuc = phuongThuc;
    }

    public LocalDateTime getThoiGian() {
        return thoiGian;
    }

    public void setThoiGian(LocalDateTime thoiGian) {
        this.thoiGian = thoiGian;
    }
}

