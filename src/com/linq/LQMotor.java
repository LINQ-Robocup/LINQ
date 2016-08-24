package com.linq;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;

public class LQMotor {
	static NXTRegulatedMotor tachoLeft;
	static NXTRegulatedMotor tachoRight;
	static NXTMotor motorLeft;
	static NXTMotor motorRight;
	
	static final int tileTacho = 690;
	
	public LQMotor(MotorPort left, MotorPort right) {
		tachoLeft = new NXTRegulatedMotor(left);
		tachoRight = new NXTRegulatedMotor(right);
		motorLeft = new NXTMotor(left);
		motorRight = new NXTMotor(right);
	}
	
	public void tileForward(int speed) {
		motorLeft.setPower(speed);
		motorRight.setPower(speed);
		
		long thresholdLeft = tachoLeft.getTachoCount();
		long thresholdRight = tachoRight.getTachoCount();
		motorLeft.forward();
		motorRight.forward();
		
		while(true) {
			LCD.drawInt(tachoLeft.getTachoCount()-(int)thresholdLeft, 0, 0);
			LCD.drawInt(tachoRight.getTachoCount()-(int)thresholdRight, 0, 1);
			if (tachoLeft.getTachoCount()-thresholdLeft >= tileTacho) {
				if (tachoRight.getTachoCount()-thresholdRight >= tileTacho) {
					motorLeft.stop();
					motorRight.stop();
					break;
				}else {
					motorLeft.stop();
				}
			}else {
				if (tachoRight.getTachoCount()-thresholdRight >= tileTacho) {
					motorRight.stop();
				}
			}
		}
		
	}
}
