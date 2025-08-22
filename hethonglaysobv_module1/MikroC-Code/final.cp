#line 1 "C:/Users/LENOVO/OneDrive - ut.edu.vn/Desktop/final.c"

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


unsigned int soThuTu = 0;
char txtSo[7];
bit update_flag;



void interrupt() {

 if (INTCON.INTF) {
 delay_ms(150);
 soThuTu++;
 update_flag = 1;
 INTCON.INTF = 0;
 }
}


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

 TRISB = 0b00000011;
 PORTB = 0;


 INTCON.GIE = 1;
 INTCON.INTE = 1;
 OPTION_REG.INTEDG = 1;


 Lcd_Init();
 Lcd_Cmd(_LCD_CLEAR);
 Lcd_Cmd(_LCD_CURSOR_OFF);

 UART1_Init(9600);
 delay_ms(100);


 Lcd_Out(1, 1, " HE THONG LAY SO ");
 Lcd_Out(2, 1, "So hien tai: ");
 update_flag = 1;


 while(1) {

 if (update_flag) {
 CapNhatHeThong();
 update_flag = 0;
 }


 if (PORTB.F1 == 1) {
 delay_ms(150);
 if (PORTB.F1 == 1) {
 soThuTu = 0;
 update_flag = 1;
 while(PORTB.F1 == 1);
 }
 }
 }
}
