package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.OrderItem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderItemMapper implements RowMapper<OrderItem> {
    @Override
    public OrderItem mapRow(ResultSet rs) throws SQLException {
        return new OrderItem(
                (Integer) rs.getObject("order_ct_id"),
                (Integer) rs.getObject("order_id"),
                (Integer) rs.getObject("mon_id"),
                (Integer) rs.getObject("so_luong"),
                rs.getBigDecimal("don_gia"),
                rs.getBigDecimal("thanh_tien")
        );
    }
}
