package com.example.restaurant_management.entityRepo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OrderItemRepo {
    private final Connection conn;

    public OrderItemRepo(Connection conn) {
        this.conn = conn;
    }

    public Connection getConn() {
        return conn;
    }

    public void insertOrderItem(int orderId, int foodId, int quantity, BigDecimal unitPrice) throws SQLException {
        String sql = "INSERT INTO order_chitiet (order_id, mon_id, so_luong, don_gia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, foodId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.executeUpdate();
        }
    }
}
