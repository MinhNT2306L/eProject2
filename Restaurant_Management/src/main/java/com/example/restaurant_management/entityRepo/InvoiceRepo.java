package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.dto.Invoice;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;

public class InvoiceRepo {
    private final Connection conn;

    public InvoiceRepo(Connection conn) {
        this.conn = conn;
    }

    public Connection getConn() {
        return conn;
    }

    public Invoice createInvoice(int orderId, String invoiceNumber, BigDecimal subtotal,
                                 BigDecimal tax, BigDecimal discount, BigDecimal amountDue) throws SQLException {
        String sql = "INSERT INTO invoices (order_id, so_hoa_don, tong_tien, thue, giam_gia, phai_thu) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setString(2, invoiceNumber);
            ps.setBigDecimal(3, subtotal);
            ps.setBigDecimal(4, tax);
            ps.setBigDecimal(5, discount);
            ps.setBigDecimal(6, amountDue);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int invoiceId = rs.getInt(1);
                    LocalDateTime issuedAt = fetchIssuedAt(invoiceId);
                    return new Invoice(invoiceId, orderId, invoiceNumber, subtotal, tax, discount, amountDue, issuedAt);
                }
                throw new SQLException("Creating invoice failed, no ID obtained.");
            }
        }
    }

    public void linkPayment(int invoiceId, int paymentId) throws SQLException {
        String sql = "INSERT INTO invoice_payments (invoice_id, payment_id) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            ps.setInt(2, paymentId);
            ps.executeUpdate();
        }
    }

    private LocalDateTime fetchIssuedAt(int invoiceId) throws SQLException {
        String sql = "SELECT xuat_luc FROM invoices WHERE invoice_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getTimestamp("xuat_luc").toLocalDateTime();
                }
            }
        }
        return null;
    }
}
