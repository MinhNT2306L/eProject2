package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.HoaDon;
import java.sql.ResultSet;
import java.sql.SQLException;

public class HoaDonMapper implements RowMapper<HoaDon> {
    @Override
    public HoaDon mapRow(ResultSet rs) throws SQLException {
        return new HoaDon(
                rs.getInt("hoadon_id"),
                (Integer) rs.getObject("order_id"),
                (Integer) rs.getObject("ban_id"),
                rs.getDouble("tong_tien"),
                rs.getString("phuong_thuc"),
                rs.getTimestamp("thoi_gian").toLocalDateTime(),
                (Double) rs.getObject("khach_tra"),
                (Double) rs.getObject("tien_thoi")
        );
    }
}
