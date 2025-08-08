#ifndef UART_H
#define UART_H

#include <xc.h>
#include <stdint.h>

void uart_init(void);
uint8_t uart_data_ready(void);
char uart_read(void);
void uart_write(char data);
void uart_write_text(const char *text);

#endif
