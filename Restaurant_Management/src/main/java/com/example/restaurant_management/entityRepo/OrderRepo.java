package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.*;
import java.util.Optional;

public class OrderRepo extends EntityRepo<Order> {
    public OrderRepo(RowMapper<Order> mapper) { super(mapper, "orders"); }

    public Optional<Order> findOpenOrderByTable(int banId) {
        String sql = "SELECT * FROM orders WHERE ban_id = ? AND trang_thai IN ('MOI','DANG_PHUC_VU') ORDER BY order_id DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, banId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(getMapper().mapRow(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    public int createOrder(int banId, Integer nvId) throws SQLException {
        String sql = "INSERT INTO orders(ban_id, nv_id, trang_thai) VALUES (?, ?, 'MOI')";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, banId);
            if (nvId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, nvId);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        throw new SQLException("Cannot create order");
    }

    public void updateStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET trang_thai = ? WHERE order_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public void updateTotal(int orderId) throws SQLException {
        String sql = "UPDATE orders o " +
                     "JOIN (SELECT order_id, COALESCE(SUM(so_luong * don_gia), 0) AS total " +
                     "      FROM order_chitiet WHERE order_id = ?) t " +
                     "ON o.order_id = t.order_id " +
                     "SET o.tong_tien = t.total " +
                     "WHERE o.order_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }
}
