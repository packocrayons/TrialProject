import java.net.*;
import java.util.Arrays;

/*
Packet Structure:
[Opcode][Args][Pad to 100 bytes]
***************OPCODES*****************
The opcode is the first (and maybe only) byte of the packet.
'0' - ignore/testing packet
'1' - Set angle - MUST BE 3 DIGITS
'2' - Increment angle
'3' - Decrement angle
'4' - Set pulseWidth
'5' - Create a servo?
*/

public class UDPReceiver {

	private final static int PACKETSIZE = 100 ;

	public static int byteArrToInt(byte[] arr){
		int ret = 0;
		for (int i = 0; i < arr.length; ++i) {
			ret = ret * 10;
			ret += arr[i];
		}
		return ret;
	}

	public static void main( String args[] )
	{ 
	    // Check the arguments
	    if( args.length != 1 )
	    {
	        System.out.println( "usage: UDPReceiver port" ) ;
	        return ;
	    }
		
		Servo s = new Servo("24"); //what the tutorial I saw used as the GPIO pin, not sure what this translates tool
		new Thread(s).start();
	    
	    try
	    {
	        // Convert the argument to ensure that is it valid
	        int port = Integer.parseInt( args[0] ) ;

	        // Construct the socket
	        DatagramSocket socket = new DatagramSocket( port ) ;

	        for( ;; )
	        {
			    System.out.println( "Receiving on port " + port ) ;
		        DatagramPacket packet = new DatagramPacket( new byte[PACKETSIZE], PACKETSIZE ) ;
	            socket.receive( packet ) ;

	            byte[] data = packet.getData();

	            String msg = new String(data).trim();

	            System.out.println( packet.getAddress() + " " + packet.getPort() + ": " + msg ) ;

	            switch (data[0]) {
	            	case '0' : break; //drop testing packets
	            	case '1' : int newAngle = byteArrToInt(Arrays.copyOfRange(data, 0, 3)); //send the first 3 digits to get back an int
	            	s.set_angle(newAngle);
			break;
			case '2' : s.inc_angle();
			break;
			case '3' : s.dec_angle();
			break;
			case '4' : s.set_angle(179);
			break; //FIX THIS CASE - DEBUG/TESTING ONLY
			case '5' : s.set_angle(1);
			break; //FIX THIS CASE - DEBUG/TESTING ONLY
			case '6' : s.print_debug();
			break;
	            	default : break;
	            }

	        }  
	    }
	    catch( Exception e )
	    {
	        System.out.println( e ) ;
	    }
	}
}

