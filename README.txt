Packet Structure:
(Opcode)(Args)(Pad to 100 bytes)

[OPCODES]
The opcode is the first (and maybe only) byte of the packet.
'0' - ignore/testing packet
'1' - Set angle - MUST BE 3 DIGITS
'2' - Increment angle
'3' - Decrement angle
'4' - Set angle to 179 degrees
'5' - Set angle to 0 degrees
'6' - Print some debug info about pulseWidth

The UDP receiver interprets packets in this form, and updates the servo
At the moment, the receiver (which posesses the main) has one servo on GPIO 24.

The UDP receiver listens on port 1077

changed something in the readme
