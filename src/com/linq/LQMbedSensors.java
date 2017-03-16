package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.Sound;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;
import lejos.util.LogColumn;
import lejos.util.Stopwatch;

public class LQMbedSensors {
	
	private byte send[] = new byte[2];
	private byte get[] = new byte[8];
	
	public byte distFrontLeftValue	= 0;
	public byte distFrontRightValue	= 0;
	public byte distLeftValue		= 0;
	public byte distRightValue		= 0;
	
	public byte tempLeftValue		= 0;
	public byte tempRightValue		= 0;
	
	public byte sonicValue			= 0;
	public byte dummyValue 			= 0;
	private final byte errorValue 	= 111;

	private NXTMotor servo;

	public LQMbedSensors() {
		RS485.hsEnable(9600, 0);
		this.servo = new NXTMotor(MotorPort.C);
		this.servo.setPower(0);
		this.servo.stop();
	}
	
	public void readAllSensors () {
//		do {
//			readAllSensors();
//		} while(dummyValue != errorValue);
		this._readAllSensors();
		while(this.dummyValue != this.errorValue) {
			Sound.beep();
			this._readAllSensors();
			Delay.msDelay(10);
		}
	}	
	private void _readAllSensors() {
		this.send[0] = 1;
		RS485.hsWrite(this.send, 0, 1);
		RS485.hsRead(this.get, 0, 8);
		this.distFrontLeftValue	= this.get[0];
		this.distFrontRightValue= this.get[1];
		this.distLeftValue 		= this.get[2];
		this.distRightValue		= this.get[3];
		this.tempLeftValue		= this.get[4];
		this.tempRightValue		= this.get[5];
		this.sonicValue			= this.get[6];
		this.dummyValue			= this.get[7];
	}

	public void readSonicSensor() {
		this.send[0] = 2;
		RS485.hsWrite(this.send, 0, 1);
		Delay.msDelay(1000);
		this._readAllSensors();
		while(this.dummyValue != this.errorValue || this.sonicValue == -1) {
			Sound.beep();
			showSensorValues();
			this._readAllSensors();
		}
	}
//	public void disableSonicSensor() {
//		this.send[0] = 3;
//		RS485.hsWrite(this.send, 0, 1);
//		Delay.msDelay(10);
//	}
	public void toggleLedBlue(boolean sw) {
		if(sw == true) {
			this.send[0] = 20;
			RS485.hsWrite(this.send, 0, 1);
			Delay.msDelay(10);
		}else{
			this.send[0] = 21;
			RS485.hsWrite(this.send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void toggleLedGreen(boolean sw) {
		if(sw == true) {
			this.send[0] = 22;
			RS485.hsWrite(this.send, 0, 1);
			Delay.msDelay(10);
		}else{
			this.send[0] = 23;
			RS485.hsWrite(this.send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void toggleLedYellow(boolean sw) {
		if(sw == true) {
			this.send[0] = 24;
			RS485.hsWrite(this.send, 0, 1);
			Delay.msDelay(10);
		}else{
			this.send[0] = 25;
			RS485.hsWrite(this.send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void rotateServo() {
		this.servo.setPower(100);
		this.servo.forward();
		this.send[0] = 30;
		RS485.hsWrite(this.send, 0, 1);
		Delay.msDelay(900);
		this.servo.setPower(0);
		this.servo.stop();
	}
	public void showSensorValues() {
		Delay.msDelay(50);
		LCD.clear();
		LCD.drawString("FL", 0, 0); LCD.drawInt(this.distFrontLeftValue, 10, 0);
		LCD.drawString("FR", 0, 1); LCD.drawInt(this.distFrontRightValue, 10, 1);
		LCD.drawString("L", 0, 2); LCD.drawInt(this.distLeftValue, 10, 2);
		LCD.drawString("R", 0, 3); LCD.drawInt(this.distRightValue, 10, 3);
		LCD.drawString("TL", 0, 4); LCD.drawInt(this.tempLeftValue, 10, 4);
		LCD.drawString("TR", 0, 5); LCD.drawInt(this.tempRightValue, 10, 5);
		LCD.drawString("U", 0, 6); LCD.drawInt(this.sonicValue, 10, 6);
		LCD.drawString("DUMMY", 0, 7); LCD.drawInt(this.dummyValue, 10, 7);
	}
	public void debugAllSensors() {
		while(Button.ESCAPE.isDown());
		
		while (!Button.ESCAPE.isDown()) {
			if(Button.ENTER.isDown()) {
				this.readSonicSensor();
				Sound.playTone(440, 500);
			}else{
				this.readAllSensors();
				this.showSensorValues();	
			}
		}
	}
/*===========================================================================*/	

	public long calcReadingSpeed(int count) {
		Stopwatch timer = new Stopwatch();
		timer.reset();
		for(int i = 0; i < count; i++) {
			this.readAllSensors();
		}
		long time = timer.elapsed();
		return time / count;
	}
	/*===========================================================================*/	
	public void debugServo() {
		while (Button.ESCAPE.isDown());
		
		LCD.clear();
		LCD.drawString("SERVO: ENTER", 2, 1);
		LCD.drawString("EXIT: ESCAPE", 2, 5);
		
		while(!Button.ESCAPE.isDown()) {
			if(Button.ENTER.isDown()) {
				LCD.drawString("ROTATING", 2, 2);
				this.rotateServo();
				LCD.clear(2);
				LCD.drawString("END", 2, 2);
			}
		}
	}
	public void debugLeds() {
		boolean debugledBlue = false;
		boolean debugledGreen = false;
		boolean debugledYellow = false;
		
		int pointer = 1;
		while(!Button.ESCAPE.isDown()) {
			LCD.clear();
			LCD.drawString("BLUE", 10, 1); 
			LCD.drawString("GREEN", 10, 2);
			LCD.drawString("YELLOW", 10, 3);
			LCD.drawString("EXIT: ESCAPE", 2, 5);
			
			LCD.drawString(">", 1, pointer);
			
			
			if(Button.LEFT.isDown()) {
				if(pointer > 1) {
					pointer--;
					Delay.msDelay(250);
				}
			}else if(Button.RIGHT.isDown()) {
				if(pointer < 3) {
					pointer++;
					Delay.msDelay(250);
				}
			}
			
			if(debugledBlue) {
				LCD.drawString("ON", 5, 1);
				this.toggleLedBlue(true);
			}else{
				LCD.drawString("OFF", 5, 1);
				this.toggleLedBlue(false);
			}
			if(debugledGreen) {
				LCD.drawString("ON", 5, 2);
				this.toggleLedGreen(true);
			}else{
				LCD.drawString("OFF", 5, 2);
				this.toggleLedGreen(false);
			}
			if(debugledYellow) {
				LCD.drawString("ON", 5, 3);
				this.toggleLedYellow(true);
			}else{
				LCD.drawString("OFF", 5, 3);
				this.toggleLedYellow(false);
			}
			
			if(Button.ENTER.isDown()) {
				switch (pointer) {
				case 1:
					debugledBlue = !debugledBlue;
					break;
				case 2:
					debugledGreen = !debugledGreen;
					break;
				case 3:
					debugledYellow = !debugledYellow;
					break;

				default:
					break;
				}
				Delay.msDelay(250);
			}
			Delay.msDelay(50);
		}
	}
}