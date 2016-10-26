package com.linq;

import lejos.nxt.MotorPort;

public class Main
{

	public static void main(String args[]) {
		
//		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
//		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
//		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();

		while(true) {
			sensor.showAllSensors();
		}
	}
}
