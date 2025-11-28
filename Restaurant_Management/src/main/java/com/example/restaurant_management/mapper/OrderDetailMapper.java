package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.OrderDetail;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetailMapper implements RowMapper<OrderDetail> {
    @Override
    public OrderDetail mapRow(ResultSet rs) throws SQLException {
        OrderDetail detail = new OrderDetail(
                rs.getInt("order_ct_id"),
                rs.getObject("order_id") != null ? rs.getInt("order_id") : null,
                rs.getObject("mon_id") != null ? rs.getInt("mon_id") : null,
                rs.getObject("so_luong") != null ? rs.getInt("so_luong") : null,
                rs.getDouble("don_gia"),
                rs.getDouble("thanh_tien"));
        // trang_thai column doesn't exist in order_chitiet table, set to null
        try {
            detail.setTrangThai(rs.getString("trang_thai"));
        } catch (SQLException e) {
            // Column might not exist in some queries, ignore or set to null
            detail.setTrangThai(null);
        }
        return detail;
    }
}
