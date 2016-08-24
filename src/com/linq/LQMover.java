package com.linq;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;

public class LQMover {
	//Create Fields
	static LQMotor motorLeft;
	static LQMotor motorRight;
	
	//Set tachometer count for forward 30cm
	static final int tileTacho = 690;
	
	
	public LQMover(MotorPort left, MotorPort right) {
		//Create Instances
		motorLeft = new LQMotor(left);
		motorRight = new LQMotor(right);
	}
	
	public void tileForward(int speed) {
		//Set speed
		motorLeft.setSpeed(speed);
		motorRight.setSpeed(speed);
		
		//Set threshold for reset counter (coudn't use resetTachoCount)
		long thresholdLeft = motorLeft.getTachoCount();
		long thresholdRight = motorRight.getTachoCount();
		
		//forward motors
		motorLeft.forward();
		motorRight.forward();
		while(true) {
			//Show tachometers value
			LCD.drawInt(motorLeft.getTachoCount()-(int)thresholdLeft, 0, 0);
			LCD.drawInt(motorRight.getTachoCount()-(int)thresholdRight, 0, 1);
			
			
			if (motorLeft.getTachoCount()-thresholdLeft >= tileTacho) {
				if (motorRight.getTachoCount()-thresholdRight >= tileTacho) {
					//Stop both motor when both tachometer over threshold
					motorLeft.stop();
					motorRight.stop();
					break;
				}else {
					//Stop left motor when only left tachometer over threshold
					motorLeft.stop();
				}
			}else {
				if (motorRight.getTachoCount()-thresholdRight >= tileTacho) {
					//Stop right motor when only left tachometer over threshold
					motorRight.stop();
				}
			}
		}
		
	}
}
