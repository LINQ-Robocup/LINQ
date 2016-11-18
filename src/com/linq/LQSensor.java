package com.linq;

import lejos.nxt.LCD;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CruizcoreGyro;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;

public class LQSensor {
	private byte send[] = new byte[1];
	private byte get[] = new byte[7];
	
	public int irDist1;
	public int irDist2;
	public int irDist3;
	public int irDist4;
	public int temp1;
	public int temp2;
	public int sr;
	
	public CruizcoreGyro gyro;
	
	public LQSensor() {
		RS485.hsEnable(9600, 0);
		send[0] = 0;
		gyro = new CruizcoreGyro(SensorPort.S3);
	}
	
	public void readAllSensors() {
		send[0] = 0;
		RS485.hsWrite(send, 0, 1);
		RS485.hsRead(get, 0, 7);
		irDist1	= get[0];
		irDist2	= get[1];
		irDist3	= get[2];
		irDist4	= get[3];
		temp1	= get[4];
		temp2	= get[5];
		sr		= get[6];
		Delay.msDelay(10);
	}
	
	public void showAllSensors() {
		int showValueOffset = 9;
		RS485.hsWrite(send, 0, 1);
		RS485.hsRead(get, 0, 7);
		
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
