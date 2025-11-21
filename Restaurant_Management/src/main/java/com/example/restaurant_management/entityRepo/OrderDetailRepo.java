package com.example.restaurant_management.entityRepo;

import com.example.restaurant_management.ConnectDB.ConnectDB;
import com.example.restaurant_management.entity.OrderDetail;
import com.example.restaurant_management.mapper.OrderDetailMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDetailRepo {
    private final Connection conn;
    private final OrderDetailMapper mapper;

    public OrderDetailRepo() {
        this.conn = ConnectDB.getConnection();
        this.mapper = new OrderDetailMapper();
    }

    public void createOrderDetail(OrderDetail orderDetail) throws SQLException {
        String sql = "INSERT INTO order_chitiet (order_id, mon_id, so_luong, don_gia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, orderDetail.getOrderId());
            ps.setObject(2, orderDetail.getMonId());
            ps.setInt(3, orderDetail.getSoLuong());
            ps.setDouble(4, orderDetail.getDonGia());
            ps.executeUpdate();
        }
    }

    public List<OrderDetail> findByOrderId(int orderId) {
        List<OrderDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM order_chitiet WHERE order_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    details.add(mapper.mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return details;
    }

    public void updateOrderDetail(OrderDetail orderDetail) throws SQLException {
        // Update quantity - thanh_tien is auto-calculated by database
        String sql = "UPDATE order_chitiet SET so_luong = ? WHERE order_ct_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderDetail.getSoLuong());
            ps.setInt(2, orderDetail.getOrderCtId());
            ps.executeUpdate();
        }
    }
}

