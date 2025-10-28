-- ===============================
-- Database: quanly_nhahang
-- ===============================
CREATE DATABASE IF NOT EXISTS `quanly_nhahang` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_0900_ai_ci;
USE `quanly_nhahang`;

-- ===============================
-- Bảng roles
-- ===============================
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `role_id` int NOT NULL AUTO_INCREMENT,
    `role_name` varchar(50) NOT NULL,
    PRIMARY KEY (`role_id`),
    UNIQUE KEY `role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dữ liệu roles: Manager và Nhân viên
INSERT INTO `roles` (`role_name`) VALUES
('Manager'),
('Nhân viên');

-- ===============================
-- Bảng nhanvien
-- ===============================
DROP TABLE IF EXISTS `nhanvien`;
CREATE TABLE `nhanvien` (
    `nv_id` int NOT NULL AUTO_INCREMENT,
    `username` varchar(50) NOT NULL,
    `password` varchar(255) NOT NULL,
    `full_name` varchar(100) DEFAULT NULL,
    `phone` varchar(20) DEFAULT NULL,
    `email` varchar(100) DEFAULT NULL,
    `role_id` int DEFAULT NULL,
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`nv_id`),
    UNIQUE KEY `username` (`username`),
    KEY `role_id` (`role_id`),
    CONSTRAINT `nhanvien_ibfk_1` FOREIGN KEY (`role_id`) REFERENCES `roles` (`role_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tài khoản manager và nhân viên
INSERT INTO `nhanvien` (`username`, `password`, `full_name`, `phone`, `email`, `role_id`) VALUES
('manager01', 'manager123', 'Nguyễn Văn M', '0909123456', 'manager@nhahang.com', 1),
('staff01', 'staff123', 'Trần Thị N', '0909234567', 'staff@nhahang.com', 2);

