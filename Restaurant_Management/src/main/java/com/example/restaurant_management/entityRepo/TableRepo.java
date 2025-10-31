package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableRepo extends EntityRepo<Table> {
    public TableRepo( RowMapper<Table> mapper, String tableName) {
        super(mapper, tableName);
    }

    public List<Table> getTableByStatus(String status) {
        try{
            List<Table> tableList = new ArrayList<>();
            String sql = "SELECT * FROM " + this.getTableName() +"WHERE `trang_thai` = " + status;
            PreparedStatement stmt = this.getConn().prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()){
                tableList.add(this.getMapper().mapRow(rs));
            }
            return tableList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
