package com.linq;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.util.Delay;

public class Main
{

	public static void main(String args[]) {
		
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
//		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();
		
		rightMotor.setPower(100);
		rightMotor.forward();
		
		while(true) {
			sensor.rotateServo(0);
			Delay.msDelay(500);
			sensor.rotateServo(180);
			Delay.msDelay(500);
		}
	}
}	
