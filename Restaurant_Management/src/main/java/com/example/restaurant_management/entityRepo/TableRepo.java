package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.Table;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableRepo extends EntityRepo<Table> {

    public TableRepo(RowMapper<Table> mapper) {
        super(mapper, "ban");
    }

    public List<Table> getTableByStatus(String status) {
        try {
            List<Table> tableList = new ArrayList<>();
            String sql = "SELECT * FROM " + this.getTableName() + " WHERE trang_thai = ?";
            PreparedStatement stmt = this.getConn().prepareStatement(sql);
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableList.add(this.getMapper().mapRow(rs));
            }
            return tableList;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    // Trong TableRepo (BanRepo)
    public void updateTableStatus(Connection conn, int banId, String trangThai) throws SQLException {
        // Lưu ý: Trang thái bàn phải khớp với ENUM trong DB: 'TRONG','DAT_TRUOC','PHUC_VU'
        String sql = "UPDATE ban SET trang_thai = ? WHERE ban_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setInt(2, banId);
            ps.executeUpdate();
        }
    }

    public boolean deleteByID(int tableId) {
        String sql = "DELETE FROM ban WHERE ban_id = ?";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, tableId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean insert(Table table) {
        String sql = "INSERT INTO ban (so_ban, trang_thai) VALUES (?, ?)";
        try (Connection conn = ConnectDB.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, table.getTableNumber());
            stmt.setString(2, table.getStatus());
            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
