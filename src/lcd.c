
#include <xc.h>
#define _XTAL_FREQ 20000000

#define LCD_RS RD0
#define LCD_RW RD1
#define LCD_EN RD2
#define LCD_DATA PORTC

void lcd_cmd(unsigned char cmd) {
    LCD_RS = 0;
    LCD_RW = 0;
    LCD_DATA = cmd;
    LCD_EN = 1;
    __delay_ms(2);
    LCD_EN = 0;
}

void lcd_init(void) {
    TRISC = 0x00;
    TRISD = 0x00;
    __delay_ms(15);
    lcd_cmd(0x38);
    lcd_cmd(0x0C);
    lcd_cmd(0x06);
    lcd_cmd(0x01);
}

void lcd_clear(void) {
    lcd_cmd(0x01);
}

void lcd_gotoxy(unsigned char x, unsigned char y) {
    unsigned char addr = (y == 1) ? 0x80 : 0xC0;
    addr += x - 1;
    lcd_cmd(addr);
}

void lcd_putch(char c) {
    LCD_RS = 1;
    LCD_RW = 0;
    LCD_DATA = c;
    LCD_EN = 1;
    __delay_ms(2);
    LCD_EN = 0;
}

void lcd_puts(const char *s) {
    while (*s) lcd_putch(*s++);
}
