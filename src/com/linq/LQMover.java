package com.linq;

import lejos.nxt.Button;
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
	
	//30cm進むためのタコメータカウント
	static final int tileTacho = 690;
	
	//各種動作における方向管理用
	public final int LEFT	= 0;
	public final int RIGHT	= 1;
	public final int FRONT	= 2;
	public final int BEHIND	= 3;
	
	public LQMover(TachoMotorPort left, TachoMotorPort right) {
		//Create Instances
		leftMotor = new LQMotor2(left);
		rightMotor = new LQMotor2(right);
		sensor = new LQSensor();
	}
	
	public void setParallel() {
		int speed = 40;
		leftMotor.stop();
		rightMotor.stop();
		Delay.msDelay(500);
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		int leftOffset = sensor.getValue(sensor.IRDIST_FL);
		Delay.msDelay(200);
		int rightOffset = sensor.getValue(sensor.IRDIST_FR);
		
		/*
		LCD.clear();
		while (!Button.ENTER.isDown()) {
			LCD.drawInt(leftOffset, 0, 1);
			LCD.drawInt(rightOffset, 0, 2);
			LCD.drawInt(sensor.getValue(sensor.IRDIST_FL), 0, 3);
			LCD.drawInt(sensor.getValue(sensor.IRDIST_FR), 0, 4);
		}
		*/
		
		if(leftOffset > rightOffset) {
			//go right
			breakRoop:while (sensor.getValue(sensor.IRDIST_FL) >= sensor.getValue(sensor.IRDIST_FR) + 6) {
				leftMotor.forward();
				rightMotor.stop();
				if(sensor.getValue(sensor.TOUCH_L) == 1) {
					while(sensor.getValue(sensor.TOUCH_R) != 1) {
						rightMotor.forward();
						leftMotor.stop();
					}
					break breakRoop;
				}else if(sensor.getValue(sensor.TOUCH_R) == 1) {
					while(sensor.getValue(sensor.TOUCH_L) != 1) {
						rightMotor.stop();
						leftMotor.forward();
					}
					break breakRoop;
				}
			}
		}else {
			//go left
			breakRoop:while (sensor.getValue(sensor.IRDIST_FL) + 6 <= sensor.getValue(sensor.IRDIST_FR)) {
				leftMotor.stop();
				rightMotor.forward();
				if(sensor.getValue(sensor.TOUCH_L) == 1) {
					while(sensor.getValue(sensor.TOUCH_R) != 1) {
						rightMotor.forward();
						leftMotor.stop();
					}
					break breakRoop;
				}else if(sensor.getValue(sensor.TOUCH_R) == 1) {
					while(sensor.getValue(sensor.TOUCH_L) != 1) {
						rightMotor.stop();
						leftMotor.forward();
					}
					break breakRoop;
				}
			}
		}
		leftMotor.stop();
		rightMotor.stop();
	}
	
	public void setDistance() {
		leftMotor.setPower(35);
		rightMotor.setPower(35);
		if(Math.abs(((sensor.getValue(sensor.IRDIST_FL) + sensor.getValue(sensor.IRDIST_FR)) /2) - 40) >= 5) {
				//too far
				while( (sensor.getValue(sensor.IRDIST_FL) + sensor.getValue(sensor.IRDIST_FR)) /2 > 35 ) {
					leftMotor.forward();
					rightMotor.forward();	
				}
				//too short
				while( (sensor.getValue(sensor.IRDIST_FL) + sensor.getValue(sensor.IRDIST_FR)) /2 < 35 ) {
					leftMotor.backward();
					rightMotor.backward();
				}
			leftMotor.stop();
			rightMotor.stop();	
		} 
	}
	
	public void tileForward(int speed) {
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		int wallDistOffset = 25;
		int wallAvoidAngle = 200;
		int perTile = tileTacho / 10;
		long leftMotorOffset = leftMotor.getTachoCount();
		long rightMotorOffset = rightMotor.getTachoCount();
		
		for(int i = 1; i <= 10; i ++) {
			/* rescue */
			int isRescue = sensor.isRescue();
			switch (isRescue) {
			case 1:
				//RESCUE LEFT
				rotate(RIGHT);
				rotate(LEFT);
				break;
			case 2:
				rotate(LEFT);
				rotate(RIGHT);
				//RESCUE RIGHT
			default:
				break;
			}
			
			/* avoid wall */
			sensor.readAllSensors();
			if(sensor.irDistLeftValue > wallDistOffset) {
				//left to right
				int leftOffset = leftMotor.getTachoCount();
				int rightOffset = rightMotor.getTachoCount();
				while(leftMotor.getTachoCount() - leftOffset < wallAvoidAngle) {
					leftMotor.forward();
					rightMotor.stop();
				}
				while(rightMotor.getTachoCount() - rightOffset < wallAvoidAngle) {
					leftMotor.stop();
					rightMotor.forward();
				}
				leftMotor.forward();
				rightMotor.forward();
			}else if(sensor.irDistRightValue > wallDistOffset) {
				//right to left
				int leftOffset = leftMotor.getTachoCount();
				int rightOffset = rightMotor.getTachoCount();
				while(rightMotor.getTachoCount() - rightOffset < wallAvoidAngle) {
					leftMotor.stop();
					rightMotor.forward();
				}
				while(rightMotor.getTachoCount() - leftOffset < wallAvoidAngle) {
					leftMotor.forward();
					rightMotor.stop();
				}
				leftMotor.forward();
				rightMotor.forward();
			}
			
			/* forward 30cm */
			while( ((leftMotor.getTachoCount() - leftMotorOffset) + (leftMotor.getTachoCount() - leftMotorOffset)) /2 < perTile*i) {
				leftMotor.forward();
				rightMotor.forward();
			}
			if(i == 10) {
				if ((leftMotor.getTachoCount() - leftMotorOffset) > perTile*i) {
					if ((rightMotor.getTachoCount() - rightMotorOffset) > perTile*i) {
						// Stop both motor when both tachometer over threshold
						leftMotor.stop();
						rightMotor.stop();
						break;
					} else {
						// Stop left motor when only left tachometer over threshold
						leftMotor.stop();
					}
				} else {
					if (rightMotor.getTachoCount() - rightMotorOffset >= perTile*i) {
						// Stop right motor when only left tachometer over threshold
						rightMotor.stop();
					}
				}
			}
		}
		leftMotor.stop();
		rightMotor.stop();
		Delay.msDelay(500);
	}

	public void rotate(int direction) {
		leftMotor.stop();
		rightMotor.stop();
		Delay.msDelay(500);

		int speed = 75;
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		
		sensor.resetGyroValue();
		int offset = sensor.getGyroValue();
		
		if(direction == LEFT) {
			while (sensor.getGyroValue() - offset - (-8700) >= 0) {
				if(sensor.getGyroValue() - offset - (-8700) <= 2000) {
					leftMotor.setPower(speed/2);
					rightMotor.setPower(speed/2);
				}
				leftMotor.backward();
				rightMotor.forward();
			}
		}else if (direction == RIGHT) {
			while (sensor.getGyroValue() - offset - 8700 <= 0) {
				if(sensor.getGyroValue() - offset - 8700 >= -2000) {
					leftMotor.setPower(speed/2);
					rightMotor.setPower(speed/2);
				}
				leftMotor.forward();
				rightMotor.backward();
			}
		}
		
		leftMotor.stop();
		rightMotor.stop();
		Delay.msDelay(300);
	}
}
