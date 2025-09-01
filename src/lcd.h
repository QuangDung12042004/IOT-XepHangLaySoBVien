
#ifndef LCD_H
#define LCD_H

void lcd_init(void);
void lcd_clear(void);
void lcd_gotoxy(unsigned char x, unsigned char y);
void lcd_puts(const char *s);
void lcd_putch(char c);

#endif
