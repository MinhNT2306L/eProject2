package com.example.restaurant_management.dto;

import java.math.BigDecimal;
import java.util.Objects;

public class PaymentRequest {
    private final BigDecimal tax;
    private final BigDecimal discount;
    private final BigDecimal amountPaid;
    private final String paymentMethod;
    private final String invoiceNumber;

    public PaymentRequest(BigDecimal tax, BigDecimal discount, BigDecimal amountPaid, String paymentMethod, String invoiceNumber) {
        this.tax = tax == null ? BigDecimal.ZERO : tax;
        this.discount = discount == null ? BigDecimal.ZERO : discount;
        this.amountPaid = Objects.requireNonNull(amountPaid, "amountPaid");
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "paymentMethod");
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getTax() {
        return tax;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }
}
