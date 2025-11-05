package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class OrderMapper implements RowMapper<Order> {
    @Override
    public Order mapRow(ResultSet rs) throws SQLException {
        Timestamp orderTimestamp = rs.getTimestamp("thoi_gian");
        Order.OrderStatus status = null;
        String statusValue = rs.getString("trang_thai");
        if (statusValue != null) {
            status = Order.OrderStatus.valueOf(statusValue);
        }

        return new Order(
                rs.getObject("order_id", Integer.class),
                rs.getObject("kh_id", Integer.class),
                rs.getObject("nv_id", Integer.class),
                rs.getObject("ban_id", Integer.class),
                orderTimestamp != null ? orderTimestamp.toLocalDateTime() : null,
                rs.getBigDecimal("tong_tien"),
                status
        );
    }
}
