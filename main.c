#include <xc.h>
#include <stdio.h>
#include <string.h>

#define _XTAL_FREQ 20000000

// CONFIG (cho PIC16F887, nếu dùng 16F877A thì sửa lại tương ứng)
#pragma config FOSC = HS, WDTE = OFF, PWRTE = OFF, BOREN = ON, LVP = OFF, CPD = OFF, WRT = OFF, CP = OFF

// ===== LCD pin mapping =====
#define RS RD0
#define EN RD1
#define D4 RD4
#define D5 RD5
#define D6 RD6
#define D7 RD7

// ===== Prototypes =====
void init_pins(void);

void Lcd_Init();
void Lcd_Command(char cmd);
void Lcd_Char(char data);
void Lcd_String(const char *str);
void Lcd_Set_Cursor(char row, char column);
void Lcd_Clear();

void UART_Init(long baudrate);
void UART_Write(char data);
void UART_Write_Text(const char *text);

// ===== Global =====
int currentNumber = 0;

// ================= MAIN =================
void main(void) {
    init_pins();
    Lcd_Init();
    UART_Init(9600);

    Lcd_Set_Cursor(1,1);
    Lcd_String("CALLING SYSTEM");
    __delay_ms(1000);
    Lcd_Clear();

    unsigned char last = 1;

    while(1){
        unsigned char now = RA0;   // 1 = nhả, 0 = nhấn (pull-up)
        if(last == 1 && now == 0){ // phát hiện cạnh nhấn
            __delay_ms(25);        // debounce
            if(RA0 == 0){
                currentNumber++;
                if(currentNumber > 99) currentNumber = 1;

                char buffer[16];
                sprintf(buffer,"Now: %02d", currentNumber);

                Lcd_Clear();
                Lcd_Set_Cursor(1,1);
                Lcd_String("Calling Number");
                Lcd_Set_Cursor(2,1);
                Lcd_String(buffer);

                UART_Write_Text("CALL:");
                UART_Write_Text(buffer);
                UART_Write('\n');
            }
            while(RA0 == 0);   // chờ nhả
            __delay_ms(10);
        }
        last = now;
    }
}

// ================= INIT =================
void init_pins(void){
    // Tắt analog & comparator (PIC16F887)
    ANSEL = 0x00;
    ANSELH = 0x00;
    CM1CON0 = 0x00;
    CM2CON0 = 0x00;

    TRISAbits.TRISA0 = 1;  // nút bấm input
    TRISD = 0x00;          // LCD output
}

// ================= LCD =================
void Lcd_Port(char a){
    D4 = (a >> 0) & 1;
    D5 = (a >> 1) & 1;
    D6 = (a >> 2) & 1;
    D7 = (a >> 3) & 1;
}

void Lcd_Cmd(char a){
    RS = 0;
    Lcd_Port(a);
    EN = 1; __delay_us(40); EN = 0;
}

void Lcd_Clear(){
    Lcd_Command(0x01);
    __delay_ms(2);
}

void Lcd_Set_Cursor(char row, char col){
    char pos;
    if(row == 1) pos = 0x80 + col - 1;
    if(row == 2) pos = 0xC0 + col - 1;
    Lcd_Command(pos);
}

void Lcd_Init(){
    Lcd_Port(0x00);
    __delay_ms(20);
    Lcd_Cmd(0x03); __delay_ms(5);
    Lcd_Cmd(0x03); __delay_ms(11);
    Lcd_Cmd(0x03);
    Lcd_Cmd(0x02);
    Lcd_Command(0x28);
    Lcd_Command(0x0C);
    Lcd_Command(0x06);
    Lcd_Command(0x01);
}

void Lcd_Command(char cmd){
    RS = 0;
    Lcd_Port(cmd >> 4);
    EN = 1; __delay_us(40); EN = 0;
    Lcd_Port(cmd & 0x0F);
    EN = 1; __delay_us(40); EN = 0;
}

void Lcd_Char(char data){
    RS = 1;
    Lcd_Port(data >> 4);
    EN = 1; __delay_us(40); EN = 0;
    Lcd_Port(data & 0x0F);
    EN = 1; __delay_us(40); EN = 0;
}

void Lcd_String(const char *str){
    while(*str) Lcd_Char(*str++);
}

// ================= UART =================
void UART_Init(long baudrate){
    float temp;
    TRISC6 = 0;  // TX
    TRISC7 = 1;  // RX
    temp = (_XTAL_FREQ - baudrate*64)/(baudrate*64);
    if(temp < 256){
        SPBRG = (unsigned char)temp;
        BRGH = 0;
    }
    else{
        temp = (_XTAL_FREQ - baudrate*16)/(baudrate*16);
        SPBRG = (unsigned char)temp;
        BRGH = 1;
    }
    SYNC = 0;
    SPEN = 1;
    TXEN = 1;
    CREN = 1;
}

void UART_Write(char data){
    while(!TRMT);
    TXREG = data;
}

void UART_Write_Text(const char *text){
    while(*text) UART_Write(*text++);
}
