package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Payment;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class PaymentRepo extends EntityRepo<Payment> {

    public PaymentRepo(RowMapper<Payment> mapper) {
        super(mapper, "payments");
    }

    public PaymentRepo(RowMapper<Payment> mapper, Connection connection) {
        super(mapper, "payments", connection);
    }

    public int createPayment(Payment payment) throws SQLException {
        String sql = "INSERT INTO payments (order_id, so_tien, phuong_thuc, trang_thai, thanh_toan_luc) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, payment.getOrderId());
            ps.setBigDecimal(2, payment.getAmount());
            ps.setString(3, payment.getMethod());
            ps.setString(4, payment.getStatus());
            LocalDateTime paidAt = payment.getPaidAt();
            ps.setTimestamp(5, paidAt == null ? null : Timestamp.valueOf(paidAt));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    payment.setPaymentId(id);
                    return id;
                }
            }
        }
        throw new SQLException("Failed to insert payment");
    }
}
