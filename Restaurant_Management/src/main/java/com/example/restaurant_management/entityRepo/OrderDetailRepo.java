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

    public List<OrderDetail> getDetailsByInvoiceId(int invoiceId) {
        List<OrderDetail> details = new ArrayList<>();
        // Note: This query assumes 'hoadon' table has 'order_id' column.
        // If not, this will fail. Using user's requested logic.
        String sql = "SELECT od.*, m.ten_mon " +
                "FROM order_chitiet od " +
                "JOIN monan m ON od.mon_id = m.mon_id " +
                "WHERE od.order_id = (SELECT order_id FROM hoadon WHERE hoadon_id = ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, invoiceId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderDetail detail = mapper.mapRow(rs);
                    // Manually map ten_mon since mapper doesn't handle it
                    detail.setTenMon(rs.getString("ten_mon"));
                    details.add(detail);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching details for invoice " + invoiceId + ": " + e.getMessage());
            // If column order_id not found, we might want to throw or return empty
            throw new RuntimeException(e);
        }
        return details;
    }
}
