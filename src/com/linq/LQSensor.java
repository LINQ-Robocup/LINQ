package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CruizcoreGyro;
import lejos.nxt.addon.RCXLightSensor;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;

public class LQSensor {
	
	public final byte IRDIST_FL		= 0;
	public final byte IRDIST_FR		= 1;
	public final byte IRDIST_L		= 2;
	public final byte IRDIST_R		= 3;
	public final byte TEMP_L		= 4;
	public final byte TEMP_R		= 5;
	public final byte SRDIST		= 6;
	
	public final byte TOUCH_R		= 7;
	public final byte TOUCH_L		= 8;
	public final byte LIGHT			= 9;
	public final byte GYRO			= 10;
	public final byte ACCEL_X		= 11;
	public final byte ACCEL_Y		= 12;
	public final byte ACCEL_Z		= 13;
	
	public int irDistFRightValue;
	public int irDistRightValue;
	public int irDistLeftValue;
	public int irDistFLeftValue;
	public int tempRightValue;
	public int tempLeftValue;
	public int srValue;

	public RCXLightSensor light_left;
	public RCXLightSensor light_right;	//light sensor
	public CruizcoreGyro gyro;
	private NXTMotor servo;
	
	private byte send[] = new byte[2];
	private byte get[] = new byte[7];
	
	public LQSensor() {
		RS485.hsEnable(9600, 0);
		send[0] = 1;
		
		light_left = new RCXLightSensor(SensorPort.S2);
		light_left.setFloodlight(true);
		light_right = new RCXLightSensor(SensorPort.S1);
		light_right.setFloodlight(true);

		gyro = new CruizcoreGyro(SensorPort.S3);
		
		servo = new NXTMotor(MotorPort.C);
		servo.setPower(100);
		servo.forward();
		
		ledGreen(false);
		ledYellow(false);
		ledRed(false);
	}
	public void readAllSensors() {
		send[0] = 1;
		RS485.hsWrite(send, 0, 1);
		RS485.hsRead(get, 0, 7);
		irDistFRightValue	= get[0] -1;
		irDistRightValue	= get[1];
		irDistLeftValue		= get[2];
		irDistFLeftValue	= get[3];
		tempRightValue		= get[4];
		tempLeftValue		= get[5];
		srValue				= get[6];
		Delay.msDelay(10);
		
	}
	public void showAllSensors() {
		byte showValueOffset = 10;
		byte menu = 0;
		resetGyroValue();
		while (!Button.ENTER.isDown()) {
			if(Button.LEFT.isDown()) {
				if(menu > 0) {
					menu--;
					Delay.msDelay(250);
				}
			}else if(Button.RIGHT.isDown()) {
				if(menu < 3) {
					menu++;
					Delay.msDelay(250);
				}
			}
			if(Button.ESCAPE.isDown()) {
				gyro.reset();
			}
			readAllSensors();
			LCD.clear();
			switch (menu) {
			case 0:
				//FR 1 / R 2/  L 3 / FL 4
				LCD.drawString("IRDIST_FL", 0, 0);	LCD.drawInt(irDistFLeftValue, showValueOffset, 0);
				LCD.drawString("IRDIST_FR", 0, 1);	LCD.drawInt(irDistFRightValue, showValueOffset, 1);
				LCD.drawString("IRDIST_L", 0, 2);	LCD.drawInt(irDistLeftValue, showValueOffset, 2);
				LCD.drawString("IRDIST_R", 0, 3);	LCD.drawInt(irDistRightValue, showValueOffset, 3);
				LCD.drawString("TEMP_R", 0, 4);		LCD.drawInt(tempRightValue, showValueOffset, 4);
				LCD.drawString("TEMP_L", 0, 5);		LCD.drawInt(tempLeftValue, showValueOffset, 5);
				LCD.drawString("SR", 0, 6); 		LCD.drawInt(srValue, showValueOffset, 6);
				break;
			case 1:
				LCD.drawString("TOUCH_L", 0, 0);	LCD.drawString(isLeftTouchPressed() ? "true" : "false", showValueOffset, 0);
				LCD.drawString("TOUCH_R", 0, 1);	LCD.drawString(isRightTouchPressed() ? "true" : "false", showValueOffset, 1);
				LCD.drawString("LIGHT"	, 0, 2);	LCD.drawInt(light_right.getLightValue(), showValueOffset, 2);
				LCD.drawString("GYRO"	, 0, 3);	LCD.drawInt(getGyroValue(), showValueOffset, 3);
				LCD.drawString("ACCEL_X", 0, 4);	LCD.drawInt(getAccelXValue(), showValueOffset, 4);
				LCD.drawString("ACCEL_Y", 0, 5);	LCD.drawInt(getAccelYValue(), showValueOffset, 5);
				LCD.drawString("ACCEL_Z", 0, 6);	LCD.drawInt(getAccelZValue(), showValueOffset, 6);
				break;
			case 2:
				LCD.drawString("isWallR", 0, 0);	LCD.drawInt(isWallRight() ? 1 : 0, showValueOffset, 0);
				LCD.drawString("isWallL", 0, 1);	LCD.drawInt(isWallLeft()  ? 1 : 0, showValueOffset, 1);
				LCD.drawString("isWallF", 0, 2);	LCD.drawInt(isWallFront() ? 1 : 0, showValueOffset, 2);
			default:
				break;
			}
			Delay.msDelay(20);
			
			
		}
		LCD.clear();
	}
	public int getValue(byte sensor) {
		readAllSensors();
		switch (sensor) {
		case IRDIST_R:
			return irDistRightValue;
		case IRDIST_FR:
			return irDistFRightValue;
		case IRDIST_L:
			return irDistLeftValue;
		case IRDIST_FL:
			return irDistFLeftValue;
		case GYRO:
			return gyro.getAngle();
		case TEMP_L:
			return tempLeftValue;
		case TEMP_R:
			return tempRightValue;
		case SRDIST:
			return srValue;
		default:
			break;
		}
		
		return -1;
	}
	public int getLightValue() {
		return light_right.getLightValue();
	}
	public boolean isLeftTouchPressed() {
		return light_left.getLightValue() > 90 ? true : false;
	}

