package com.example.restaurant_management.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Invoice {
    private final int invoiceId;
    private final int orderId;
    private final String invoiceNumber;
    private final BigDecimal subtotal;
    private final BigDecimal tax;
    private final BigDecimal discount;
    private final BigDecimal amountDue;
    private final LocalDateTime issuedAt;

    public Invoice(int invoiceId, int orderId, String invoiceNumber, BigDecimal subtotal,
                   BigDecimal tax, BigDecimal discount, BigDecimal amountDue, LocalDateTime issuedAt) {
        this.invoiceId = invoiceId;
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.subtotal = subtotal;
        this.tax = tax;
        this.discount = discount;
        this.amountDue = amountDue;
        this.issuedAt = issuedAt;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getAmountDue() {
        return amountDue;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}
