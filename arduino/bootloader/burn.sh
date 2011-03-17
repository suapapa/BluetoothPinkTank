#!/bin/bash
# Run this script with sudo

# ATMEGA8L
BINARY=ATmegaBOOT-8L.hex
HFUSE=0xCA
LFUSE=0xD4

# ATMEGA8
#BINARY=ATmegaBOOT.hex
#HFUSE=0xCA
#LFUSE=0xDF

AVRDUDE_OPTS="-cstk500 -pm8 -P/dev/ttyUSB0 -b115200"
#AVRDUDE_OPTS="-cstk200 -pm8 -P/dev/parport1"

avrdude $AVRDUDE_OPTS -e -u -Ulock:w:0x3f:m -Uefuse:w:0x00:m -Uhfuse:w:$HFUSE:m -Ulfuse:w:$LFUSE:m
avrdude $AVRDUDE_OPTS -Uflash:w:$BINARY -Ulock:w:0x0f:m

# FYI: http://www.arduino.cc/playground/Learning/Atmega83-3V
# FYI : http://www.avrfreaks.net/index.php?name=PNphpBB2&file=printview&t=96696&start=0
