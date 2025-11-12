package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Order;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderMapper implements RowMapper<Order> {
    @Override
    public Order mapRow(ResultSet rs) throws SQLException {
        return new Order(
                rs.getInt("order_id"),
                (Integer) rs.getObject("kh_id"),
                (Integer) rs.getObject("nv_id"),
                (Integer) rs.getObject("ban_id"),
                rs.getTimestamp("thoi_gian").toLocalDateTime(),
                rs.getDouble("tong_tien"),
                rs.getString("trang_thai")
        );
    }
}
