package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.entity.OrderDetail;
import com.example.restaurant_management.mapper.RowMapper;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailRepo extends EntityRepo<OrderDetail> {
    public OrderDetailRepo(RowMapper<OrderDetail> mapper) { super(mapper, "order_chitiet"); }

    public void addOrUpdateItem(int orderId, int monId, int deltaQty, double donGia) throws SQLException {
        String upsert = "INSERT INTO order_chitiet(order_id, mon_id, so_luong, don_gia) " +
                        "VALUES(?,?,?,?) " +
                        "ON DUPLICATE KEY UPDATE so_luong = so_luong + VALUES(so_luong), don_gia = VALUES(don_gia)";
        try (PreparedStatement ps = getConn().prepareStatement(upsert)) {
            ps.setInt(1, orderId);
            ps.setInt(2, monId);
            ps.setInt(3, deltaQty);
            ps.setDouble(4, donGia);
            ps.executeUpdate();
        }
    }
    public List<OrderDetail> findByOrder(int orderId) {
        List<OrderDetail> list = new ArrayList<>();
        String sql = "SELECT order_ct_id, order_id, mon_id, so_luong, don_gia, (so_luong*don_gia) AS thanh_tien " +
                     "FROM order_chitiet WHERE order_id=? ORDER BY order_ct_id";
        try (PreparedStatement ps = getConn().prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(getMapper().mapRow(rs));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    public void removeItem(int orderId, int monId) throws SQLException {
        try (PreparedStatement ps = getConn().prepareStatement("DELETE FROM order_chitiet WHERE order_id=? AND mon_id=?")) {
            ps.setInt(1, orderId);
            ps.setInt(2, monId);
            ps.executeUpdate();
        }
    }
}
