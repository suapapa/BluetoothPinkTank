#!/usr/bin/python

import serial
import time
import sys

if len(sys.argv) != 2:
    print "Please specify a port, e.g. %s /dev/ttyUSB0" % sys.argv[0]
    sys.exit(-1)

ser = serial.Serial(sys.argv[1], baudrate=38400)
ser.write('#RST')
ser.close()

