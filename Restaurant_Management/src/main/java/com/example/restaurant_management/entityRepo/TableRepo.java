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
        List<Table> tableList = new ArrayList<>();
        try {
            String sql = "SELECT * FROM ban WHERE trang_thai = ?";
            PreparedStatement stmt = this.getConn().prepareStatement(sql);
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tableList.add(this.getMapper().mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tableList;
    }

    // ✅ Cập nhật trạng thái bàn (TRONG hoặc DANG_PHUC_VU)
    public static void updateTableStatus(Connection conn, int banId, String trangThai) throws SQLException {
        String sql = "UPDATE ban SET trang_thai = ? WHERE ban_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (!trangThai.equals("TRONG") && !trangThai.equals("DANG_PHUC_VU") && !trangThai.equals("BAO_TRI")) {
                throw new IllegalArgumentException("Trạng thái bàn không hợp lệ: " + trangThai);
            }
            ps.setString(1, trangThai);
            ps.setInt(2, banId);
            ps.executeUpdate();
        }
    }
    public void updateStatusSimple(int banId, String trangThai) throws SQLException {
        updateTableStatus(getConn(), banId, trangThai);
    }

}
