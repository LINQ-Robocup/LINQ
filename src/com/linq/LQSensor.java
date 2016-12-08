package com.linq;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CruizcoreGyro;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;

public class LQSensor {
	private byte send[] = new byte[2];
	private byte get[] = new byte[7];
	
	public final int IRDIST_RIGHT	= 0;
	public final int IRDIST2	= 1;
	public final int IRDIST3	= 2;
	public final int IRDIST_LEFT	= 3;
	public final int TEMP1		= 4;
	public final int TEMP2		= 5;
	public final int SRDIST		= 6;
	
	private int irDist1Value;
	private int irDist2Value;
	private int irDist3Value;
	private int irDist4Value;
	private int temp1Value;
	private int temp2Value;
	private int srValue;
	
	public CruizcoreGyro gyro;
	
	public LQSensor() {
		RS485.hsEnable(9600, 0);
		send[0] =1;
		gyro = new CruizcoreGyro(SensorPort.S3);

		//servo
		NXTMotor servo = new NXTMotor(MotorPort.C);
	}
	public void readAllSensors() {
		send[0] = 1;
		RS485.hsWrite(send, 0, 1);
		RS485.hsRead(get, 0, 7);
		irDist1Value	= get[0];
		irDist2Value	= get[1];
		irDist3Value	= get[2];
		irDist4Value	= get[3];
		temp1Value		= get[4];
		temp2Value		= get[5];
		srValue			= get[6];
		Delay.msDelay(10);
	}
	public int getSensorValue(int sensor) {
		readAllSensors();
		switch (sensor) {
		case IRDIST_RIGHT:
			return irDist1Value;
		case IRDIST2:
			return irDist2Value;
		case IRDIST3:
			return irDist3Value;
		case IRDIST_LEFT:
			return irDist4Value;
		default:
			break;
		}
		
		return -1;
	}
//	public int getSensorValue(int sensor) {
//		int value = 0;
//		switch (sensor) {
//		case 0:
//			send[0] = 1;
//			RS485.hsWrite(send, 0, 1);
//			RS485.hsRead(get, 0, 1);
//			irDist1Value	= get[0];
//			Delay.msDelay(10);
//			break;
//		case 1:
//			
//			break;
//		case 2:
//			
//			break;
//		case 3:
//			
//			break;
//		case 4:
//			
//			break;
//		case 5:
//			
//			break;
//
//
//		default:
//			break;
//		}
//		
//		return value;
//	}
	
	public void showAllSensors() {
		int showValueOffset = 9;
//		send[0] = 1;
//		RS485.hsWrite(send, 0, 1);
//		RS485.hsRead(get, 0, 7);
		readAllSensors();
		
		LCD.clear();
		LCD.drawString("irDist1", 0, 0);	LCD.drawInt(get[0], showValueOffset, 0);
		LCD.drawString("irDist2", 0, 1);	LCD.drawInt(get[1], showValueOffset, 1);
		LCD.drawString("irDist3", 0, 2);	LCD.drawInt(get[2], showValueOffset, 2);
		LCD.drawString("irDist4", 0, 3);	LCD.drawInt(get[3], showValueOffset, 3);
		LCD.drawString("temp1", 0, 4);		LCD.drawInt(get[4], showValueOffset, 4);
		LCD.drawString("temp2", 0, 5);		LCD.drawInt(get[5], showValueOffset, 5);
		LCD.drawString("sr", 0, 6); 		LCD.drawInt(get[6], showValueOffset, 6);

		Delay.msDelay(10);
	}
	
	public int getGyroValue() {
		return gyro.getAngle();
	}
	
	public void resetGyroValue() {
		gyro.reset();
	}
	
	public void rotateServo(int angle) {
		if(angle == 0) {
			send[0] = 8;
		}else{
			send[0] = 9;
		}
		RS485.hsWrite(send, 0, 1);
		Delay.msDelay(10);
	}
}
