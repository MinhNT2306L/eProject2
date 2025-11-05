package com.example.restaurant_management.entityRepo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class OrderRepo {
    private final Connection conn;

    public OrderRepo(Connection conn) {
        this.conn = conn;
    }

    public Connection getConn() {
        return conn;
    }

    public int createOrder(int tableId, BigDecimal totalAmount, String status) throws SQLException {
        String sql = "INSERT INTO orders (ban_id, tong_tien, trang_thai) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, tableId);
            ps.setBigDecimal(2, totalAmount);
            ps.setString(3, status);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                throw new SQLException("Creating order failed, no ID obtained.");
            }
        }
    }

    public void updateOrderTotalAndStatus(int orderId, BigDecimal totalAmount, String status) throws SQLException {
        String sql = "UPDATE orders SET tong_tien = ?, trang_thai = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBigDecimal(1, totalAmount);
            ps.setString(2, status);
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }
}
