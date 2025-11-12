package com.example.restaurant_management.Controller;

public class BillModel {
    private String tenMon;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public BillModel(String tenMon, int soLuong, double donGia) {
        this.tenMon = tenMon;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = soLuong * donGia;
    }

    public String getTenMon() { return tenMon; }
    public int getSoLuong() { return soLuong; }
    public double getDonGia() { return donGia; }
    public double getThanhTien() { return thanhTien; }
}
