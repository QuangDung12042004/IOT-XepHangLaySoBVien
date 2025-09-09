# HƯỚNG DẪN SỬ DỤNG HOSPITAL QUEUE SYSTEM

## 📋 Tổng quan
Hệ thống xếp hàng đợi bệnh viện với 3 thành phần chính:
- **Trang người dùng**: Lấy số thứ tự, xem hàng đợi, đếm ngược
- **Trang nhân viên**: Gọi số tiếp theo 
- **Backend API**: Quản lý dữ liệu và logic nghiệp vụ

## 🚀 Cách chạy hệ thống

### 1. Khởi động Backend
```bash
cd backend
java -jar target/hospital-queue-0.0.1-SNAPSHOT.jar
```
✅ Server chạy tại: http://localhost:8080

### 2. Import dữ liệu mẫu vào MySQL
```bash
mysql -u root -p hospital_queue < fontend/sample_data.sql
```

### 3. Mở các trang web

**Trang người dùng**: `fontend/user.html`
- Chọn khoa khám
- Nhập tên (sẽ được lưu)
- Lấy số thứ tự
- Xem đồng hồ đếm ngược

**Trang nhân viên**: `fontend/staff.html` 
- Chọn khoa
- Nhập mã nhân viên (demo: >= 3 ký tự)
- Gọi số tiếp theo

## ⏰ Tính năng đồng hồ đếm ngược

### Cách hoạt động:
1. Sau khi lấy số, hệ thống tính thời gian dự kiến
2. Đồng hồ đếm ngược từ thời gian đó về 0
3. **QUAN TRỌNG**: Đồng hồ được lưu trong localStorage, tiếp tục chạy sau khi reload trang
4. Khi về 0, phát âm thanh "bíp bíp bíp"

### Test đồng hồ đếm ngược:
1. Mở `user.html`, lấy 1 số
2. **Reload trang** → Đồng hồ vẫn tiếp tục đếm
3. **Đóng tab, mở lại** → Đồng hồ vẫn chạy đúng
4. **Chuyển tab khác rồi quay lại** → Đồng hồ cập nhật chính xác

## 🏥 Quản lý dữ liệu MySQL

### Cấu trúc bảng:

**departments (khoa)**
```sql
- id: BIGINT AUTO_INCREMENT PRIMARY KEY  
- code: VARCHAR(100) UNIQUE NOT NULL     # Mã khoa (K01, K02...)
- name: VARCHAR(255) NOT NULL            # Tên khoa
- location: VARCHAR(255)                 # Vị trí
```

**tickets (vé số)**
```sql
- id: BIGINT AUTO_INCREMENT PRIMARY KEY
- department_id: BIGINT NOT NULL         # ID khoa
- number: INT NOT NULL                   # Số thứ tự trong ngày
- status: VARCHAR(20) NOT NULL           # WAITING/CALLED/COMPLETED/CANCELED
- createdAt: DATETIME NOT NULL           # Thời gian lấy số
- calledAt: DATETIME                     # Thời gian được gọi
- completedAt: DATETIME                  # Thời gian hoàn thành
- holder_name: VARCHAR(100)              # Tên người lấy số
```

### Thêm khoa mới:
```sql
INSERT INTO departments (code, name, location) VALUES 
('K99', 'Khoa Thử Nghiệm', 'Tầng 9');
```

### Thêm vé chờ thủ công:
```sql
INSERT INTO tickets (department_id, number, status, createdAt, holder_name) VALUES 
(1, 15, 'WAITING', NOW(), 'Nguyễn Văn Test');
```

### Xem trạng thái hàng đợi:
```sql
SELECT d.name, t.number, t.status, t.holder_name, t.createdAt
FROM tickets t 
JOIN departments d ON t.department_id = d.id 
WHERE d.code = 'K01' AND t.status IN ('WAITING', 'CALLED')
ORDER BY t.createdAt;
```

## 🔧 API Endpoints

**Lấy danh sách khoa:**
```
GET /api/departments
```

**Lấy số thứ tự:**
```
POST /api/tickets/take-number
Body: {"departmentId": 1, "name": "Nguyễn Văn A"}
```

**Xem trạng thái hàng đợi:**
```
GET /api/tickets/queue-status/{departmentId}
```

**Gọi số tiếp theo:**
```
POST /api/tickets/call-next/{departmentId}
```

## 🎯 Workflow thực tế

### Người dùng:
1. Mở `user.html`
2. Chọn BASE_URL (localhost hoặc IP)
3. Chọn khoa
4. Nhập tên → **Tên được lưu cho lần sau**
5. Bấm "Lấy số" → Nhận số + bắt đầu đếm ngược
6. **Đồng hồ tiếp tục chạy dù reload/đóng mở trang**
7. Khi đến lượt → Nghe "bíp bíp bíp"

### Nhân viên:
1. Mở `staff.html`  
2. Chọn khoa đang trực
3. Nhập mã nhân viên (demo)
4. Bấm "Gọi số tiếp theo" → Gọi người đầu hàng chờ
5. Hiển thị "Đã gọi: Tên - Số"

## 🐛 Debug & Troubleshooting

**Lỗi không kết nối được backend:**
- Kiểm tra server có chạy tại localhost:8080
- Thử đổi BASE_URL trong dropdown

**Đồng hồ không chạy:**
- Kiểm tra Console (F12) có lỗi JavaScript không
- Xóa localStorage: `localStorage.clear()`

**Không có dữ liệu:**
- Import lại file sample_data.sql
- Kiểm tra kết nối MySQL

**Âm thanh không phát:**
- Trình duyệt chặn autoplay → Click vào trang trước
- Fallback: sẽ hiện alert thay âm thanh

## 📝 Tùy chỉnh

**Thay đổi thời gian ước tính:**
- Sửa trong TicketService.java: `estimatedWaitTime(waitingTickets.size() * 10)`
- 10 = 10 phút/người, có thể đổi thành 5, 15...

**Thêm âm thanh khác:**
- Sửa function `beep()` trong app-user.js
- Đổi `freq = 880` (tần số), `repeat = 3` (số lần)

**Thay đổi giao diện:**
- Sửa file Bootstrap classes trong HTML
- Tùy chỉnh CSS trong assets/style.css

## 📊 Báo cáo & Thống kê

**Xem số liệu theo ngày:**
```sql
SELECT 
    d.name as khoa,
    COUNT(*) as tong_ve,
    COUNT(CASE WHEN t.status = 'COMPLETED' THEN 1 END) as da_kham
FROM tickets t
JOIN departments d ON t.department_id = d.id  
WHERE DATE(t.createdAt) = CURDATE()
GROUP BY d.id, d.name;
```

**Thời gian chờ trung bình:**
```sql
SELECT 
    d.name,
    AVG(TIMESTAMPDIFF(MINUTE, t.createdAt, t.calledAt)) as phut_cho_tb
FROM tickets t
JOIN departments d ON t.department_id = d.id
WHERE t.calledAt IS NOT NULL 
AND DATE(t.createdAt) = CURDATE()
GROUP BY d.id, d.name;
```
