package com.example.restaurant_management.service;

import com.example.restaurant_management.entity.Invoice;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.entity.OrderItem;
import com.example.restaurant_management.entity.Payment;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class OrderServiceDemo {

    public static void main(String[] args) {
        OrderService orderService = new OrderService();
        Order order = new Order(null, 1, 2, 1, LocalDateTime.now(), BigDecimal.ZERO, "MOI");
        List<OrderItem> items = List.of(
                new OrderItem(null, null, 1, 2, new BigDecimal("50000"), null),
                new OrderItem(null, null, 3, 1, new BigDecimal("15000"), null)
        );
        Payment payment = new Payment(null, null, null, "THE", "HOAN_TAT", LocalDateTime.now());
        Invoice invoice = new Invoice(null, null, null, null, new BigDecimal("7000"), BigDecimal.ZERO, null, LocalDateTime.now());

        try {
            Invoice savedInvoice = orderService.createOrderWithDetails(order, items, payment, invoice);
            orderService.getLatestInvoiceForOrder(order.getOrderId())
                    .ifPresent(latest -> System.out.println("Latest invoice number: " + latest.getInvoiceNumber()));
            System.out.println("Created invoice ID: " + savedInvoice.getInvoiceId());
        } catch (SQLException e) {
            System.err.println("Failed to create order transaction: " + e.getMessage());
        } finally {
            orderService.close();
        }
    }
}
