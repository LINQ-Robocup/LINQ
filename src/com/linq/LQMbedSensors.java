package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;

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

	public LQMbedSensors() {
		RS485.hsEnable(9600, 0);
	}
	
	public void readMbedSensorsValue () {
//		do {
//			readAllSensors();
//		} while(dummyValue != errorValue);
		Delay.msDelay(1);
		readAllSensors();
		if(dummyValue != errorValue) {
//			Sound.beep();
			readAllSensors();
		}
	}
	public void readSonicValue() {
		_readSonicValue();
		if(dummyValue != errorValue) {
			Sound.beep();
			_readSonicValue();
		}
	}
	public void _readSonicValue() {
		send[0] = 2;
		RS485.hsWrite(send, 0, 1);
		RS485.hsRead(get, 0, 2);
		sonicValue = get[0];
		dummyValue = get[1];
	}
	
	private void readAllSensors() {
		send[0] = 1;
		RS485.hsWrite(send, 0, 1);
		RS485.hsRead(get, 0, 7);
		distFrontLeftValue	= get[0];
		distFrontRightValue		= get[1];
		distLeftValue = get[2];
		distRightValue	= get[3];
		tempLeftValue		= get[4];
		tempRightValue		= get[5];
		dummyValue			= get[6];
	}
	
	public void toggleLed1(boolean sw) {
		if(sw == true) {
			send[0] = 20;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}else{
			send[0] = 21;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void toggleLed2(boolean sw) {
		if(sw == true) {
			send[0] = 22;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}else{
			send[0] = 23;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void toggleLed3(boolean sw) {
		if(sw == true) {
			send[0] = 24;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}else{
			send[0] = 25;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void rotateServoLeft() {
		send[0] = 30;
		RS485.hsWrite(send, 0, 1);
		Delay.msDelay(10);	
	}
	public void rotateServoRight() {
		send[0] = 31;
		RS485.hsWrite(send, 0, 1);
		Delay.msDelay(10);	
	}
	public void showSensorValues() {
		LCD.drawString("FL", 0, 0); LCD.drawInt(distFrontLeftValue, 10, 0);
		LCD.drawString("FR", 0, 1); LCD.drawInt(distFrontRightValue, 10, 1);
		LCD.drawString("L", 0, 2); LCD.drawInt(distLeftValue, 10, 2);
		LCD.drawString("R", 0, 3); LCD.drawInt(distRightValue, 10, 3);
		LCD.drawString("TL", 0, 4); LCD.drawInt(tempLeftValue, 10, 4);
		LCD.drawString("TR", 0, 5); LCD.drawInt(tempRightValue, 10, 5);
		LCD.drawString("U", 0, 6); LCD.drawInt(sonicValue, 10, 6);
		LCD.drawString("DUMMY", 0, 7); LCD.drawInt(dummyValue, 10, 7);
		Delay.msDelay(50);
		LCD.clear();
	}
}