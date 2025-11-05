package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Payment;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class PaymentMapper implements RowMapper<Payment> {
    @Override
    public Payment mapRow(ResultSet rs) throws SQLException {
        Payment.PaymentMethod method = null;
        String methodValue = rs.getString("phuong_thuc");
        if (methodValue != null) {
            method = Payment.PaymentMethod.valueOf(methodValue);
        }

        Payment.PaymentStatus status = null;
        String statusValue = rs.getString("trang_thai");
        if (statusValue != null) {
            status = Payment.PaymentStatus.valueOf(statusValue);
        }

        Timestamp paidTimestamp = rs.getTimestamp("thanh_toan_luc");

        return new Payment(
                rs.getObject("payment_id", Integer.class),
                rs.getObject("order_id", Integer.class),
                rs.getBigDecimal("so_tien"),
                method,
                status,
                paidTimestamp != null ? paidTimestamp.toLocalDateTime() : null
        );
    }
}
