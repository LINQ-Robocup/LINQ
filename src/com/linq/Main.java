package com.linq;


import com.linq.LQMover;
import com.linq.LQMotor2;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.addon.LDCMotor;
import lejos.nxt.addon.LServo;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;
import lejos.util.Stopwatch;

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
