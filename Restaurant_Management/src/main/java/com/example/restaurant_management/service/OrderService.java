package com.example.restaurant_management.service;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.Invoice;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.entity.OrderItem;
import com.example.restaurant_management.entity.Payment;
import com.example.restaurant_management.entityRepo.InvoiceRepo;
import com.example.restaurant_management.entityRepo.OrderItemRepo;
import com.example.restaurant_management.entityRepo.OrderRepo;
import com.example.restaurant_management.entityRepo.PaymentRepo;
import com.example.restaurant_management.mapper.InvoiceMapper;
import com.example.restaurant_management.mapper.OrderItemMapper;
import com.example.restaurant_management.mapper.OrderMapper;
import com.example.restaurant_management.mapper.PaymentMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class OrderService {

    private final Connection connection;
    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;
    private final InvoiceRepo invoiceRepo;

    public OrderService() {
        this(ConnectDB.getConnection());
    }

    public OrderService(Connection connection) {
        this.connection = connection;
        this.orderRepo = new OrderRepo(new OrderMapper(), connection);
        this.orderItemRepo = new OrderItemRepo(new OrderItemMapper(), connection);
        this.paymentRepo = new PaymentRepo(new PaymentMapper(), connection);
        this.invoiceRepo = new InvoiceRepo(new InvoiceMapper(), connection);
    }

    public Invoice createOrderWithDetails(Order order, List<OrderItem> items, Payment payment, Invoice invoice) throws SQLException {
        Objects.requireNonNull(order, "order must not be null");
        Objects.requireNonNull(payment, "payment must not be null");
        Objects.requireNonNull(invoice, "invoice must not be null");
        List<OrderItem> itemList = items == null ? Collections.emptyList() : items;

        boolean originalAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            if (order.getOrderTime() == null) {
                order.setOrderTime(LocalDateTime.now());
            }
            if (order.getStatus() == null || order.getStatus().isBlank()) {
                order.setStatus("MOI");
            }
            int orderId = orderRepo.createOrder(order);

            BigDecimal total = BigDecimal.ZERO;
            if (!itemList.isEmpty()) {
                for (OrderItem item : itemList) {
                    item.setOrderId(orderId);
                    BigDecimal unitPrice = item.getUnitPrice() == null ? BigDecimal.ZERO : item.getUnitPrice();
                    int quantity = item.getQuantity() == null ? 0 : item.getQuantity();
                    BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                    if (item.getTotalPrice() == null) {
                        item.setTotalPrice(lineTotal);
                    }
                    total = total.add(lineTotal);
                }
                orderItemRepo.insertItemsBatch(itemList);
            }

            orderRepo.updateOrderTotal(orderId, total);

            payment.setOrderId(orderId);
            if (payment.getAmount() == null) {
                payment.setAmount(total);
            }
            if (payment.getMethod() == null || payment.getMethod().isBlank()) {
                payment.setMethod("TIEN_MAT");
            }
            if (payment.getStatus() == null || payment.getStatus().isBlank()) {
                payment.setStatus("HOAN_TAT");
            }
            if (payment.getPaidAt() == null) {
                payment.setPaidAt(LocalDateTime.now());
            }
            paymentRepo.createPayment(payment);

            invoice.setOrderId(orderId);
            if (invoice.getInvoiceNumber() == null || invoice.getInvoiceNumber().isBlank()) {
                invoice.setInvoiceNumber("INV-" + UUID.randomUUID());
            }
            if (invoice.getTotalAmount() == null) {
                invoice.setTotalAmount(total);
            }
            if (invoice.getTax() == null) {
                invoice.setTax(BigDecimal.ZERO);
            }
            if (invoice.getDiscount() == null) {
                invoice.setDiscount(BigDecimal.ZERO);
            }
            if (invoice.getAmountDue() == null) {
                invoice.setAmountDue(invoice.getTotalAmount().add(invoice.getTax()).subtract(invoice.getDiscount()));
            }
            if (invoice.getIssuedAt() == null) {
                invoice.setIssuedAt(LocalDateTime.now());
            }
            invoiceRepo.createInvoice(invoice);

            connection.commit();
            return invoice;
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(originalAutoCommit);
        }
    }

    public Optional<Invoice> getLatestInvoiceForOrder(int orderId) throws SQLException {
        return invoiceRepo.findLatestInvoiceByOrder(orderId);
    }

    public void close() {
        try {
            connection.close();
        } catch (SQLException ignored) {
        }
    }
}