	public boolean isRightTouchPressed() {
		return light_right.getLightValue() > 90 ? true : false;
	}
	public int getGyroValue() {
		return gyro.getAngle();
	}
	
	public void resetGyroValue() {
		gyro.reset();
	}
	public int getAccelXValue() {
		return gyro.getAccel(0);
	}
	public int getAccelYValue() {
		return gyro.getAccel(1);
	}
	public int getAccelZValue() {
		return gyro.getAccel(2);
	}
	
	public boolean isWallLeft() {
		int threshold = 100;
		readAllSensors();
//		if(getValue(IRDIST_L) < threshold) {
		if(irDistLeftValue < threshold) {
			return true;
		}else{
			return false;
		}
	}
	public boolean isWallRight() {
		int threshold = 100;
		readAllSensors();
//		if(getValue(IRDIST_R) < threshold) {
		if(irDistRightValue < threshold) {
			return true;
		}else{
			return false;
		}
	}
	public boolean isWallFront() {
		int threshold = 20;
//		if(getValue(SRDIST) < threshold) {
		readAllSensors();
		if(srValue < threshold) {
			return true;
		}else{
			return false;
		}
	}
	public void servoLeft() {
		send[0] = 12;
		RS485.hsWrite(send, 0, 1);
		Delay.msDelay(1000);
	}
	public void servoRight() {
		send[0] = 11;
		RS485.hsWrite(send, 0, 1);
		Delay.msDelay(1000);
	}
	public void resetServo() {
		send[0] = 13;
		RS485.hsWrite(send, 0, 1);
		Delay.msDelay(10);
	}
	public void ledGreen(boolean toggle) {
		if(toggle) {
			send[0] = 14;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}else{
			send[0] = 15;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void ledYellow(boolean toggle) {
		if(toggle) {
			send[0] = 16;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}else{
			send[0] = 17;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void ledRed(boolean toggle) {
		if(toggle) {
			send[0] = 18;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}else{
			send[0] = 19;
			RS485.hsWrite(send, 0, 1);
			Delay.msDelay(10);
		}
	}
	public void blinkLED() {
		send[0] = 10;
		RS485.hsWrite(send, 0, 1);	
		Delay.msDelay(5000);
	}
}
