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
	private static final int TEMP1 = 1;
	private static final int TEMP2 = 2;
	private static final int IRDIST1 = 3;
	private static final int IRDIST2 = 4;
	private static final int IRDIST3 = 5;
	private static final int IRDIST4 = 6;
	private static final int SRDIST = 7;	

	public static void main(String args[]) {
		
		//ÉÇÅ[É^Å[ÇÃêÈåæ
//		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
//		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
//		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();

		while(true) {
			sensor.showAllSensors();
		}
	}

}
