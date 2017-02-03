# Source: https://pymotw.com/2/socket/udp.html

import socket, sys, time
import pifacedigitalio as pfio

pfio.init();



host = sys.argv[1]
textport = sys.argv[2]

s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
port = int(textport)
server_address = (host, port)
flag = False;

while 1:
    b1=pfio.digital_read(0)
    b2=pfio.digital_read(1)
    b3=pfio.digital_read(2)
    b4=pfio.digital_read(3)

    data = bytearray([0]*100)

    flag = False
    if b1==1:
        data[0] = '2'
	flag = True
    if b2==1:
        data[0] = '3'
	flag = True
    if b3==1:
        data[0] = '4'
	flag = True
    if b4==1:
	data[0] = '5'
	flag = True
    
    if flag == True:
    	s.sendto(data, server_address)
    time.sleep(0.25)

s.shutdown(1)
