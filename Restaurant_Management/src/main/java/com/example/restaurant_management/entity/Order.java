package com.example.restaurant_management.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Order {
    public enum OrderStatus {
        MOI,
        DANG_PHUC_VU,
        DA_THANH_TOAN,
        DA_HUY
    }

    private Integer orderId;
    private Integer customerId;
    private Integer employeeId;
    private Integer tableId;
    private LocalDateTime orderTime;
    private BigDecimal totalAmount;
    private OrderStatus status;

    public Order() {
    }

    public Order(Integer orderId, Integer customerId, Integer employeeId, Integer tableId,
                 LocalDateTime orderTime, BigDecimal totalAmount, OrderStatus status) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.employeeId = employeeId;
        this.tableId = tableId;
        this.orderTime = orderTime;
        this.totalAmount = totalAmount;
        this.status = status;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    public Integer getTableId() {
        return tableId;
    }

    public void setTableId(Integer tableId) {
        this.tableId = tableId;
    }

    public LocalDateTime getOrderTime() {
        return orderTime;
    }

    public void setOrderTime(LocalDateTime orderTime) {
        this.orderTime = orderTime;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
