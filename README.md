# eProject2
link erd : https://www.mermaidchart.com/d/69cf8b4e-896c-4b1c-8464-e9f6f5d3dce8
Funtion:
# Restaurant Management System (Desktop + Web)

## 1. Giới thiệu

Hệ thống quản lý nhà hàng gồm:
- **Desktop App (JavaFX)**: Dành cho thu ngân/quản lý tại quầy.
- **Web Client (HTML/JS)**: Dành cho khách đặt món tại bàn và nhân viên phục vụ.
- **Backend Server (nhúng)**: Chạy kèm trong Desktop App, cung cấp REST API và WebSocket, đồng thời phục vụ file tĩnh cho Web Client.

## 2. Công nghệ sử dụng

- **Backend**: Java (JDK 24), JavaFX, Java-WebSocket, Gson, MySQL Connector.
- **Frontend**: HTML5, CSS3, Vanilla JavaScript.
- **Database**: MySQL.

## 3. Cách chạy
### 3.1. Chuẩn bị
1. Cài đặt JDK 24.
2. Cài đặt MySQL, tạo database:
   ```sql
   CREATE DATABASE restaurant_db CHARACTER SET utf8mb4;
