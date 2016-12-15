package com.linq;

import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.TachoMotorPort;
import lejos.util.Delay;

public class LQMover {
	// Create Fields
	static LQMotor2 leftMotor;
	static LQMotor2 rightMotor;
	static LQSensor sensor;

	//温度のしきい値
	int tempThreshold = 25;
	
	// Set tachometer count for forward 30cm
	static final int tileTacho = 690;
	
	public final int LEFT = 0;
	public final int RIGHT = 1;
	public final int BACK = 2;
	
	public LQMover(TachoMotorPort left, TachoMotorPort right) {
		//Create Instances
		leftMotor = new LQMotor2(left);
		rightMotor = new LQMotor2(right);
		sensor = new LQSensor();
	}
	
	public void setParallel() {
		leftMotor.setPower(35);
		rightMotor.setPower(35);
		int leftOffset = sensor.getValue(sensor.IRDIST_L);
		int rightOffset = sensor.getValue(sensor.IRDIST_R);
		LCD.drawInt(leftOffset, 0, 0);
		LCD.drawInt(rightOffset, 0, 1);
		if(leftOffset > rightOffset) {
			//go right
			while (sensor.getValue(sensor.IRDIST_L) -2  >= sensor.getValue(sensor.IRDIST_R)) {
				boolean isWall = false;
				if(isWall) break;
				if(sensor.getValue(sensor.TOUCH_R) == 1) {
					isWall = true;
					while(sensor.getValue(sensor.TOUCH_L) != 1) {
						leftMotor.setPower(50);
						rightMotor.stop();
						leftMotor.forward();
					}
				}
				leftMotor.forward();
				rightMotor.stop();
			}
		}else {
			//go left
			while (sensor.getValue(sensor.IRDIST_R)-2  >= sensor.getValue(sensor.IRDIST_L)) {
				boolean isWall = false;
				if(isWall) break;
				if(sensor.getValue(sensor.TOUCH_L) == 1) {
					isWall = true;
					while(sensor.getValue(sensor.TOUCH_R) != 1) {
						rightMotor.setPower(50);
						leftMotor.stop();
						rightMotor.forward();
					}
				}
				leftMotor.stop();
				rightMotor.forward();
			}
		}
		leftMotor.stop();
		rightMotor.stop();
		LCD.drawInt(sensor.getValue(sensor.IRDIST_L), 0, 4);
		LCD.drawInt(sensor.getValue(sensor.IRDIST_R), 0, 5);
	}
	
	public void setDistance() {
		leftMotor.setPower(35);
		rightMotor.setPower(35);
		if(Math.abs(((sensor.getValue(sensor.IRDIST_L) + sensor.getValue(sensor.IRDIST_R)) /2) - 40) >= 5) {
				//too far
				while( (sensor.getValue(sensor.IRDIST_L) + sensor.getValue(sensor.IRDIST_R)) /2 > 35 ) {
					leftMotor.forward();
					rightMotor.forward();	
				}
				//too short
				while( (sensor.getValue(sensor.IRDIST_L) + sensor.getValue(sensor.IRDIST_R)) /2 < 35 ) {
					leftMotor.backward();
					rightMotor.backward();
				}
			leftMotor.stop();
			rightMotor.stop();	
		} 
	}
	
	public void tileForward(int speed) {
		// Set speed
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);

		// Set threshold for reset counter (coudn't use resetTachoCount)
		long thresholdLeft = leftMotor.getTachoCount();
		long thresholdRight = rightMotor.getTachoCount();

		int nowLoading = 0;
		int nowCounting = 1;
		int nowMeasureing = tileTacho/10;
		// forward motors
		
		int wallDistance = 10;
		int wallAvoidDistance = 30;
		while (true) {
			nowLoading = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) /2;
			if(nowLoading > nowMeasureing * nowCounting) {
//				Sound.beep();
				//壁との距離を取る
				sensor.readAllSensors();
//				if(sensor.getValue(sensor.IRDIST_L) < wallDistance) {
				if(sensor.irDistLeftValue < wallDistance) {
					int leftOffset = leftMotor.getTachoCount();
					while (leftMotor.getTachoCount() - leftOffset > 160) {
						leftMotor.forward();
						rightMotor.stop();
					}
					int rightOffset = rightMotor.getTachoCount();
					while (rightMotor.getTachoCount() - rightOffset > 160) {
						rightMotor.forward();
						leftMotor.stop();
					}
				}
				//一定の温度以上を感知した場合
//				if(sensor.getValue(sensor.TEMP_L) > tempThreshold || sensor.getValue(sensor.TEMP_R) > tempThreshold) {
				if(sensor.tempLeftValue > tempThreshold || sensor.tempRightValue > tempThreshold) {
					leftMotor.stop();
					rightMotor.stop();
					Sound.beepSequence();
					while(true);
				}
				nowCounting++;
			}

			if(sensor.isLeftTouchPressed() == 1 || sensor.isRightTouchPressed() == 1) {
				leftMotor.stop();
				rightMotor.stop();
				break;
			}
			
			leftMotor.forward();
			rightMotor.forward();
			// Show tachometers value
			LCD.drawInt(leftMotor.getTachoCount() - (int) thresholdLeft, 0, 0);
			LCD.drawInt(rightMotor.getTachoCount() - (int) thresholdRight, 0, 1);

			if (leftMotor.getTachoCount() - thresholdLeft >= tileTacho) {
				if (rightMotor.getTachoCount() - thresholdRight >= tileTacho) {
					// Stop both motor when both tachometer over threshold
					leftMotor.stop();
					rightMotor.stop();
					break;
				} else {
					// Stop left motor when only left tachometer over threshold
					leftMotor.stop();
				}
			} else {
				if (rightMotor.getTachoCount() - thresholdRight >= tileTacho) {
					// Stop right motor when only left tachometer over threshold
					rightMotor.stop();
				}
			}
		}
	}

	public void rotate(int direction) {
		int speed = 85;
		double offset = sensor.getGyroValue();
		
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		
		if(direction == LEFT) {
			leftMotor.backward();
			rightMotor.forward();
		}else if(direction == RIGHT) {
			leftMotor.forward();
			rightMotor.forward();
		}else{
			return;
		}
		
		while (Math.abs(sensor.getGyroValue() - offset) < 8700) {
			if(Math.abs(sensor.getGyroValue() - offset) > 7000) {
				leftMotor.setPower(speed/2);
				rightMotor.setPower(speed/2);
			}
		}
		leftMotor.stop();
		rightMotor.stop();
		Delay.msDelay(500);
	}
}
