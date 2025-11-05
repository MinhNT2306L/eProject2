package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Invoice;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Optional;

public class InvoiceRepo extends EntityRepo<Invoice> {

    public InvoiceRepo(RowMapper<Invoice> mapper) {
        super(mapper, "invoices");
    }

    public InvoiceRepo(RowMapper<Invoice> mapper, Connection connection) {
        super(mapper, "invoices", connection);
    }

    public int createInvoice(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO invoices (order_id, so_hoa_don, tong_tien, thue, giam_gia, phai_thu, xuat_luc) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, invoice.getOrderId());
            ps.setString(2, invoice.getInvoiceNumber());
            ps.setBigDecimal(3, invoice.getTotalAmount());
            ps.setBigDecimal(4, invoice.getTax());
            ps.setBigDecimal(5, invoice.getDiscount());
            ps.setBigDecimal(6, invoice.getAmountDue());
            LocalDateTime issuedAt = invoice.getIssuedAt();
            ps.setTimestamp(7, issuedAt == null ? null : Timestamp.valueOf(issuedAt));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    invoice.setInvoiceId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to insert invoice");
    }

    public Optional<Invoice> findLatestInvoiceByOrder(int orderId) throws SQLException {
        String sql = "SELECT * FROM invoices WHERE order_id = ? ORDER BY xuat_luc DESC LIMIT 1";
        try (PreparedStatement ps = this.getConn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(getMapper().mapRow(rs));
                }
            }
        }
        return Optional.empty();
    }
}
