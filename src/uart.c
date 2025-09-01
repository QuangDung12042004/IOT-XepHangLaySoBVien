#include "uart.h"

void uart_init(void) {
    TXSTAbits.BRGH = 1;         // High speed
    BAUDCTLbits.BRG16 = 1;      // 16-bit Baud Rate Generator
    SPBRG = 129;                // Baud rate 9600 for 20MHz
    SPBRGH = 0;

    RCSTAbits.SPEN = 1;         // Enable serial port
    TXSTAbits.TXEN = 1;         // Enable transmitter
    RCSTAbits.CREN = 1;         // Enable receiver
}

uint8_t uart_data_ready(void) {
    return PIR1bits.RCIF;
}

char uart_read(void) {
    while (!uart_data_ready());
    return RCREG;
}

void uart_write(char data) {
    while (!TXSTAbits.TRMT);
    TXREG = data;
}

void uart_write_text(const char *text) {
    while (*text != '\0') {
        uart_write(*text);
        text++;
    }
}
