package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.OrderItem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderItemMapper implements RowMapper<OrderItem> {
    @Override
    public OrderItem mapRow(ResultSet rs) throws SQLException {
        return new OrderItem(
                rs.getObject("order_ct_id", Integer.class),
                rs.getObject("order_id", Integer.class),
                rs.getObject("mon_id", Integer.class),
                rs.getObject("so_luong", Integer.class),
                rs.getBigDecimal("don_gia"),
                rs.getBigDecimal("thanh_tien")
        );
    }
}
