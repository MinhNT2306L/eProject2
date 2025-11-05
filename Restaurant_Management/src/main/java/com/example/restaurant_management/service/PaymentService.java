package com.example.restaurant_management.service;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.dto.Invoice;
import com.example.restaurant_management.dto.PaymentRequest;
import com.example.restaurant_management.entity.Food;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.entityRepo.InvoiceRepo;
import com.example.restaurant_management.entityRepo.OrderItemRepo;
import com.example.restaurant_management.entityRepo.OrderRepo;
import com.example.restaurant_management.entityRepo.PaymentRepo;
import com.example.restaurant_management.entityRepo.TableRepo;
import com.example.restaurant_management.mapper.TableMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

public class PaymentService {
    private static final String ORDER_STATUS_NEW = "MOI";
    private static final String ORDER_STATUS_PAID = "DA_THANH_TOAN";
    private static final String TABLE_STATUS_FREE = "TRONG";
    private static final String PAYMENT_STATUS_COMPLETED = "HOAN_TAT";

    private final OrderRepo orderRepo;
    private final OrderItemRepo orderItemRepo;
    private final PaymentRepo paymentRepo;
    private final InvoiceRepo invoiceRepo;
    private final TableRepo tableRepo;
    private final Connection connection;

    public PaymentService(OrderRepo orderRepo,
                          OrderItemRepo orderItemRepo,
                          PaymentRepo paymentRepo,
                          InvoiceRepo invoiceRepo,
                          TableRepo tableRepo) {
        this.orderRepo = Objects.requireNonNull(orderRepo, "orderRepo");
        this.orderItemRepo = Objects.requireNonNull(orderItemRepo, "orderItemRepo");
        this.paymentRepo = Objects.requireNonNull(paymentRepo, "paymentRepo");
        this.invoiceRepo = Objects.requireNonNull(invoiceRepo, "invoiceRepo");
        this.tableRepo = Objects.requireNonNull(tableRepo, "tableRepo");
        this.connection = Objects.requireNonNull(orderRepo.getConn(), "connection");
        ensureSharedConnection(this.connection, orderItemRepo.getConn(), "orderItemRepo");
        ensureSharedConnection(this.connection, paymentRepo.getConn(), "paymentRepo");
        ensureSharedConnection(this.connection, invoiceRepo.getConn(), "invoiceRepo");
        ensureSharedConnection(this.connection, tableRepo.getConn(), "tableRepo");
    }

    public static PaymentService createDefault() {
        Connection conn = ConnectDB.getConnection();
        return new PaymentService(
                new OrderRepo(conn),
                new OrderItemRepo(conn),
                new PaymentRepo(conn),
                new InvoiceRepo(conn),
                new TableRepo(conn, new TableMapper())
        );
    }

    public Invoice checkout(Table table, Map<Food, Integer> items, PaymentRequest info) throws SQLException {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(info, "info");

        if (items.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty");
        }

        boolean initialAutoCommit = connection.getAutoCommit();
        try {
            connection.setAutoCommit(false);

            BigDecimal subtotal = BigDecimal.ZERO;
            int orderId = orderRepo.createOrder(table.getTableId(), BigDecimal.ZERO, ORDER_STATUS_NEW);

            for (Map.Entry<Food, Integer> entry : items.entrySet()) {
                Food food = entry.getKey();
                Integer quantity = entry.getValue();
                if (food == null || quantity == null || quantity <= 0) {
                    throw new IllegalArgumentException("Invalid order item");
                }
                BigDecimal unitPrice = BigDecimal.valueOf(food.getPrice());
                BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
                subtotal = subtotal.add(lineTotal);
                orderItemRepo.insertOrderItem(orderId, food.getFoodId(), quantity, unitPrice);
            }

            BigDecimal tax = info.getTax();
            BigDecimal discount = info.getDiscount();
            BigDecimal amountDue = subtotal.add(tax).subtract(discount);
            if (amountDue.compareTo(BigDecimal.ZERO) < 0) {
                amountDue = BigDecimal.ZERO;
            }

            if (info.getAmountPaid().compareTo(amountDue) < 0) {
                throw new IllegalArgumentException("Amount paid is less than the amount due");
            }

            orderRepo.updateOrderTotalAndStatus(orderId, subtotal, ORDER_STATUS_PAID);

            String invoiceNumber = info.getInvoiceNumber();
            if (invoiceNumber == null || invoiceNumber.isBlank()) {
                invoiceNumber = generateInvoiceNumber(orderId);
            }

            Invoice invoice = invoiceRepo.createInvoice(orderId, invoiceNumber, subtotal, tax, discount, amountDue);

            int paymentId = paymentRepo.createPayment(orderId, info.getAmountPaid(), info.getPaymentMethod(), PAYMENT_STATUS_COMPLETED);
            invoiceRepo.linkPayment(invoice.getInvoiceId(), paymentId);

            tableRepo.updateTableStatus(table.getTableId(), TABLE_STATUS_FREE);

            connection.commit();
            return invoice;
        } catch (SQLException | RuntimeException ex) {
            connection.rollback();
            throw ex;
        } finally {
            connection.setAutoCommit(initialAutoCommit);
        }
    }

    private String generateInvoiceNumber(int orderId) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return "INV-" + timestamp + "-" + orderId;
    }

    private void ensureSharedConnection(Connection expected, Connection actual, String repoName) {
        if (actual == null || actual != expected) {
            throw new IllegalArgumentException(repoName + " does not share the same database connection");
        }
    }
}
