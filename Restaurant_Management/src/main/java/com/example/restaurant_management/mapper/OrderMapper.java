package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Order;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class OrderMapper implements RowMapper<Order> {
    @Override
    public Order mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("thoi_gian");
        return new Order(
                rs.getInt("order_id"),
                rs.getObject("kh_id") != null ? rs.getInt("kh_id") : null,
                rs.getObject("nv_id") != null ? rs.getInt("nv_id") : null,
                rs.getObject("ban_id") != null ? rs.getInt("ban_id") : null,
                timestamp != null ? timestamp.toLocalDateTime() : null,
                rs.getDouble("tong_tien"),
                rs.getString("trang_thai")
        );
    }
}

