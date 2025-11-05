package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Payment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PaymentMapper implements RowMapper<Payment> {
    @Override
    public Payment mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("thanh_toan_luc");
        LocalDateTime paidAt = timestamp == null ? null : timestamp.toLocalDateTime();
        return new Payment(
                (Integer) rs.getObject("payment_id"),
                (Integer) rs.getObject("order_id"),
                rs.getBigDecimal("so_tien"),
                rs.getString("phuong_thuc"),
                rs.getString("trang_thai"),
                paidAt
        );
    }
}
