-- =====================================
-- DỮ LIỆU MẪU CHO HOSPITAL QUEUE SYSTEM
-- =====================================

-- Sử dụng database hospital_queue
USE hospital_queue;

-- =====================================
-- 1. DỮ LIỆU KHOA (DEPARTMENTS)
-- =====================================

-- Xóa dữ liệu cũ nếu có
DELETE FROM notifications;
DELETE FROM appointments;
DELETE FROM tickets;
DELETE FROM departments;
ALTER TABLE notifications AUTO_INCREMENT = 1;
ALTER TABLE appointments AUTO_INCREMENT = 1;
ALTER TABLE tickets AUTO_INCREMENT = 1;
ALTER TABLE departments AUTO_INCREMENT = 1;

-- Thêm các khoa
INSERT INTO departments (code, name, location) VALUES
('K01', 'Khoa Nội Tổng Hợp', 'Tầng 2 - Khu A'),
('K02', 'Khoa Ngoại Tổng Hợp', 'Tầng 3 - Khu A'), 
('K03', 'Khoa Nhi', 'Tầng 1 - Khu B'),
('K04', 'Khoa Sản Phụ Khoa', 'Tầng 4 - Khu B'),
('K05', 'Khoa Mắt', 'Tầng 2 - Khu C'),
('K06', 'Khoa Tim Mạch', 'Tầng 5 - Khu A'),
('K07', 'Khoa Thần Kinh', 'Tầng 6 - Khu A'),
('K08', 'Khoa Da Liễu', 'Tầng 1 - Khu C'),
('EMG01', 'Phòng Cấp Cứu 1', 'Tầng 1 - Cấp Cứu'),
('VIP01', 'Phòng Khám VIP', 'Tầng 8 - Khu VIP'),
('REG01', 'Quầy Đăng Ký', 'Sảnh Chính'),
('PAY01', 'Quầy Thu Ngân', 'Sảnh Chính');

-- =====================================
-- 2. DỮ LIỆU VÉ (TICKETS) - KHOA NỘI K01
-- =====================================

-- Vé đã hoàn thành (COMPLETED) - trong quá khứ
INSERT INTO tickets (department_id, number, status, created_at, called_at, completed_at, holder_name) VALUES
(1, 1, 'COMPLETED', NOW() - INTERVAL 120 MINUTE, NOW() - INTERVAL 110 MINUTE, NOW() - INTERVAL 100 MINUTE, 'Nguyễn Văn An'),
(1, 2, 'COMPLETED', NOW() - INTERVAL 110 MINUTE, NOW() - INTERVAL 100 MINUTE, NOW() - INTERVAL 90 MINUTE, 'Trần Thị Bình'),
(1, 3, 'COMPLETED', NOW() - INTERVAL 100 MINUTE, NOW() - INTERVAL 90 MINUTE, NOW() - INTERVAL 80 MINUTE, 'Lê Văn Cường');

-- Vé đang được gọi (CALLED) - hiện tại
INSERT INTO tickets (department_id, number, status, created_at, called_at, holder_name) VALUES
(1, 4, 'CALLED', NOW() - INTERVAL 60 MINUTE, NOW() - INTERVAL 5 MINUTE, 'Phạm Thị Dung');

-- Vé đang chờ (WAITING) - hàng đợi
INSERT INTO tickets (department_id, number, status, created_at, holder_name) VALUES
(1, 5, 'WAITING', NOW() - INTERVAL 50 MINUTE, 'Hoàng Văn Em'),
(1, 6, 'WAITING', NOW() - INTERVAL 45 MINUTE, 'Vũ Thị Phương'),
(1, 7, 'WAITING', NOW() - INTERVAL 40 MINUTE, 'Đỗ Văn Giang'),
(1, 8, 'WAITING', NOW() - INTERVAL 35 MINUTE, 'Phan Thị Hoa'),
(1, 9, 'WAITING', NOW() - INTERVAL 30 MINUTE, 'Bùi Văn Ích'),
(1, 10, 'WAITING', NOW() - INTERVAL 25 MINUTE, 'Đặng Thị Kim');

