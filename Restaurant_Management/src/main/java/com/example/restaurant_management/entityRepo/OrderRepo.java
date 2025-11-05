package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Order;
import com.example.restaurant_management.mapper.RowMapper;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class OrderRepo extends EntityRepo<Order> {

    public OrderRepo(RowMapper<Order> mapper) {
        super(mapper, "orders");
    }

    public OrderRepo(RowMapper<Order> mapper, Connection connection) {
        super(mapper, "orders", connection);
    }

    public int createOrder(Order order) throws SQLException {
        String sql = "INSERT INTO orders (kh_id, nv_id, ban_id, thoi_gian, tong_tien, trang_thai) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (order.getCustomerId() == null) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, order.getCustomerId());
            }
            if (order.getEmployeeId() == null) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, order.getEmployeeId());
            }
            if (order.getTableId() == null) {
                ps.setNull(3, java.sql.Types.INTEGER);
            } else {
                ps.setInt(3, order.getTableId());
            }
            LocalDateTime orderTime = order.getOrderTime();
            if (orderTime == null) {
                ps.setTimestamp(4, null);
            } else {
                ps.setTimestamp(4, Timestamp.valueOf(orderTime));
            }
            BigDecimal total = order.getTotalAmount();
            ps.setBigDecimal(5, total == null ? BigDecimal.ZERO : total);
            ps.setString(6, order.getStatus());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    order.setOrderId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to insert order");
    }

    public void updateOrderTotal(int orderId, BigDecimal totalAmount) throws SQLException {
        String sql = "UPDATE orders SET tong_tien = ?, trang_thai = ? WHERE order_id = ?";
        try (PreparedStatement ps = this.getConn().prepareStatement(sql)) {
            ps.setBigDecimal(1, totalAmount);
            ps.setString(2, "DA_THANH_TOAN");
            ps.setInt(3, orderId);
            ps.executeUpdate();
        }
    }
}
