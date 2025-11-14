package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.Invoice;
import com.example.restaurant_management.mapper.InvoiceMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class InvoiceRepo {
    private final Connection conn;
    private final InvoiceMapper mapper;

    public InvoiceRepo() {
        try {
            this.conn = ConnectDB.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể kết nối đến database: " + e.getMessage(), e);
        }
        this.mapper = new InvoiceMapper();
    }

    public int createInvoice(Invoice invoice) throws SQLException {
        String sql = "INSERT INTO hoadon (ban_id, tong_tien, phuong_thuc, thoi_gian) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, invoice.getBanId());
            ps.setDouble(2, invoice.getTongTien());
            ps.setString(3, invoice.getPhuongThuc());
            ps.setTimestamp(4, invoice.getThoiGian() != null ? java.sql.Timestamp.valueOf(invoice.getThoiGian()) : java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating invoice failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating invoice failed, no ID obtained.");
                }
            }
        }
    }

    public Invoice findById(int hoadonId) {
        String sql = "SELECT * FROM hoadon WHERE hoadon_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, hoadonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapper.mapRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}

