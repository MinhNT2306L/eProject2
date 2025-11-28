package com.example.restaurant_management.entity;

public class OrderDetail {
    private int orderCtId;
    private Integer orderId;
    private Integer monId;
    private Integer soLuong;
    private double donGia;
    private double thanhTien;
    private String trangThai;

    public OrderDetail() {
    }

    public OrderDetail(int orderCtId, Integer orderId, Integer monId, Integer soLuong, double donGia,
            double thanhTien) {
        this.orderCtId = orderCtId;
        this.orderId = orderId;
        this.monId = monId;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
    }

    public OrderDetail(int orderCtId, Integer orderId, Integer monId, Integer soLuong, double donGia, double thanhTien,
            String trangThai) {
        this.orderCtId = orderCtId;
        this.orderId = orderId;
        this.monId = monId;
        this.soLuong = soLuong;
        this.donGia = donGia;
        this.thanhTien = thanhTien;
        this.trangThai = trangThai;
    }

    public int getOrderCtId() {
        return orderCtId;
    }

    public void setOrderCtId(int orderCtId) {
        this.orderCtId = orderCtId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getMonId() {
        return monId;
    }

    public void setMonId(Integer monId) {
        this.monId = monId;
    }

    public Integer getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(Integer soLuong) {
        this.soLuong = soLuong;
    }

    public double getDonGia() {
        return donGia;
    }

    public void setDonGia(double donGia) {
        this.donGia = donGia;
    }

    public double getThanhTien() {
        return thanhTien;
    }

    public void setThanhTien(double thanhTien) {
        this.thanhTien = thanhTien;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    private String tenMon;

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }
}
