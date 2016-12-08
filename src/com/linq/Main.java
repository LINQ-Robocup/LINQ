package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.util.Delay;

import java.lang.Math;

public class Main
{

	public static void main(String args[]) {
		
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.B);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();
		
		leftMotor.setPower(40);
		rightMotor.setPower(40);
		Button
		while () {
			
		}
		int leftOffset = sensor.getSensorValue(sensor.IRDIST_LEFT);
		int rightOffset = sensor.getSensorValue(sensor.IRDIST_RIGHT);
		LCD.drawInt(leftOffset, 0, 0);
		LCD.drawInt(rightOffset, 0, 1);
		if(leftOffset > rightOffset) {
			//go right
			while (sensor.getSensorValue(sensor.IRDIST_LEFT) > rightOffset) {
				leftMotor.forward();
				rightMotor.stop();
			}
		}else {
			//go left
			while (sensor.getSensorValue(sensor.IRDIST_RIGHT) > leftOffset) {
				leftMotor.stop();
				rightMotor.forward();
			}
		}
		leftMotor.stop();
		rightMotor.stop();
		
//		while (true) {
//			LCD.clear();
//			LCD.drawInt(sensor.getSensorValue(sensor.IRDIST1), 0, 0);
//			LCD.drawInt(sensor.getSensorValue(sensor.IRDIST4), 10, 0);
//			Delay.msDelay(10);
//		}
	}
}	