-- =====================================
-- 3. DỮ LIỆU VÉ - KHOA NGOẠI K02
-- =====================================

-- Vé đã hoàn thành
INSERT INTO tickets (department_id, number, status, created_at, called_at, completed_at, holder_name) VALUES
(2, 1, 'COMPLETED', NOW() - INTERVAL 90 MINUTE, NOW() - INTERVAL 80 MINUTE, NOW() - INTERVAL 70 MINUTE, 'Mai Văn Long');

-- Vé đang được gọi
INSERT INTO tickets (department_id, number, status, created_at, called_at, holder_name) VALUES
(2, 2, 'CALLED', NOW() - INTERVAL 40 MINUTE, NOW() - INTERVAL 3 MINUTE, 'Ngô Thị Minh');

-- Vé đang chờ
INSERT INTO tickets (department_id, number, status, created_at, holder_name) VALUES
(2, 3, 'WAITING', NOW() - INTERVAL 30 MINUTE, 'Lý Văn Năm'),
(2, 4, 'WAITING', NOW() - INTERVAL 25 MINUTE, 'Trịnh Thị Oanh'),
(2, 5, 'WAITING', NOW() - INTERVAL 20 MINUTE, 'Đinh Văn Phát');

-- =====================================
-- 4. DỮ LIỆU VÉ - KHOA NHI K03
-- =====================================

-- Vé đang chờ
INSERT INTO tickets (department_id, number, status, created_at, holder_name) VALUES
(3, 1, 'WAITING', NOW() - INTERVAL 35 MINUTE, 'Bé Nguyễn Quang'),
(3, 2, 'WAITING', NOW() - INTERVAL 30 MINUTE, 'Bé Trần Thy'),
(3, 3, 'WAITING', NOW() - INTERVAL 25 MINUTE, 'Bé Lê Sơn'),
(3, 4, 'WAITING', NOW() - INTERVAL 20 MINUTE, 'Bé Phạm Thảo');

-- =====================================
-- 5. DỮ LIỆU VÉ - KHOA SẢN PHỤ KHOA K04
-- =====================================

-- Vé đang được gọi
INSERT INTO tickets (department_id, number, status, created_at, called_at, holder_name) VALUES
(4, 1, 'CALLED', NOW() - INTERVAL 20 MINUTE, NOW() - INTERVAL 2 MINUTE, 'Chị Đỗ Thị Uyên');

-- Vé đang chờ
INSERT INTO tickets (department_id, number, status, created_at, holder_name) VALUES
(4, 2, 'WAITING', NOW() - INTERVAL 15 MINUTE, 'Chị Phan Thị Vân'),
(4, 3, 'WAITING', NOW() - INTERVAL 10 MINUTE, 'Chị Bùi Thị Xuân');

-- =====================================
-- HOÀN THÀNH IMPORT DỮ LIỆU
-- =====================================

-- Kiểm tra kết quả
SELECT 'TỔNG SỐ KHOA' as info, COUNT(*) as so_luong FROM departments
UNION ALL
SELECT 'TỔNG SỐ VÉ' as info, COUNT(*) as so_luong FROM tickets
UNION ALL
SELECT 'VÉ ĐANG CHỜ' as info, COUNT(*) as so_luong FROM tickets WHERE status = 'WAITING'
UNION ALL
SELECT 'VÉ ĐANG GỌI' as info, COUNT(*) as so_luong FROM tickets WHERE status = 'CALLED'
UNION ALL
SELECT 'VÉ ĐÃ HOÀN THÀNH' as info, COUNT(*) as so_luong FROM tickets WHERE status = 'COMPLETED';

-- Hiển thị trạng thái từng khoa
SELECT 
    d.code,
    d.name,
    COUNT(CASE WHEN t.status = 'WAITING' THEN 1 END) as cho,
    COUNT(CASE WHEN t.status = 'CALLED' THEN 1 END) as dang_goi,
    COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as hoan_thanh
FROM departments d
LEFT JOIN tickets t ON d.id = t.department_id
GROUP BY d.id, d.code, d.name
ORDER BY d.code;
