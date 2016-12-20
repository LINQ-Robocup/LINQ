package com.linq;

import javax.microedition.sensor.GyroChannelInfo;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.LegacySensorPort;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.TouchSensor;
import lejos.nxt.addon.CruizcoreGyro;
import lejos.nxt.addon.RCXLightSensor;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;

public class LQSensor {
	
	public final int IRDIST_FL		= 0;
	public final int IRDIST_FR		= 1;
	public final int IRDIST_L		= 2;
	public final int IRDIST_R		= 3;
	public final int TEMP_L			= 4;
	public final int TEMP_R			= 5;
	public final int SRDIST			= 6;
	
	public final int TOUCH_R		= 7;
	public final int TOUCH_L		= 8;
	public final int LIGHT			= 9;
	public final int GYRO			= 10;
	public final int ACCEL_X		= 11;
	public final int ACCEL_Y		= 12;
	public final int ACCEL_Z		= 13;
	
	public int irDistFRightValue;
	public int irDistRightValue;
	public int irDistLeftValue;
	public int irDistFLeftValue;
	public int tempRightValue;
	public int tempLeftValue;
	public int srValue;

	private byte send[] = new byte[2];
	private byte get[] = new byte[7];
	
	public RCXLightSensor light_left;
	public RCXLightSensor light_right;	//light sensor

	public CruizcoreGyro gyro;
	private NXTMotor servo;
	
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
	public int getValue(int sensor) {
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
		case TOUCH_R:
			return light_right.getLightValue() > 90 ? 1 : 0;
		case TOUCH_L:
			return light_left.getLightValue() > 90 ? 1 : 0;
		case LIGHT:
			return light_right.getLightValue();
		case GYRO:
			return gyro.getAngle();
		case TEMP_L:
			return tempLeftValue;
		case TEMP_R:
			return tempRightValue;
		default:
			break;
		}
		
		return -1;
	}
	public int isLeftTouchPressed() {
		return light_left.getLightValue() > 90 ? 1 : 0;
	}

	public int isRightTouchPressed() {
		return light_right.getLightValue() > 90 ? 1 : 0;
	}
	public void showAllSensors() {
		int showValueOffset = 10;
		int menu = 0;
		
		while (!Button.ENTER.isDown()) {
			if(Button.LEFT.isDown()) {
				if(menu > 0) {
					menu--;
					Delay.msDelay(250);
				}
			}else if(Button.RIGHT.isDown()) {
				if(menu < 2) {
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
				LCD.drawString("TOUCH_L", 0, 0);	LCD.drawInt(isLeftTouchPressed(), showValueOffset, 0);
				LCD.drawString("TOUCH_R", 0, 1);	LCD.drawInt(isRightTouchPressed(), showValueOffset, 1);
				LCD.drawString("LIGHT"	, 0, 2);		LCD.drawInt(light_right.getLightValue(), showValueOffset, 2);
				LCD.drawString("GYRO"	, 0, 3);		LCD.drawInt(getGyroValue(), showValueOffset, 3);
				LCD.drawString("ACCEL_X", 0, 4);	LCD.drawInt(getAccelXValue(), showValueOffset, 4);
				LCD.drawString("ACCEL_Y", 0, 5);	LCD.drawInt(getAccelYValue(), showValueOffset, 5);
				LCD.drawString("ACCEL_Z", 0, 6);	LCD.drawInt(getAccelZValue(), showValueOffset, 6);
				break;
				
			default:
				break;
			}
			Delay.msDelay(20);
			
			
		}
		LCD.clear();
	}
	public void blinkLED() {
		send[0] = 10;
		RS485.hsWrite(send, 0, 1);	
	}
	public int getGyroValue() {
		return gyro.getAngle();
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
	
	public void resetGyroValue() {
		gyro.reset();
	}
	
	public void rotateServo() {
			send[0] = 10;
			send[0] = 10;
		RS485.hsWrite(send, 0, 1);
		Delay.msDelay(10);
	}
}
