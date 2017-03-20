package com.linq;

import java.util.Timer;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.NXTMotor;
import lejos.nxt.SensorPort;
import lejos.nxt.addon.CruizcoreGyro;
import lejos.nxt.addon.RCXLightSensor;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class LQNXTSensors {

	public final byte TOUCH_R		= 0;
	public final byte TOUCH_L		= 1;
	public final byte LIGHT			= 2;
	public final byte GYRO			= 3;
	public final byte ACCEL_X		= 4;
	public final byte ACCEL_Y		= 5;
	public final byte ACCEL_Z		= 6;

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
	public NXTMotor servo;
/*===========================================================================*/
	
	public LQNXTSensors() {
		
		this.light_left = new RCXLightSensor(SensorPort.S2);
		this.light_left.setFloodlight(true);
		this.light_right = new RCXLightSensor(SensorPort.S1);
		this.light_right.setFloodlight(true);

		this.gyro = new CruizcoreGyro(SensorPort.S3);
	}
/*===========================================================================*/
	public void debugSensors() {
		int showValueOffset = 10;
		LCD.clear();
		LCD.drawString("TOUCH_L", 	0, 0);	LCD.drawString(isLeftTouchPressed() ? "true" : "false", showValueOffset, 0);
		LCD.drawString("TOUCH_R", 	0, 1);	LCD.drawString(isRightTouchPressed() ? "true" : "false", showValueOffset, 1);
		LCD.drawString("LIGHT"	, 	0, 2);	LCD.drawInt(this.getLightValue(), showValueOffset, 2);
		LCD.drawString("GYRO"	, 	0, 3);	LCD.drawInt(this.getGyroValue(), showValueOffset, 3);
		LCD.drawString("ACCEL_X", 	0, 4);	LCD.drawInt(this.getAccelXValue(), showValueOffset, 4);
		LCD.drawString("ACCEL_Y", 	0, 5);	LCD.drawInt(this.getAccelYValue(), showValueOffset, 5);
		LCD.drawString("ACCEL_Z", 	0, 6);	LCD.drawInt(this.getAccelZValue(), showValueOffset, 6);
		if(Button.ENTER.isDown()) {
			this.resetGyroValue();
		}
		Delay.msDelay(20);
	}

/*===========================================================================*/	

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
/*===========================================================================*/
	
	public int getLightValue() {
		return light_right.getLightValue();
	}
	public boolean isLeftTouchPressed() {
		return light_left.getLightValue() > 90 ? true : false;
	}

	public boolean isRightTouchPressed() {
		return light_right.getLightValue() > 90 ? true : false;
	}
/*===========================================================================*/
}
