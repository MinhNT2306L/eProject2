package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Invoice;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

public class InvoiceMapper implements RowMapper<Invoice> {
    @Override
    public Invoice mapRow(ResultSet rs) throws SQLException {
        Timestamp issuedTimestamp = rs.getTimestamp("xuat_luc");

        return new Invoice(
                rs.getObject("invoice_id", Integer.class),
                rs.getObject("order_id", Integer.class),
                rs.getString("so_hoa_don"),
                rs.getBigDecimal("tong_tien"),
                rs.getBigDecimal("thue"),
                rs.getBigDecimal("giam_gia"),
                rs.getBigDecimal("phai_thu"),
                issuedTimestamp != null ? issuedTimestamp.toLocalDateTime() : null
        );
    }
}
