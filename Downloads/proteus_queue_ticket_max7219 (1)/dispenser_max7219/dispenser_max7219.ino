#include <LiquidCrystal.h>
#include "shared.h"

// LCD: RS, EN, D4, D5, D6, D7
LiquidCrystal lcd(7, 6, 5, 4, 3, 2);

const uint8_t PIN_TAKE  = 8;
const uint8_t PIN_BUZZ  = 10;

uint16_t ticket  = 0;
uint16_t serving = 0;

bool readButton(uint8_t pin) { return digitalRead(pin) == LOW; }

void beep(uint16_t dur = 30, uint16_t freq = 2200) {
  pinMode(PIN_BUZZ, OUTPUT);
  tone(PIN_BUZZ, freq, dur);
}

String fmt3(uint16_t v) {
  if (v > 999) v = 0;
  char buf[5];
  snprintf(buf, sizeof(buf), "%03u", (unsigned)v);
  return String(buf);
}

void drawLCD() {
  lcd.setCursor(0,0);
  lcd.print("Take Ticket: ");
  lcd.print(fmt3(ticket));
  int len1 = 16 - (13 + 3);
  if (len1 > 0) for (int i=0;i<len1;i++) lcd.print(' ');

  lcd.setCursor(0,1);
  lcd.print("Now Serving: ");
  lcd.print(fmt3(serving));
  int len2 = 16 - (13 + 3);
  if (len2 > 0) for (int i=0;i<len2;i++) lcd.print(' ');
}

// === MAX7219 add-on (shows latest ticket on 7-seg) ===

// === MAX7219 minimal driver (no external libs) ===
// Pins (change if needed)
const uint8_t MAX_DIN = 11;
const uint8_t MAX_CLK = 13;
const uint8_t MAX_CS  = 12;

// Send one register
void max_send(uint8_t addr, uint8_t data) {
  digitalWrite(MAX_CS, LOW);
  shiftOut(MAX_DIN, MAX_CLK, MSBFIRST, addr);
  shiftOut(MAX_DIN, MAX_CLK, MSBFIRST, data);
  digitalWrite(MAX_CS, HIGH);
}

// Initialize for 3 digits (scan 0..2), Code-B decode
void max_init() {
  pinMode(MAX_DIN, OUTPUT);
  pinMode(MAX_CLK, OUTPUT);
  pinMode(MAX_CS,  OUTPUT);
  digitalWrite(MAX_CS, HIGH);

  max_send(0x0F, 0x00); // Display test off
  max_send(0x09, 0xFF); // Decode mode: Code B for all digits
  max_send(0x0A, 0x04); // Intensity (0x00..0x0F)
  max_send(0x0B, 0x02); // Scan limit: digits 0..2 (3 digits)
  max_send(0x0C, 0x01); // Shutdown = normal operation
  // Clear all 8 in case different hardware
  for (uint8_t i=1;i<=8;i++) max_send(i, 0x0F); // 0x0F = blank in Code-B
}

// Show a 0..999 number right-aligned on digits 0..2
void max_show3(uint16_t v) {
  if (v > 999) v = 0;
  uint8_t hundreds = (v/100) % 10;
  uint8_t tens     = (v/10)  % 10;
  uint8_t ones     = v % 10;

  // Rightmost is digit 0 => Register 1
  // Write ones->reg1, tens->reg2, hundreds->reg3
  max_send(1, ones);
  if (v >= 10)  max_send(2, tens);     else max_send(2, 0x0F);
  if (v >= 100) max_send(3, hundreds); else max_send(3, 0x0F);

  // Blank others just in case
  for (uint8_t i=4;i<=8;i++) max_send(i, 0x0F);
}

void setup() {
  pinMode(PIN_TAKE, INPUT_PULLUP);
  pinMode(PIN_BUZZ, OUTPUT);

  lcd.begin(16,2);
  lcd.clear();
  Serial.begin(9600);

  // Load EEPROM
  ticket  = eepromRead16(EE_TICKET);
  serving = eepromRead16(EE_SERVING);
  if (ticket  > 999) ticket  = 0;
  if (serving > 999) serving = 0;

  drawLCD();

  // Init MAX7219 7-seg
  max_init();
  max_show3(ticket);
}

void loop() {
  // Handle take button
  static bool prev = false;
  bool now = readButton(PIN_TAKE);
  if (now && !prev) {
    delay(50);
    if (readButton(PIN_TAKE)) {
      ticket++;
      if (ticket > 999) ticket = 1;
      eepromWrite16(EE_TICKET, ticket);
      drawLCD();
      max_show3(ticket);
      beep();

      // Broadcast to peer
      Serial.print('T');
      char buf[4];
      snprintf(buf, sizeof(buf), "%03u", (unsigned)ticket);
      Serial.println(buf);
    }
  }
  prev = now;

  // Receive peer updates (serving from counter)
  while (Serial.available()) {
    char type = Serial.read();
    if (type == 'S') {
      char d0 = Serial.read();
      char d1 = Serial.read();
      char d2 = Serial.read();
      serving = (d0-'0')*100 + (d1-'0')*10 + (d2-'0');
      while (Serial.available()) {
        if (Serial.read()=='\n') break;
      }
      eepromWrite16(EE_SERVING, serving);
      drawLCD();
      // 7-seg stays showing ticket on dispenser
    }
  }
}