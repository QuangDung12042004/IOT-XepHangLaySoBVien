
_interrupt:
	MOVWF      R15+0
	SWAPF      STATUS+0, 0
	CLRF       STATUS+0
	MOVWF      ___saveSTATUS+0
	MOVF       PCLATH+0, 0
	MOVWF      ___savePCLATH+0
	CLRF       PCLATH+0

;final.c,23 :: 		void interrupt() {
;final.c,25 :: 		if (INTCON.INTF) {
	BTFSS      INTCON+0, 1
	GOTO       L_interrupt0
;final.c,26 :: 		delay_ms(150);     // Ch?ng d?i phím
	MOVLW      2
	MOVWF      R11+0
	MOVLW      134
	MOVWF      R12+0
	MOVLW      153
	MOVWF      R13+0
L_interrupt1:
	DECFSZ     R13+0, 1
	GOTO       L_interrupt1
	DECFSZ     R12+0, 1
	GOTO       L_interrupt1
	DECFSZ     R11+0, 1
	GOTO       L_interrupt1
;final.c,27 :: 		soThuTu++;         // Tang s? th? t?
	INCF       _soThuTu+0, 1
	BTFSC      STATUS+0, 2
	INCF       _soThuTu+1, 1
;final.c,28 :: 		update_flag = 1;   // B?t c? báo hi?u cho vòng l?p chính
	BSF        _update_flag+0, BitPos(_update_flag+0)
;final.c,29 :: 		INTCON.INTF = 0;   // Xóa c? ng?t d? s?n sàng cho l?n ti?p theo
	BCF        INTCON+0, 1
;final.c,30 :: 		}
L_interrupt0:
;final.c,31 :: 		}
L_end_interrupt:
L__interrupt12:
	MOVF       ___savePCLATH+0, 0
	MOVWF      PCLATH+0
	SWAPF      ___saveSTATUS+0, 0
	MOVWF      STATUS+0
	SWAPF      R15+0, 1
	SWAPF      R15+0, 0
	RETFIE
; end of _interrupt

_CapNhatHeThong:

;final.c,34 :: 		void CapNhatHeThong() {
;final.c,35 :: 		IntToStr(soThuTu, txtSo);
	MOVF       _soThuTu+0, 0
	MOVWF      FARG_IntToStr_input+0
	MOVF       _soThuTu+1, 0
	MOVWF      FARG_IntToStr_input+1
	MOVLW      _txtSo+0
	MOVWF      FARG_IntToStr_output+0
	CALL       _IntToStr+0
;final.c,36 :: 		Ltrim(txtSo);
	MOVLW      _txtSo+0
	MOVWF      FARG_Ltrim_string+0
	CALL       _Ltrim+0
;final.c,38 :: 		Lcd_Out(2, 13, "    ");
	MOVLW      2
	MOVWF      FARG_Lcd_Out_row+0
	MOVLW      13
	MOVWF      FARG_Lcd_Out_column+0
	MOVLW      ?lstr1_final+0
	MOVWF      FARG_Lcd_Out_text+0
	CALL       _Lcd_Out+0
;final.c,39 :: 		Lcd_Out(2, 13, txtSo);
	MOVLW      2
	MOVWF      FARG_Lcd_Out_row+0
	MOVLW      13
	MOVWF      FARG_Lcd_Out_column+0
	MOVLW      _txtSo+0
	MOVWF      FARG_Lcd_Out_text+0
	CALL       _Lcd_Out+0
;final.c,41 :: 		UART1_Write_Text("So thu tu moi: ");
	MOVLW      ?lstr2_final+0
	MOVWF      FARG_UART1_Write_Text_uart_text+0
	CALL       _UART1_Write_Text+0
;final.c,42 :: 		UART1_Write_Text(txtSo);
	MOVLW      _txtSo+0
	MOVWF      FARG_UART1_Write_Text_uart_text+0
	CALL       _UART1_Write_Text+0
;final.c,43 :: 		UART1_Write_Text("\r\n");
	MOVLW      ?lstr3_final+0
	MOVWF      FARG_UART1_Write_Text_uart_text+0
	CALL       _UART1_Write_Text+0
;final.c,44 :: 		}
L_end_CapNhatHeThong:
	RETURN
; end of _CapNhatHeThong

_main:

