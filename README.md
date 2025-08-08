# Module: Bảng hiển thị trung tâm (Thành viên 3)

## Mục đích
Module dùng PIC16F887 để hiển thị số thứ tự đang được gọi từ các quầy. Nhận dữ liệu UART như `Q1-025`.

## Cách sử dụng
1. Mở file `display_module [Autosaved].pdsprj` bằng Proteus.
2. Nạp file HEX: `PIC16F887.production.hex` cho chip.
3. Nhấn nút Run để mô phỏng.
4. Gõ chuỗi `Q1-007` trong Virtual Terminal để kiểm thử.

## File và thư mục
- `proteus/`: Mạch mô phỏng Proteus và file HEX.
- `src/`: Mã nguồn (C, header).
