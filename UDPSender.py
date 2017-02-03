# Source: https://pymotw.com/2/socket/udp.html

import socket, sys, time
import RPi.GPIO as io
import sys


io.setmode(io.BCM)
io.setup(23,io.IN,pull_up_down=io.PUD_UP)
io.setup(24,io.IN,pull_up_down=io.PUD_UP)
io.setup(25,io.IN,pull_up_down=io.PUD_UP)


host = sys.argv[1]
textport = sys.argv[2]

s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
port = int(textport)
server_address = (host, port)

while 1:
    b1=io.input(23)
    b2=io.input(24)
    b3=io.input(25)
    b4=io.input(26)

    data = bytearray([0]*100)

    if b1!=1:
        data[7] = 1
        #print ("Enter data to transmit: ENTER to quit")
        #data = sys.stdin.readline().strip()
        #if not len(data):
        #break       
    if b2!=1:
        data[6] = 1
    if b3!=1:
        data[5] = 1
        data[7] = 1
    if b4!=1:
        data[4] = 1
    
    #print ("Enter data to transmit: ENTER to quit")
    #data = sys.stdin.readline().strip()
    #if not len(data):
    #break
#    s.sendall(data.encode('utf-8'))
    s.sendto(data.encode('utf-8'), server_address)
    time.sleep(1)

s.shutdown(1)