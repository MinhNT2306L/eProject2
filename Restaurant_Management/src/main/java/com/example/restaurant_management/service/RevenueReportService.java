package com.example.restaurant_management.service;

import com.example.restaurant_management.ConnectDB.ConnectDB;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RevenueReportService {
    private final Connection conn;

    public RevenueReportService() {
        this.conn = ConnectDB.getConnection();
    }

    // Tổng doanh thu theo khoảng thời gian
    public double getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(SUM(tong_tien), 0) as total " +
                     "FROM hoadon " +
                     "WHERE DATE(thoi_gian) BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Doanh thu theo ngày trong khoảng thời gian
    public List<Map<String, Object>> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT DATE(thoi_gian) as date, COALESCE(SUM(tong_tien), 0) as revenue, COUNT(*) as count " +
                     "FROM hoadon " +
                     "WHERE DATE(thoi_gian) BETWEEN ? AND ? " +
                     "GROUP BY DATE(thoi_gian) " +
                     "ORDER BY date ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("date", rs.getDate("date").toLocalDate());
                    data.put("revenue", rs.getDouble("revenue"));
                    data.put("count", rs.getInt("count"));
                    result.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Doanh thu theo tháng
    public List<Map<String, Object>> getMonthlyRevenue(int year) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT MONTH(thoi_gian) as month, COALESCE(SUM(tong_tien), 0) as revenue, COUNT(*) as count " +
                     "FROM hoadon " +
                     "WHERE YEAR(thoi_gian) = ? " +
                     "GROUP BY MONTH(thoi_gian) " +
                     "ORDER BY month ASC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, year);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("month", rs.getInt("month"));
                    data.put("revenue", rs.getDouble("revenue"));
                    data.put("count", rs.getInt("count"));
                    result.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Doanh thu theo phương thức thanh toán
    public List<Map<String, Object>> getRevenueByPaymentMethod(LocalDate startDate, LocalDate endDate) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT phuong_thuc, COALESCE(SUM(tong_tien), 0) as revenue, COUNT(*) as count " +
                     "FROM hoadon " +
                     "WHERE DATE(thoi_gian) BETWEEN ? AND ? " +
                     "GROUP BY phuong_thuc";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("method", rs.getString("phuong_thuc"));
                    data.put("revenue", rs.getDouble("revenue"));
                    data.put("count", rs.getInt("count"));
                    result.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Top món ăn bán chạy
    public List<Map<String, Object>> getTopSellingFoods(LocalDate startDate, LocalDate endDate, int limit) {
        List<Map<String, Object>> result = new ArrayList<>();
        String sql = "SELECT m.ten_mon, m.loai_mon, SUM(od.so_luong) as total_quantity, " +
                     "SUM(od.thanh_tien) as total_revenue " +
                     "FROM order_chitiet od " +
                     "JOIN orders o ON od.order_id = o.order_id " +
                     "JOIN monan m ON od.mon_id = m.mon_id " +
                     "WHERE DATE(o.thoi_gian) BETWEEN ? AND ? " +
                     "AND o.trang_thai = 'DA_THANH_TOAN' " +
                     "GROUP BY m.mon_id, m.ten_mon, m.loai_mon " +
                     "ORDER BY total_quantity DESC " +
                     "LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            ps.setInt(3, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("foodName", rs.getString("ten_mon"));
                    data.put("category", rs.getString("loai_mon"));
                    data.put("quantity", rs.getInt("total_quantity"));
                    data.put("revenue", rs.getDouble("total_revenue"));
                    result.add(data);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Số lượng đơn hàng theo trạng thái
    public Map<String, Integer> getOrderCountByStatus(LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> result = new HashMap<>();
        String sql = "SELECT trang_thai, COUNT(*) as count " +
                     "FROM orders " +
                     "WHERE DATE(thoi_gian) BETWEEN ? AND ? " +
                     "GROUP BY trang_thai";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.put(rs.getString("trang_thai"), rs.getInt("count"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    // Doanh thu trung bình mỗi đơn hàng
    public double getAverageOrderValue(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COALESCE(AVG(tong_tien), 0) as avg_value " +
                     "FROM hoadon " +
                     "WHERE DATE(thoi_gian) BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_value");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    // Tổng số hóa đơn
    public int getTotalInvoices(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(*) as count " +
                     "FROM hoadon " +
                     "WHERE DATE(thoi_gian) BETWEEN ? AND ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}

