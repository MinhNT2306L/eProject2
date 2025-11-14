package com.example.restaurant_management.ConnectDB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Lớp quản lý kết nối đến MySQL Database
 * 
 * LƯU Ý: Nếu gặp lỗi "Access denied", vui lòng kiểm tra:
 * 1. Mật khẩu MySQL của bạn có đúng không? (Hiện tại: "12345678")
 * 2. MySQL đã được khởi động chưa?
 * 3. Database 'quanly_nhahang' đã được tạo chưa?
 * 
 * Để thay đổi mật khẩu, sửa giá trị PASSWORD bên dưới
 */
public class ConnectDB {

    // ============================================
    // CẤU HÌNH KẾT NỐI DATABASE
    // ============================================
    // Nếu gặp lỗi "Access denied", hãy thay đổi PASSWORD thành mật khẩu MySQL của bạn
    private static final String URL = "jdbc:mysql://localhost:3306/quanly_nhahang";
    private static final String USER = "root";
    private static final String PASSWORD = "1234"; // ← THAY ĐỔI MẬT KHẨU Ở ĐÂY
    // ============================================

    private static Connection connection;

    private ConnectDB() {}

    public static Connection getConnection() throws SQLException {
        try {
            // Kiểm tra driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");
            
            Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
            return conn;
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC Driver không tìm thấy! Vui lòng kiểm tra dependency trong pom.xml", e);
        } catch (SQLException e) {
            // Xử lý lỗi cụ thể hơn
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("Access denied")) {
                throw new SQLException(
                    "LỖI XÁC THỰC: Mật khẩu MySQL không đúng!\n" +
                    "Vui lòng kiểm tra:\n" +
                    "  1. Mật khẩu hiện tại trong code: '" + PASSWORD + "'\n" +
                    "  2. Mật khẩu MySQL thực tế của bạn\n" +
                    "  3. Sửa PASSWORD trong file ConnectDB.java (dòng 21)\n" +
                    "Hoặc reset mật khẩu MySQL bằng lệnh: ALTER USER 'root'@'localhost' IDENTIFIED BY 'mật_khẩu_mới';",
                    e
                );
            } else if (errorMsg != null && errorMsg.contains("Unknown database")) {
                throw new SQLException(
                    "Database 'quanly_nhahang' chưa được tạo!\n" +
                    "Vui lòng tạo database bằng lệnh: CREATE DATABASE quanly_nhahang;",
                    e
                );
            } else if (errorMsg != null && errorMsg.contains("Communications link failure")) {
                throw new SQLException(
                    "Không thể kết nối đến MySQL Server!\n" +
                    "Vui lòng kiểm tra:\n" +
                    "  1. MySQL đã được khởi động chưa?\n" +
                    "  2. Port 3306 có đúng không?\n" +
                    "  3. Firewall có chặn kết nối không?",
                    e
                );
            }
            throw e;
        }
    }
}
