package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Invoice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class InvoiceMapper implements RowMapper<Invoice> {
    @Override
    public Invoice mapRow(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("xuat_luc");
        LocalDateTime issuedAt = timestamp == null ? null : timestamp.toLocalDateTime();
        return new Invoice(
                (Integer) rs.getObject("invoice_id"),
                (Integer) rs.getObject("order_id"),
                rs.getString("so_hoa_don"),
                rs.getBigDecimal("tong_tien"),
                rs.getBigDecimal("thue"),
                rs.getBigDecimal("giam_gia"),
                rs.getBigDecimal("phai_thu"),
                issuedAt
        );
    }
}
