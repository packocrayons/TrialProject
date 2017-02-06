//Author : Brydon Gibson (with some help from the internet)
/*This is a class to control a servo on a raspberry pi 3. It should work on a rpi2 but it is untested.
Connect the signal pin (the white/light brown wire) to the GPIO pin sent to the constructor of this class. Pinouts for the RPi can be found online.
Make sure to power the servo appropriately, most don't have reverso polarity protection and most cannot handle more than 5 volts.

A servo works by reading the length of a rectangular pulse, at 50Hz. A 2000us high pulse moves the servo all the way one way, and a 1000us pulse moves it all the way the other way.
Due to rounding (PULSE_RATIO), the servo does not actually get a 2000us pulse at 180*. Most servos don't actually move a full 180* either.
You can send most servos down to 900us and up to about 2100us, and they might move a little further.
If the servo is shaking a lot, please shut it off, they have no temperature protection and might melt themselves.
*/

/*Note : this class was intended to be run in its own thread, and should be started as such. Example:
Servo myServo = new Servo("24");
new Thread(myServo).start();
myServo.increment_angle();
... etc

Be careful when adding things to this class, long prints or long operations on this thread (in the functions) will cause the servo to shake and be inaccurate (see PWM jitter).
It is best to leave this thread as is and do most operations on another thread.
*/

import java.io.FileWriter;
import java.io.File;

import java.lang.Runnable;
import java.lang.Thread;

import java.io.IOException;
import java.lang.InterruptedException;

class Servo implements Runnable {
	
	public static final int PULSE_RATIO = (1000/180);
	public static final String GPIO_HIGH="1";
	public static final String GPIO_LOW ="0";
	public static final int PULSE_PERIOD = 20; //20 milliseconds = 50 pulses/second

	private int angle; //approximate - PULSE_RATIO splits 1000 pulses into a resolution of 180, servos don't always move exactly 180
	private String channel;
	private int pulseWidth; //in microseconds, the high time of the pulse

	public Servo(String gPIOChannel){ //constructor
		this(gPIOChannel, 0);
	}

	public Servo(String gPIOChannel, int initialAngle){
		angle = initialAngle;
		FileWriter unexportFile = null;
		try{
			unexportFile= new FileWriter("/sys/class/gpio/unexport");
		} catch (IOException e){
			System.err.println("Can't open unexport file : " + e);
		}
		FileWriter exportFile = null;									//setup stuff, most of this doesn't need to be touched
		try{
			exportFile= new FileWriter("/sys/class/gpio/export");	
		} catch (IOException e){
			System.err.println("Can't open export file : " + e);
		}
		System.out.println("Setting up a servo on GPIO " + gPIOChannel);
		File exportFileCheck = new File("/sys/class/gpio/gpio" + gPIOChannel);
		if (exportFileCheck.exists()){
			tryWriteAndFlush(unexportFile, gPIOChannel);
		}
		tryWriteAndFlush(exportFile, gPIOChannel);

		FileWriter directionFile = null;
		try{
			directionFile = new FileWriter("/sys/class/gpio/gpio" + gPIOChannel + "/direction");
		} catch (IOException e){
			System.out.println("Error opening diretion file : " + e);
		}
		tryWriteAndFlush(directionFile, "out");
		channel = gPIOChannel;
	}

	public void run(){
		long t;
		FileWriter commandChannel = null;
		try{
			commandChannel = new FileWriter("/sys/class/gpio" + channel + "/value");
		} catch (IOException e){
			System.out.println("Error opening commandChannel : " + e);
		}
		while(true){ //this is the main loop that controls the servo.
			pulseWidth = (angle*PULSE_RATIO) + 1000;
			tryWriteAndFlush(commandChannel, GPIO_HIGH);
			t = System.nanoTime();
			while((System.nanoTime() - t) < (pulseWidth * 1000)); //busy wait, best way to get microsec accuracy
			tryWriteAndFlush(commandChannel, GPIO_LOW); //send the pin low

			try{
				Thread.sleep(PULSE_PERIOD - (pulseWidth/1000)); //sleep for ~20 milliseconds. Accuracy is not required here.
			} catch (InterruptedException e){
				System.out.println("Thread interrupted : " + e);
			}
		}

	}

	public synchronized void set_angle(int newAngle){
		if (newAngle < 180 && newAngle > 0) angle = newAngle;
	}

	public synchronized void inc_angle(){
		angle++;
	}

	public synchronized void dec_angle(){
		angle--;
	}

	public synchronized void set_pulseWidth(int newp){
		if (newp > 1000 && newp < 2000)	angle = newp/PULSE_RATIO;
	}

	private void tryWriteAndFlush(FileWriter f, String s){
		try{
			f.write(s);
			f.flush();
		} catch (IOException e){
			System.out.println("Error writing to file : " + e);
		}
	}

}