package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.OrderDetail;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OrderDetailMapper implements RowMapper<OrderDetail> {
    @Override
    public OrderDetail mapRow(ResultSet rs) throws SQLException {
        return new OrderDetail(
                rs.getInt("order_ct_id"),
                rs.getInt("order_id"),
                rs.getInt("mon_id"),
                rs.getInt("so_luong"),
                rs.getDouble("don_gia"),
                rs.getDouble("thanh_tien")
        );
    }
}
