# Module: Thiết kế & lập trình module gọi số (Thành viên 2)

## Mục đích  
Module được xây dựng để gọi số thứ tự phục vụ khách hàng. Sử dụng vi điều khiển **PIC16F887** kết hợp LCD/LED hiển thị. Khi nhân viên nhấn nút gọi, hệ thống sẽ lấy số tiếp theo, gửi số đến bảng hiển thị trung tâm/backend và đồng thời hiển thị trên LCD/LED.

---

## Chức năng chính  
- **Vẽ sơ đồ mạch**: gồm nút gọi, LCD/LED, vi điều khiển PIC16F887.  
- **Lập trình C/C++ cho vi điều khiển**:  
  - Nhân viên nhấn nút gọi → lấy số tiếp theo.  
  - Gửi số gọi đến **bảng trung tâm/backend** qua UART.  
  - Hiển thị số đang gọi trên LCD/LED.  
  - Quản lý danh sách chờ, cập nhật trạng thái số.  
- **Kiểm thử trên Proteus**: mô phỏng mạch, kết nối với module lấy số.  

---

## Cách sử dụng  
1. Mở file **call_module.pdsprj** bằng Proteus.  
2. Nạp file **PIC16F887.production.hex** vào vi điều khiển.  
3. Nhấn **Run** để mô phỏng.  
4. Thử nhấn nút gọi để kiểm tra chức năng lấy số và gửi đến bảng trung tâm.  

---

## File và thư mục  
- `proteus/`: Mạch mô phỏng Proteus và file HEX.  
- `src/`: Mã nguồn (C/C++ và header).  
