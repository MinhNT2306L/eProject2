package com.example.restaurant_management.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    private Integer invoiceId;
    private Integer orderId;
    private String invoiceNumber;
    private BigDecimal totalAmount;

    private BigDecimal amountDue;
    private LocalDateTime issuedAt;

    public Invoice() {
    }


        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.totalAmount = totalAmount;
        this.tax = tax;
        this.discount = discount;
        this.amountDue = amountDue;
        this.issuedAt = issuedAt;
    }

    public Integer getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }


    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public void setAmountDue(BigDecimal amountDue) {
        this.amountDue = amountDue;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }
}
