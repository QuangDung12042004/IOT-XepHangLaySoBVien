// Khai b�o ch�n k?t n?i v?i LCD (Ch? d? 4-bit)
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

// Bi?n to�n c?c
unsigned int soThuTu = 0;
char txtSo[7];
bit update_flag; // C? b�o hi?u c?n c?p nh?t m�n h�nh

// === H�M PH?C V? NG?T (INTERRUPT SERVICE ROUTINE) ===
// �o?n code n�y s? t? d?ng ch?y m?i khi c� s? ki?n nh?n n�t tr�n ch�n RB0
void interrupt() {
  // Ki?m tra xem c� ph?i ng?t ngo�i t? ch�n RB0 kh�ng
  if (INTCON.INTF) {
    delay_ms(150);     // Ch?ng d?i ph�m
    soThuTu++;         // Tang s? th? t?
    update_flag = 1;   // B?t c? b�o hi?u cho v�ng l?p ch�nh
    INTCON.INTF = 0;   // X�a c? ng?t d? s?n s�ng cho l?n ti?p theo
  }
}

// H�m c?p nh?t m�n h�nh v� g?i UART
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
  // C?u h�nh PORTB: RB0 v� RB1 l� input
  TRISB = 0b00000011;
  PORTB = 0;

  // === C?U H�NH NG?T NGO�I CHO N�T L?Y S? (RB0) ===
  INTCON.GIE = 1;         // Cho ph�p ng?t to�n c?c (Global Interrupt Enable)
  INTCON.INTE = 1;         // Cho ph�p ng?t ngo�i tr�n ch�n RB0 (External Interrupt Enable)
  OPTION_REG.INTEDG = 1;   // C?u h�nh ng?t t?i c?nh l�n (t? th?p -> cao)

  // Kh?i t?o c�c module
  Lcd_Init();
  Lcd_Cmd(_LCD_CLEAR);
  Lcd_Cmd(_LCD_CURSOR_OFF);

  UART1_Init(9600);
  delay_ms(100);

  // Hi?n th? th�ng b�o ban d?u
  Lcd_Out(1, 1, " HE THONG LAY SO ");
  Lcd_Out(2, 1, "So hien tai: ");
  update_flag = 1; // B?t c? d? c?p nh?t s? 0 ban d?u

  // V�ng l?p ch�nh c?a chuong tr�nh
  while(1) {
    // 1. Ki?m tra c? b�o hi?u t? h�m ng?t
    if (update_flag) {
      CapNhatHeThong();
      update_flag = 0; // X�a c? sau khi d� c?p nh?t
    }

    // 2. X? l� n�t Reset S? (RB1) - V?n d�ng c�ch ki?m tra th�ng thu?ng
    if (PORTB.F1 == 1) {
        delay_ms(150);
        if (PORTB.F1 == 1) {
          soThuTu = 0;
          update_flag = 1; // B?t c? d? c?p nh?t l?i m�n h�nh
          while(PORTB.F1 == 1);
        }
    }
  }
}