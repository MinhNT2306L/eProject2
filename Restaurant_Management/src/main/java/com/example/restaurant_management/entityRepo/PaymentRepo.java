package com.example.restaurant_management.entityRepo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PaymentRepo {
    private final Connection conn;

    public PaymentRepo(Connection conn) {
        this.conn = conn;
    }

    public Connection getConn() {
        return conn;
    }

    public int createPayment(int orderId, BigDecimal amount, String method, String status) throws SQLException {
        String sql = "INSERT INTO payments (order_id, so_tien, phuong_thuc, trang_thai) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, orderId);
            ps.setBigDecimal(2, amount);
            ps.setString(3, method);
            ps.setString(4, status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Creating payment failed, no ID obtained.");
            }
        }
    }
}
