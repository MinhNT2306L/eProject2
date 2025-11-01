package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Table;

import java.sql.ResultSet;
import java.sql.SQLException;

public class TableMapper implements RowMapper<Table> {
    @Override
    public Table mapRow(ResultSet rs) throws SQLException {
        return new Table(
                rs.getInt("ban_id"),
                rs.getInt("so_ban"),
                rs.getString("trang_thai")
        );
    }
}
