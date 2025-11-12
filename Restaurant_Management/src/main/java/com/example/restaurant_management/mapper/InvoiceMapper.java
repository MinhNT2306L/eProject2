package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Invoice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class InvoiceMapper implements RowMapper<Invoice> {
    @Override
    public Invoice mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("thoi_gian");
        return new Invoice(
                rs.getInt("hoadon_id"),
                rs.getObject("ban_id") != null ? rs.getInt("ban_id") : null,
                rs.getDouble("tong_tien"),
                rs.getString("phuong_thuc"),
                timestamp != null ? timestamp.toLocalDateTime() : null
        );
    }
}

