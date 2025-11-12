package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.OrderDetail;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetailMapper implements RowMapper<OrderDetail> {
    @Override
    public OrderDetail mapRow(ResultSet rs) throws SQLException {
        return new OrderDetail(
                rs.getInt("order_ct_id"),
                rs.getObject("order_id") != null ? rs.getInt("order_id") : null,
                rs.getObject("mon_id") != null ? rs.getInt("mon_id") : null,
                rs.getObject("so_luong") != null ? rs.getInt("so_luong") : null,
                rs.getDouble("don_gia"),
                rs.getDouble("thanh_tien")
        );
    }
}

