package com.example.restaurant_management.mapper;

import com.example.restaurant_management.entity.Table;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TableMapper implements RowMapper<Table> {
    @Override
    public Table mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("ban_id");
        String tableNumber = "Bàn " + rs.getInt("so_ban"); // convert int -> String
        String status = rs.getString("trang_thai");
        return new Table(id, tableNumber, status);
    }
}
