#!/bin/bash

# Run this script with sudo
# FYI: http://www.arduino.cc/playground/Learning/Atmega83-3V
avrdude -v -patmega8 -cusbtiny -e -Ulock:w:0x3F:m -Uhfuse:w:0xca:m -Ulfuse:w:0xd4:m
avrdude -v -patmega8 -cusbtiny -U flash:w:ATmegaBOOT-8L.hex -U lock:w:0x0f:m
