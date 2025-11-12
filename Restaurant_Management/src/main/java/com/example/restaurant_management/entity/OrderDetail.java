package com.example.restaurant_management.entity;

public class OrderDetail {
    private int orderCtId;
    private int orderId;
    private int monId;
    private int soLuong;
    private double donGia;
    private double thanhTien;

    public OrderDetail(int orderCtId, int orderId, int monId, int soLuong, double donGia, double thanhTien) {
        this.orderCtId = orderCtId;
        this.orderId = orderId;
        this.monId = monId;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public int getOrderCtId() { return orderCtId; }
    public int getOrderId() { return orderId; }
    public int getMonId() { return monId; }
    public int getSoLuong() { return soLuong; }
    public double getDonGia() { return donGia; }
    public double getThanhTien() { return thanhTien; }
}
