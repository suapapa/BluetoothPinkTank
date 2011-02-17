#!/bin/bash

# Run this script with sudo

avrdude -v -patmega8 -cusbtiny -e -Ulock:w:0x3F:m -Uhfuse:w:0xca:m -Ulfuse:w:0xdf:m
avrdude -v -patmega8 -cusbtiny -U flash:w:ATmegaBOOT.hex -U lock:w:0x0f:m