-- ===============================
-- Bảng khachhang
-- ===============================
DROP TABLE IF EXISTS `khachhang`;
CREATE TABLE `khachhang` (
    `kh_id` int NOT NULL AUTO_INCREMENT,
    `ten_kh` varchar(100) NOT NULL,
    `sdt` varchar(20) DEFAULT NULL,
    `email` varchar(100) DEFAULT NULL,
    `ngay_tao` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`kh_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dữ liệu khachhang mẫu
INSERT INTO `khachhang` (`ten_kh`, `sdt`, `email`) VALUES
('Phạm Thị D', '0912345678', 'phamthiD@gmail.com'),
('Ngô Văn E', '0923456789', 'ngovanE@gmail.com');

-- ===============================
-- Bảng ban
-- ===============================
DROP TABLE IF EXISTS `ban`;
CREATE TABLE `ban` (
    `ban_id` int NOT NULL AUTO_INCREMENT,
    `so_ban` int NOT NULL,
    `suc_chua` int DEFAULT NULL,
    `trang_thai` enum('TRONG','DAT_TRUOC','PHUC_VU') DEFAULT 'TRONG',
    PRIMARY KEY (`ban_id`),
    UNIQUE KEY `so_ban` (`so_ban`),
    CONSTRAINT `ban_chk_1` CHECK ((`suc_chua` > 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dữ liệu ban mẫu
INSERT INTO `ban` (`so_ban`, `suc_chua`, `trang_thai`) VALUES
(1, 4, 'TRONG'),
(2, 2, 'DAT_TRUOC'),
(3, 6, 'PHUC_VU');

-- ===============================
-- Bảng monan
-- ===============================
DROP TABLE IF EXISTS `monan`;
CREATE TABLE `monan` (
    `mon_id` int NOT NULL AUTO_INCREMENT,
    `ten_mon` varchar(100) NOT NULL,
    `loai_mon` varchar(50) DEFAULT NULL,
    `gia` decimal(10,2) NOT NULL,
    `mo_ta` text,
    `trang_thai` enum('CON_HANG','HET_HANG') DEFAULT 'CON_HANG',
    `created_at` datetime DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`mon_id`),
    CONSTRAINT `monan_chk_1` CHECK ((`gia` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dữ liệu monan mẫu
INSERT INTO `monan` (`ten_mon`, `loai_mon`, `gia`, `mo_ta`, `trang_thai`) VALUES
('Phở bò', 'Món chính', 50000, 'Phở bò truyền thống', 'CON_HANG'),
('Gỏi cuốn', 'Khai vị', 20000, 'Gỏi cuốn tôm thịt', 'CON_HANG'),
('Cà phê sữa', 'Đồ uống', 15000, 'Cà phê sữa đá', 'CON_HANG');

-- ===============================
-- Bảng datban
-- ===============================
DROP TABLE IF EXISTS `datban`;
CREATE TABLE `datban` (
    `datban_id` int NOT NULL AUTO_INCREMENT,
    `kh_id` int DEFAULT NULL,
    `ban_id` int DEFAULT NULL,
    `thoi_gian_dat` datetime NOT NULL,
    `ghi_chu` text,
    `trang_thai` enum('CHO_XAC_NHAN','DA_XAC_NHAN','DA_HUY') DEFAULT 'CHO_XAC_NHAN',
    PRIMARY KEY (`datban_id`),
    KEY `kh_id` (`kh_id`),
    KEY `ban_id` (`ban_id`),
    CONSTRAINT `datban_ibfk_1` FOREIGN KEY (`kh_id`) REFERENCES `khachhang` (`kh_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `datban_ibfk_2` FOREIGN KEY (`ban_id`) REFERENCES `ban` (`ban_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dữ liệu datban mẫu
INSERT INTO `datban` (`kh_id`, `ban_id`, `thoi_gian_dat`, `ghi_chu`, `trang_thai`) VALUES
(1, 2, '2025-10-28 18:00:00', 'Đặt bàn cho 2 người', 'CHO_XAC_NHAN'),
(2, 3, '2025-10-28 19:00:00', 'Bàn cạnh cửa sổ', 'DA_XAC_NHAN');

-- ===============================
-- Bảng orders
-- ===============================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
    `order_id` int NOT NULL AUTO_INCREMENT,
    `kh_id` int DEFAULT NULL,
    `nv_id` int DEFAULT NULL,
    `ban_id` int DEFAULT NULL,
    `thoi_gian` datetime DEFAULT CURRENT_TIMESTAMP,
    `tong_tien` decimal(12,2) DEFAULT '0.00',
    `trang_thai` enum('MOI','DANG_PHUC_VU','DA_THANH_TOAN','DA_HUY') DEFAULT 'MOI',
    PRIMARY KEY (`order_id`),
    KEY `kh_id` (`kh_id`),
    KEY `nv_id` (`nv_id`),
    KEY `ban_id` (`ban_id`),
    CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`kh_id`) REFERENCES `khachhang` (`kh_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`nv_id`) REFERENCES `nhanvien` (`nv_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `orders_ibfk_3` FOREIGN KEY (`ban_id`) REFERENCES `ban` (`ban_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dữ liệu orders mẫu
INSERT INTO `orders` (`kh_id`, `nv_id`, `ban_id`, `thoi_gian`, `tong_tien`, `trang_thai`) VALUES
(1, 2, 1, '2025-10-28 12:00:00', 70000, 'MOI'),
(2, 2, 3, '2025-10-28 12:30:00', 35000, 'DANG_PHUC_VU');

-- ===============================
-- Bảng order_chitiet
-- ===============================
DROP TABLE IF EXISTS `order_chitiet`;
CREATE TABLE `order_chitiet` (
    `order_ct_id` int NOT NULL AUTO_INCREMENT,
    `order_id` int DEFAULT NULL,
    `mon_id` int DEFAULT NULL,
    `so_luong` int DEFAULT NULL,
    `don_gia` decimal(10,2) DEFAULT NULL,
    `thanh_tien` decimal(12,2) GENERATED ALWAYS AS ((`so_luong` * `don_gia`)) STORED,
    PRIMARY KEY (`order_ct_id`),
    KEY `order_id` (`order_id`),
    KEY `mon_id` (`mon_id`),
    CONSTRAINT `order_chitiet_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`order_id`) 
        ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT `order_chitiet_ibfk_2` FOREIGN KEY (`mon_id`) REFERENCES `monan` (`mon_id`) 
        ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT `order_chitiet_chk_1` CHECK ((`so_luong` > 0)),
    CONSTRAINT `order_chitiet_chk_2` CHECK ((`don_gia` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Dữ liệu order_chitiet mẫu
INSERT INTO `order_chitiet` (`order_id`, `mon_id`, `so_luong`, `don_gia`) VALUES
(1, 1, 1, 50000),
(1, 3, 1, 20000),
(2, 2, 1, 20000),
(2, 3, 1, 15000);
