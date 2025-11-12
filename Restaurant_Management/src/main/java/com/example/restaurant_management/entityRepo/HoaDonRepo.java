package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.HoaDon;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.*;
import java.util.Optional;

public class HoaDonRepo extends EntityRepo<HoaDon> {
    public HoaDonRepo(RowMapper<HoaDon> mapper) { super(mapper, "hoadon"); }

    public int createFromOrder(Integer orderId, Integer banId, double tongTien,
                               String phuongThuc, Double khachTra, Double tienThoi) throws SQLException {
        String sql = "INSERT INTO hoadon(order_id, ban_id, tong_tien, phuong_thuc, thoi_gian, khach_tra, tien_thoi) " +
                     "VALUES(?,?,?,?,NOW(),?,?)";
        try (PreparedStatement ps = getConn().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (orderId == null) ps.setNull(1, Types.INTEGER); else ps.setInt(1, orderId);
            if (banId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, banId);
            ps.setDouble(3, tongTien);
            ps.setString(4, phuongThuc);
            if (khachTra == null) ps.setNull(5, Types.DECIMAL); else ps.setDouble(5, khachTra);
            if (tienThoi == null) ps.setNull(6, Types.DECIMAL); else ps.setDouble(6, tienThoi);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) { if (rs.next()) return rs.getInt(1); }
        }
        throw new SQLException("Cannot create hoadon");
    }

    public Optional<HoaDon> findLatestByTable(int banId) {
        String sql = "SELECT * FROM hoadon WHERE ban_id = ? ORDER BY hoadon_id DESC LIMIT 1";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, banId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(getMapper().mapRow(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }

    public Optional<HoaDon> findById(int hoaDonId) {
        String sql = "SELECT * FROM hoadon WHERE hoadon_id = ?";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, hoaDonId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(getMapper().mapRow(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return Optional.empty();
    }
}