;final.c,46 :: 		void main() {
;final.c,48 :: 		TRISB = 0b00000011;
	MOVLW      3
	MOVWF      TRISB+0
;final.c,49 :: 		PORTB = 0;
	CLRF       PORTB+0
;final.c,52 :: 		INTCON.GIE = 1;         // Cho phép ng?t toàn c?c (Global Interrupt Enable)
	BSF        INTCON+0, 7
;final.c,53 :: 		INTCON.INTE = 1;         // Cho phép ng?t ngoài trên chân RB0 (External Interrupt Enable)
	BSF        INTCON+0, 4
;final.c,54 :: 		OPTION_REG.INTEDG = 1;   // C?u hình ng?t t?i c?nh lên (t? th?p -> cao)
	BSF        OPTION_REG+0, 6
;final.c,57 :: 		Lcd_Init();
	CALL       _Lcd_Init+0
;final.c,58 :: 		Lcd_Cmd(_LCD_CLEAR);
	MOVLW      1
	MOVWF      FARG_Lcd_Cmd_out_char+0
	CALL       _Lcd_Cmd+0
;final.c,59 :: 		Lcd_Cmd(_LCD_CURSOR_OFF);
	MOVLW      12
	MOVWF      FARG_Lcd_Cmd_out_char+0
	CALL       _Lcd_Cmd+0
;final.c,61 :: 		UART1_Init(9600);
	MOVLW      51
	MOVWF      SPBRG+0
	BSF        TXSTA+0, 2
	CALL       _UART1_Init+0
;final.c,62 :: 		delay_ms(100);
	MOVLW      2
	MOVWF      R11+0
	MOVLW      4
	MOVWF      R12+0
	MOVLW      186
	MOVWF      R13+0
L_main2:
	DECFSZ     R13+0, 1
	GOTO       L_main2
	DECFSZ     R12+0, 1
	GOTO       L_main2
	DECFSZ     R11+0, 1
	GOTO       L_main2
	NOP
;final.c,65 :: 		Lcd_Out(1, 1, " HE THONG LAY SO ");
	MOVLW      1
	MOVWF      FARG_Lcd_Out_row+0
	MOVLW      1
	MOVWF      FARG_Lcd_Out_column+0
	MOVLW      ?lstr4_final+0
	MOVWF      FARG_Lcd_Out_text+0
	CALL       _Lcd_Out+0
;final.c,66 :: 		Lcd_Out(2, 1, "So hien tai: ");
	MOVLW      2
	MOVWF      FARG_Lcd_Out_row+0
	MOVLW      1
	MOVWF      FARG_Lcd_Out_column+0
	MOVLW      ?lstr5_final+0
	MOVWF      FARG_Lcd_Out_text+0
	CALL       _Lcd_Out+0
;final.c,67 :: 		update_flag = 1; // B?t c? d? c?p nh?t s? 0 ban d?u
	BSF        _update_flag+0, BitPos(_update_flag+0)
;final.c,70 :: 		while(1) {
L_main3:
;final.c,72 :: 		if (update_flag) {
	BTFSS      _update_flag+0, BitPos(_update_flag+0)
	GOTO       L_main5
;final.c,73 :: 		CapNhatHeThong();
	CALL       _CapNhatHeThong+0
;final.c,74 :: 		update_flag = 0; // Xóa c? sau khi dã c?p nh?t
	BCF        _update_flag+0, BitPos(_update_flag+0)
;final.c,75 :: 		}
L_main5:
;final.c,78 :: 		if (PORTB.F1 == 1) {
	BTFSS      PORTB+0, 1
	GOTO       L_main6
;final.c,79 :: 		delay_ms(150);
	MOVLW      2
	MOVWF      R11+0
	MOVLW      134
	MOVWF      R12+0
	MOVLW      153
	MOVWF      R13+0
L_main7:
	DECFSZ     R13+0, 1
	GOTO       L_main7
	DECFSZ     R12+0, 1
	GOTO       L_main7
	DECFSZ     R11+0, 1
	GOTO       L_main7
;final.c,80 :: 		if (PORTB.F1 == 1) {
	BTFSS      PORTB+0, 1
	GOTO       L_main8
;final.c,81 :: 		soThuTu = 0;
	CLRF       _soThuTu+0
	CLRF       _soThuTu+1
;final.c,82 :: 		update_flag = 1; // B?t c? d? c?p nh?t l?i màn hình
	BSF        _update_flag+0, BitPos(_update_flag+0)
;final.c,83 :: 		while(PORTB.F1 == 1);
L_main9:
	BTFSS      PORTB+0, 1
	GOTO       L_main10
	GOTO       L_main9
L_main10:
;final.c,84 :: 		}
L_main8:
;final.c,85 :: 		}
L_main6:
;final.c,86 :: 		}
	GOTO       L_main3
;final.c,87 :: 		}
L_end_main:
	GOTO       $+0
; end of _main
