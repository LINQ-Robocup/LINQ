package com.linq;


import com.linq.LQMover;
import com.linq.LQMotor2;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.addon.LDCMotor;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class Main {
	
	public static void main(String args[]) {
		
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		
		RS485.hsEnable(9600, 64);
		byte buffer[] = new byte[1];
		buffer[0] = 11;
		
		Stopwatch stopwatch = new Stopwatch();
		stopwatch.reset();
		for(int i = 0; i < 1000050; i++) {
			LCD.clear();
//			RS485.hsWrite(buffer, 0, 1);
			RS485.hsRead(buffer, 0, 1);
//			LCD.drawInt(buffer[0], 0, 0);
//			Delay.msDelay(20);
		}
		int time = stopwatch.elapsed();
		while(true) {
			LCD.drawInt(time, 0, 0);
		}
		
		
//		int count = 3;
//		for(int i = 0; i < count; i++) {
//			mover.tileForward(100);
//			Delay.msDelay(1000);
//		}
	}

}
