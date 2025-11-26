package com.example.restaurant_management.ConnectDB;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class TestConnection {
    
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("KIỂM TRA KẾT NỐI MYSQL");
        System.out.println("========================================\n");
        
        Connection connection = null;
        
        try {
            // Thử kết nối
            System.out.println("Đang thử kết nối đến database...");
            connection = ConnectDB.getConnection();
            
            if (connection != null && !connection.isClosed()) {
                System.out.println("✓ KẾT NỐI THÀNH CÔNG!\n");
                
                // Lấy thông tin database
                DatabaseMetaData metaData = connection.getMetaData();
                System.out.println("Thông tin kết nối:");
                System.out.println("  - Database: " + metaData.getDatabaseProductName());
                System.out.println("  - Version: " + metaData.getDatabaseProductVersion());
                System.out.println("  - Driver: " + metaData.getDriverName());
                System.out.println("  - URL: " + metaData.getURL());
                System.out.println("  - Username: " + metaData.getUserName());
                System.out.println();
                
                // Kiểm tra các bảng trong database
                System.out.println("Đang kiểm tra các bảng trong database...");
                ResultSet tables = metaData.getTables(null, null, "%", new String[]{"TABLE"});
                int tableCount = 0;
                System.out.println("Các bảng tìm thấy:");
                while (tables.next()) {
                    tableCount++;
                    String tableName = tables.getString("TABLE_NAME");
                    System.out.println("  " + tableCount + ". " + tableName);
                }
                System.out.println("Tổng số bảng: " + tableCount);
                System.out.println();
                
                // Test query đơn giản
                System.out.println("Đang test query...");
                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as total FROM ban");
                if (rs.next()) {
                    int total = rs.getInt("total");
                    System.out.println("✓ Query thành công! Số lượng bàn trong database: " + total);
                }
                rs.close();
                stmt.close();
                
                System.out.println("\n========================================");
                System.out.println("KẾT NỐI HOẠT ĐỘNG BÌNH THƯỜNG!");
                System.out.println("========================================");
                
            } else {
                System.out.println("✗ KẾT NỐI THẤT BẠI: Connection is null or closed");
            }
            
        } catch (SQLException e) {
            System.out.println("✗ LỖI KẾT NỐI!");
            System.out.println("========================================");
            System.out.println(e.getMessage());
            System.out.println("========================================\n");
            
            // Hiển thị thông tin chi tiết
            System.out.println("Chi tiết kỹ thuật:");
            System.out.println("  - SQL State: " + e.getSQLState());
            System.out.println("  - Error Code: " + e.getErrorCode());
            System.out.println();
            
            // Hướng dẫn cụ thể cho lỗi Access denied
            if (e.getMessage() != null && e.getMessage().contains("Access denied")) {
                System.out.println("════════════════════════════════════════");
                System.out.println("HƯỚNG DẪN SỬA LỖI 'ACCESS DENIED':");
                System.out.println("════════════════════════════════════════");
                System.out.println();
                System.out.println("CÁCH 1: Thay đổi mật khẩu trong code");
                System.out.println("  1. Mở file: ConnectDB.java");
                System.out.println("  2. Tìm dòng: private static final String PASSWORD = \"12345678\";");
                System.out.println("  3. Thay \"12345678\" bằng mật khẩu MySQL của bạn");
                System.out.println("  4. Lưu file và chạy lại");
                System.out.println();
                System.out.println("CÁCH 2: Reset mật khẩu MySQL");
                System.out.println("  Mở MySQL Command Line hoặc MySQL Workbench và chạy:");
                System.out.println("  ALTER USER 'root'@'localhost' IDENTIFIED BY '12345678';");
                System.out.println("  FLUSH PRIVILEGES;");
                System.out.println();
                System.out.println("CÁCH 3: Kiểm tra mật khẩu MySQL hiện tại");
                System.out.println("  - Mở MySQL Workbench");
                System.out.println("  - Thử kết nối với các mật khẩu khác nhau");
                System.out.println("  - Hoặc kiểm tra file cấu hình MySQL");
                System.out.println();
            }
            
            System.out.println("Các nguyên nhân khác có thể:");
            System.out.println("  1. MySQL chưa được khởi động");
            System.out.println("  2. Database 'quanly_nhahang' chưa được tạo");
            System.out.println("  3. Port 3306 không đúng hoặc bị chặn");
            System.out.println("  4. MySQL Connector chưa được tải về");
            System.out.println();
            
            // In stack trace để debug
            System.out.println("Stack trace (để debug):");
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("✗ LỖI KHÔNG XÁC ĐỊNH!");
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Đóng kết nối
            if (connection != null) {
                try {
                    connection.close();
                    System.out.println("\nĐã đóng kết nối.");
                } catch (SQLException e) {
                    System.out.println("Lỗi khi đóng kết nối: " + e.getMessage());
                }
            }
        }
    }
}


