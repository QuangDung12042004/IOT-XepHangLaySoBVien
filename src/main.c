
#include <xc.h>
#include "lcd.h"
#include "uart.h"

#define _XTAL_FREQ 20000000

void main(void) {
    TRISD = 0x00;
    lcd_init();
    uart_init();

    lcd_clear();
    lcd_gotoxy(1, 1);
    lcd_puts("LCD + UART Demo");

    __delay_ms(2000);
    lcd_clear();

    while (1) {
        if (uart_data_ready()) {
            char c = uart_read();
            lcd_gotoxy(1, 1);
            lcd_putch(c);
        }
    }
}
