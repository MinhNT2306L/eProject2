package com.example.restaurant_management.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    private Integer paymentId;
    private Integer orderId;
    private BigDecimal amount;
    private String method;
    private String status;
    public enum PaymentMethod {
        TIEN_MAT,
        THE,
        CHUYEN_KHOAN,
        VI_DIEN_TU
    }

    public enum PaymentStatus {
        CHO_XU_LY,
        HOAN_TAT,
        THAT_BAI
    }

    private Integer paymentId;
    private Integer orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private LocalDateTime paidAt;

    public Payment() {
    }

    public Payment(Integer paymentId, Integer orderId, BigDecimal amount, String method, String status,
                   LocalDateTime paidAt) {
    public Payment(Integer paymentId, Integer orderId, BigDecimal amount, PaymentMethod method,
                   PaymentStatus status, LocalDateTime paidAt) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.paidAt = paidAt;
    }

    public Integer getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(Integer paymentId) {
        this.paymentId = paymentId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}
