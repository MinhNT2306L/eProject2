package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.mapper.OrderMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OrderRepo {
    private final Connection conn;
    private final OrderMapper mapper;

    public OrderRepo() {
        this.conn = ConnectDB.getConnection();
        this.mapper = new OrderMapper();
    }

    public int createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (kh_id, nv_id, ban_id, thoi_gian, tong_tien, trang_thai) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setObject(1, order.getKhId());
            ps.setObject(2, order.getNvId());
            ps.setObject(3, order.getBanId());
            ps.setTimestamp(4, order.getThoiGian() != null ? java.sql.Timestamp.valueOf(order.getThoiGian()) : java.sql.Timestamp.valueOf(LocalDateTime.now()));
            ps.setDouble(5, order.getTongTien());
            ps.setString(6, order.getTrangThai());
            
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating order failed, no rows affected.");
            }
            
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                } else {
                    throw new SQLException("Creating order failed, no ID obtained.");
                }
            }
        }
    }

    public Order findById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
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

    public List<Order> findByTableId(int banId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM orders WHERE ban_id = ? AND trang_thai != 'DA_HUY' ORDER BY thoi_gian DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, banId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    orders.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return orders;
    }

    public Order findActiveOrderByTableId(int banId) {
        String sql = "SELECT * FROM orders WHERE ban_id = ? AND trang_thai IN ('MOI', 'DANG_PHUC_VU') ORDER BY thoi_gian DESC LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, banId);
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

    public void updateOrderStatus(int orderId, String status) throws SQLException {
        String sql = "UPDATE orders SET trang_thai = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }

    public void updateOrderTotal(int orderId, double tongTien) throws SQLException {
        String sql = "UPDATE orders SET tong_tien = ? WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, tongTien);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        }
    }
}

