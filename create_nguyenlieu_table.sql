-- ===============================
-- Script tạo bảng nguyenlieu
-- Chạy script này trong MySQL để tạo bảng nguyên liệu
-- ===============================

USE `quanly_nhahang`;

-- Tạo bảng nguyenlieu nếu chưa tồn tại
CREATE TABLE IF NOT EXISTS `nguyenlieu` (
    `nl_id` int NOT NULL AUTO_INCREMENT,
    `ten_nguyen_lieu` varchar(100) NOT NULL,
    `so_luong` decimal(10,2) NOT NULL DEFAULT '0.00',
    `don_vi` enum('kg','g','cai') DEFAULT 'kg',
    `nha_cung_cap` varchar(100) DEFAULT NULL,
    `ngay_nhap` date DEFAULT (CURRENT_DATE),
    `ngay_het_han` date DEFAULT NULL,
    `trang_thai` enum('CON_HANG','HET_HANG','HET_HAN') DEFAULT 'CON_HANG',
    PRIMARY KEY (`nl_id`),
    CONSTRAINT `nguyenlieu_chk_1` CHECK ((`so_luong` >= 0))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Chèn dữ liệu mẫu (chỉ nếu bảng trống)
INSERT INTO `nguyenlieu` (`ten_nguyen_lieu`, `so_luong`, `don_vi`, `nha_cung_cap`, `ngay_nhap`, `ngay_het_han`, `trang_thai`) 
SELECT * FROM (
    SELECT 'Thịt bò' as `ten_nguyen_lieu`, 50.00 as `so_luong`, 'kg' as `don_vi`, 'Công ty Thực phẩm ABC' as `nha_cung_cap`, '2025-01-15' as `ngay_nhap`, '2025-02-15' as `ngay_het_han`, 'CON_HANG' as `trang_thai`
    UNION ALL
    SELECT 'Rau xà lách', 20.00, 'kg', 'Nông trại XYZ', '2025-01-20', '2025-01-25', 'CON_HANG'
    UNION ALL
    SELECT 'Bánh phở', 100.00, 'cai', 'Cơ sở sản xuất DEF', '2025-01-18', '2025-02-18', 'CON_HANG'
) AS tmp
WHERE NOT EXISTS (SELECT 1 FROM `nguyenlieu`);

SELECT 'Bảng nguyenlieu đã được tạo thành công!' AS Status;

