// Khai báo chân k?t n?i v?i LCD (Ch? d? 4-bit)
sbit LCD_RS at RC2_bit;
sbit LCD_EN at RC1_bit;
sbit LCD_D4 at RD4_bit;
sbit LCD_D5 at RD5_bit;
sbit LCD_D6 at RD6_bit;
sbit LCD_D7 at RD7_bit;

sbit LCD_RS_Direction at TRISC2_bit;
sbit LCD_EN_Direction at TRISC1_bit;
sbit LCD_D4_Direction at TRISD4_bit;
sbit LCD_D5_Direction at TRISD5_bit;
sbit LCD_D6_Direction at TRISD6_bit;
sbit LCD_D7_Direction at TRISD7_bit;

// Bi?n toàn c?c
unsigned int soThuTu = 0;
char txtSo[7];
bit update_flag; // C? báo hi?u c?n c?p nh?t màn hình

// === HÀM PH?C V? NG?T (INTERRUPT SERVICE ROUTINE) ===
// Ðo?n code này s? t? d?ng ch?y m?i khi có s? ki?n nh?n nút trên chân RB0
void interrupt() {
  // Ki?m tra xem có ph?i ng?t ngoài t? chân RB0 không
  if (INTCON.INTF) {
    delay_ms(150);     // Ch?ng d?i phím
    soThuTu++;         // Tang s? th? t?
    update_flag = 1;   // B?t c? báo hi?u cho vòng l?p chính
    INTCON.INTF = 0;   // Xóa c? ng?t d? s?n sàng cho l?n ti?p theo
  }
}

// Hàm c?p nh?t màn hình và g?i UART
void CapNhatHeThong() {
    IntToStr(soThuTu, txtSo);
    Ltrim(txtSo);

    Lcd_Out(2, 13, "    ");
    Lcd_Out(2, 13, txtSo);

    UART1_Write_Text("So thu tu moi: ");
    UART1_Write_Text(txtSo);
    UART1_Write_Text("\r\n");
}

void main() {
  // C?u hình PORTB: RB0 và RB1 là input
  TRISB = 0b00000011;
  PORTB = 0;

  // === C?U HÌNH NG?T NGOÀI CHO NÚT L?Y S? (RB0) ===
  INTCON.GIE = 1;         // Cho phép ng?t toàn c?c (Global Interrupt Enable)
  INTCON.INTE = 1;         // Cho phép ng?t ngoài trên chân RB0 (External Interrupt Enable)
  OPTION_REG.INTEDG = 1;   // C?u hình ng?t t?i c?nh lên (t? th?p -> cao)

  // Kh?i t?o các module
  Lcd_Init();
  Lcd_Cmd(_LCD_CLEAR);
  Lcd_Cmd(_LCD_CURSOR_OFF);

  UART1_Init(9600);
  delay_ms(100);

  // Hi?n th? thông báo ban d?u
  Lcd_Out(1, 1, " HE THONG LAY SO ");
  Lcd_Out(2, 1, "So hien tai: ");
  update_flag = 1; // B?t c? d? c?p nh?t s? 0 ban d?u

  // Vòng l?p chính c?a chuong trình
  while(1) {
    // 1. Ki?m tra c? báo hi?u t? hàm ng?t
    if (update_flag) {
      CapNhatHeThong();
      update_flag = 0; // Xóa c? sau khi dã c?p nh?t
    }

    // 2. X? lý nút Reset S? (RB1) - V?n dùng cách ki?m tra thông thu?ng
    if (PORTB.F1 == 1) {
        delay_ms(150);
        if (PORTB.F1 == 1) {
          soThuTu = 0;
          update_flag = 1; // B?t c? d? c?p nh?t l?i màn hình
          while(PORTB.F1 == 1);
        }
    }
  }
}