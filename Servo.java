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

	public Servo(String gPIOChannel){
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
		FileWriter exportFile = null;
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
			commandChannel = new FileWriter("/sys/class/gpio/gpio" + channel + "/value");
		} catch (IOException e){
			System.out.println("Error opening commandChannel : " + e);
		}
		while(true){
			pulseWidth = (angle*PULSE_RATIO) + 1000;
			tryWriteAndFlush(commandChannel, GPIO_HIGH);
			t = System.nanoTime();
			while((System.nanoTime() - t) < (pulseWidth * 1000)){
				//debug remnant brackets //busy wait, best way to get microsec accuracy
			}
			tryWriteAndFlush(commandChannel, GPIO_LOW); //send the pin low

			try{
				Thread.sleep(PULSE_PERIOD - (pulseWidth/1000)); //sleep for ~20 milliseconds. Accuracy is not required here.
			} catch (InterruptedException e){
				System.out.println("Thread interrupted : " + e);
			}
		}

	}

	public synchronized void print_debug(){
		System.out.println("angle : " + angle);
		System.out.println("Pulsewidth : " + pulseWidth);
	}

	public synchronized void set_angle(int newAngle){
		System.out.println("Setting angle to" + newAngle);
		if (angle < 180 && angle > 0) angle = newAngle;
	}

	public synchronized void inc_angle(){
		System.out.println("Incrementing angle");
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
