package com.example.restaurant_management.entity;

import java.time.LocalDateTime;

public class HoaDon {
    private int hoaDonId;
    private Integer orderId;
    private Integer banId;
    private double tongTien;
    private String phuongThuc; // TIEN_MAT | CHUYEN_KHOAN (text)
    private LocalDateTime thoiGian;
    private Double khachTra;
    private Double tienThoi;

    public HoaDon(int hoaDonId, Integer orderId, Integer banId, double tongTien,
                  String phuongThuc, LocalDateTime thoiGian, Double khachTra, Double tienThoi) {
        this.hoaDonId = hoaDonId;
        this.orderId = orderId;
        this.banId = banId;
        this.tongTien = tongTien;
        this.phuongThuc = phuongThuc;
        this.thoiGian = thoiGian;
        this.khachTra = khachTra;
        this.tienThoi = tienThoi;
    }
    public int getHoaDonId() { return hoaDonId; }
    public Integer getOrderId() { return orderId; }
    public Integer getBanId() { return banId; }
    public double getTongTien() { return tongTien; }
    public String getPhuongThuc() { return phuongThuc; }
    public LocalDateTime getThoiGian() { return thoiGian; }
    public Double getKhachTra() { return khachTra; }
    public Double getTienThoi() { return tienThoi; }
}
