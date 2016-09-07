package com.linq;


import com.linq.LQMover;
import com.linq.LQMotor2;

import lejos.nxt.MotorPort;
import lejos.util.Delay;

public class Main {
	
	public static void main(String args[]) {
		
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.C);
		
		
		
		int count = 3;
		for(int i = 0; i < count; i++) {
			mover.tileForward(100);
			Delay.msDelay(1000);
		}
	}

}
