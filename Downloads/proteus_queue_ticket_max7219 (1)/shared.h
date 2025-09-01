#pragma once
#include <EEPROM.h>

const int EE_TICKET  = 0;
const int EE_SERVING = 2;

inline void eepromWrite16(int addr, uint16_t val) {
  EEPROM.update(addr,   (uint8_t)(val & 0xFF));
  EEPROM.update(addr+1, (uint8_t)((val >> 8) & 0xFF));
}
inline uint16_t eepromRead16(int addr) {
  uint16_t lo = EEPROM.read(addr);
  uint16_t hi = EEPROM.read(addr+1);
  return (uint16_t)(lo | (hi<<8));
}