package com.linq;


import com.linq.LQMover;
import com.linq.LQMotor;

import lejos.nxt.MotorPort;
import lejos.util.Delay;

public class Main {
	
	public static void main(String args[]) {
		LQMotor motorLeft = new LQMotor(MotorPort.A);
		LQMotor motorRight = new LQMotor(MotorPort.B);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		
		
		for(int i = 0; i < 2; i++) {
			mover.tileForward(60);
			Delay.msDelay(1000);
		}
	}

}
