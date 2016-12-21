package com.linq;

import java.lang.Math;

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

	// offset 
	int offset = 0;
	
	//温度のしきい値
	int tempThreshold = 25;
	
	//30cm進むためのタコメータカウント
	static final int tileTacho = 690;
	
	//各種動作における方向管理用
	public final byte LEFT		= 0;
	public final byte RIGHT		= 1;
	public final byte FRONT		= 2;
	public final byte BEHIND	= 3;
	
	//床の色管理
	public final byte WHITE		= 0;
	public final byte BLACK		= 1;
	public final byte RAMP		= 2;
	public final byte SILVER	= 3;
	public final byte VICTIM	= 4;
	
	public LQMover(TachoMotorPort left, TachoMotorPort right) {
		//Create Instances
		leftMotor = new LQMotor2(left);
		rightMotor = new LQMotor2(right);
		sensor = new LQSensor();
	}
	
	/**
	 * 姿勢制御(前方の壁と平行になるように修正)
	 */
	public void setParallel(int speed) {
		leftMotor.stop();
		rightMotor.stop();
		sensor.readAllSensors();
		int leftOffset = sensor.irDistFLeftValue;
		int rightOffset = sensor.irDistFRightValue;
		
		if(leftOffset > 80 || rightOffset > 80) {
			return;
		}
		
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		if(leftOffset > rightOffset + 1) {
			leftMotor.forward();
			rightMotor.stop();
			while(true) {
				if(sensor.getValue(sensor.TOUCH_R) == 1) {
					if(sensor.getValue(sensor.TOUCH_L) == 1) {
						break;
					} 
				} else {
					sensor.readAllSensors();
					if(sensor.irDistFLeftValue <= sensor.irDistFRightValue + 1) {
						break;
					}
				}
			}
			leftMotor.stop();
			rightMotor.stop();
			offset = 0;
		} else if(rightOffset > leftOffset + 3){
			leftMotor.stop();
			rightMotor.forward();
			while(true) {
				if(sensor.getValue(sensor.TOUCH_L) == 1) {
					if(sensor.getValue(sensor.TOUCH_R) == 1) {
						break;
					} 
				} else {
					sensor.readAllSensors();
					if(sensor.irDistFRightValue <= sensor.irDistFLeftValue + 3) {
						break;
					}
				}
			}
			leftMotor.stop();
			rightMotor.stop();
			offset = 0;
		}
		
		int dist = (sensor.irDistFLeftValue + sensor.irDistFRightValue) / 2;
		if(dist < 44 || dist > 46) {
			setDistance(speed-5);
		}
		leftMotor.stop();
		rightMotor.stop();
	}
	
	/**
	 * タイルの中心に移動(前方の壁との距離を修正)
	 */
	public void setDistance(int speed) {
		while(true) {
			sensor.readAllSensors();
			int dist = (sensor.irDistFLeftValue + sensor.irDistFRightValue) / 2;
			if(dist > 80) {
				break;
			} else if(Math.abs(dist-45) > 3) {
				leftMotor.setPower(speed);
				rightMotor.setPower(speed);
				if(dist > 45) {
					leftMotor.forward();
					rightMotor.forward();
				} else {
					leftMotor.backward();
					rightMotor.backward();
				}
			} else {
				break;
			}
		}
		leftMotor.stop();
		rightMotor.stop(); 
	}
	public void avoidWall(int direction) {
		leftMotor.stop();
		rightMotor.stop();
		int wallAvoidAngle = 180;
		sensor.readAllSensors();
		if(direction == LEFT) {
			//left to right
			int leftOffset = leftMotor.getTachoCount();
			int rightOffset = rightMotor.getTachoCount();
			while(leftMotor.getTachoCount() - leftOffset > wallAvoidAngle*-1) {
				leftMotor.backward();
				rightMotor.stop();
			}
			while(rightMotor.getTachoCount() - rightOffset > wallAvoidAngle*-1) {
				leftMotor.stop();
				rightMotor.backward();
			}
			while(leftMotor.getTachoCount() < leftOffset) {
				leftMotor.forward();
				rightMotor.stop();
			}
			while(rightMotor.getTachoCount() < leftOffset) {
				leftMotor.stop();
				rightMotor.forward();
			}
		}else if(direction == RIGHT) {
			//right to left
			int leftOffset = leftMotor.getTachoCount();
			int rightOffset = rightMotor.getTachoCount();
			while(rightMotor.getTachoCount() - rightOffset > wallAvoidAngle*-1) {
				leftMotor.stop();
				rightMotor.backward();
			}
			while(leftMotor.getTachoCount() - leftOffset > wallAvoidAngle*-1) {
				leftMotor.backward();
				rightMotor.stop();
			}
			while(rightMotor.getTachoCount()  < leftOffset) {
				leftMotor.stop();
				rightMotor.forward();
			}
			while(leftMotor.getTachoCount()  < leftOffset) {
				leftMotor.forward();
				rightMotor.stop();
			}
		}
	}
	
	public void climbRamp(int speed) {
		int flatThreshold = -50;
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		int rampCount = 0;
		while (true) {
			if(sensor.getAccelYValue() > flatThreshold) {
				rampCount++;
				if(rampCount > 20) {
					break;
				}
			}else{
				rampCount = 0;
			}
			leftMotor.forward();
			rightMotor.forward();
		}
		leftMotor.stop();
		rightMotor.stop();
		setParallel(50);
		setDistance(50);
	}
	
	public void downRamp(int speed) {
		int flatThreshold = 50;
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		int rampCount = 0;
		while (true) {
			if(sensor.getAccelYValue() < flatThreshold) {
				rampCount++;
				if(rampCount > 20) {
					break;
				}
			}else{
				rampCount = 0;
			}
		}
		leftMotor.stop();
		rightMotor.stop();
		setParallel(50);
		setDistance(50);
	}
	public int tileForward(int speed, boolean pass) {
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		offset = 0;
		int wallDistThreshold = 80;
		int perTile = tileTacho / 10;
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		long leftMotorOffset = leftMotor.getTachoCount();
		long rightMotorOffset = rightMotor.getTachoCount();
		
		int rotation = 0;
		byte slopeCount = 0;
		byte blackCount = 0;
		byte tempLeftCount = 0;
		byte tempRightCount = 0;
		int tempThreshold = 30;
		int rampThreshold = -300;
		
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		sensor.resetGyroValue();
		for(int i = 1; i <= 10; i ++) {
			sensor.readAllSensors();
			sensor.getGyroValue();
			
			/* black */
			if(sensor.getLightValue() < 20) {
				blackCount++;
				if(blackCount > 3) {
					while (leftMotor.getTachoCount() > 0) {
						leftMotor.backward();
						rightMotor.backward();
					}
					leftMotor.stop();
					rightMotor.stop();
					return BLACK;
				}
			}else{
				blackCount = 0;
			}
			
			/* rescue */
			if(pass == false && i > 1 && i < 10) {
				//left
				if(sensor.irDistLeftValue < wallDistThreshold && sensor.tempLeftValue > tempThreshold) {
					tempLeftCount++;
					if(tempLeftCount > 0) {
						int gyroValue = sensor.getGyroValue();
						rotate(gyroValue + 9000, true);
						leftMotor.setPower(speed);
						rightMotor.setPower(speed);
						leftMotor.stop();
						rightMotor.stop();

						sensor.servoRight();
						sensor.blinkLED();
						
						rotate(gyroValue, true);
						leftMotor.setPower(speed);
						rightMotor.setPower(speed);
						leftMotor.stop();
						rightMotor.stop();

						pass = true;
					}
				}else{
					tempLeftCount = 0;
				}
				//right
				if(sensor.irDistRightValue < wallDistThreshold && sensor.tempRightValue > tempThreshold) {
					tempRightCount++;
					if(tempRightCount > 0) {
						int gyroValue = sensor.getGyroValue();
						rotate(gyroValue - 9000, true);
						leftMotor.setPower(speed);
						rightMotor.setPower(speed);
						leftMotor.stop();
						rightMotor.stop();

						sensor.servoLeft();
						sensor.blinkLED();
						
						rotate(gyroValue, true);
						leftMotor.setPower(speed);
						rightMotor.setPower(speed);
						leftMotor.stop();
						rightMotor.stop();
						pass = true;
					}
				}else{
					tempRightCount = 0;
				}
			}
			
			/* forward 30cm */
			while(rotation < perTile*i) {
				rotation = (int) ((leftMotor.getTachoCount() - leftMotorOffset + rightMotor.getTachoCount() - rightMotorOffset) /2);
				int leftTouch = sensor.isLeftTouchPressed();
				int rightTouch = sensor.isRightTouchPressed();
				if(leftTouch == 1) {
					avoidWall(LEFT);
				}else if(rightTouch == 1){
					avoidWall(RIGHT);
				}
				leftMotor.forward();
				rightMotor.forward();
			}
		}
		leftMotor.stop();
		rightMotor.stop();
		Delay.msDelay(500);
		/* ramp */
		for (int i = 0; i < 2; i++) {
			if(sensor.getAccelYValue() < rampThreshold) {
				slopeCount++;
				if(slopeCount >= 2) {
//					climbRamp(speed);
					while (!Button.ENTER.isDown()) {
						LCD.clear();
						LCD.drawString("RAMP", 5, 5);
					}
					return RAMP;
				}
			}else{
				break;
			}
		}
		/* silver */
		int silverThreshold = 30;
		int silverMaxThreshold = 60;
		byte silverCount = 0;
		for (int i = 0; i < 3; i++) {
			if (sensor.getLightValue() > silverThreshold && sensor.getLightValue() < silverMaxThreshold) {
				silverCount++;
				if(silverCount == 3) {
					return SILVER;
				}
			}else {
				break;
			}
		}
		offset = offset - sensor.getGyroValue();
		return WHITE;
	}

	/**
	 * ジャイロのoffset値を更新
	 * @param direction : 目標角度
	 */
	private void setOffset(int direction) {
		offset = direction - sensor.getGyroValue();
	}
	
	/**
	 * 90度右回転
	 * @param pass : true)通過済み, false)未通過・被災者探索(左側)
	 */
	public void turnRight(boolean pass) {
		final int ANGLE = 9000;//時計回り90度
		leftMotor.stop();
		rightMotor.stop();
		sensor.resetGyroValue();
		rotate(ANGLE, pass);
		leftMotor.stop();
		rightMotor.stop();
		setOffset(ANGLE-offset);
	}
	
	/**
	 * 90度左回転
	 * @param pass : true)通過済み, false)未通過・被災者探索(右側)
	 */
	public void turnLeft(boolean pass) {
		final int ANGLE = -9000;//反時計回り90度
		leftMotor.stop();
		rightMotor.stop();
		sensor.resetGyroValue();
		rotate(ANGLE, pass);
		leftMotor.stop();
		rightMotor.stop();
		setOffset(ANGLE-offset);
	}
	
	/**
	 * 目標角度を0とした方向修正
	 * @param direction : 目標角度(-18000 ~ 18000)
	 * @param pass : true)通過済み, false)未通過・被災者探索
	 */
	private void rotate(int direction, boolean pass) {
		int curDirection = 0;
		byte tmp_cnt = 0;
		byte speed = 40;
		while(true) {
			/* 目標角度が0になるように curDirectionの値を設定 */
			int gyro = sensor.getGyroValue();
			curDirection = gyro - direction - offset;
			if(Math.abs(curDirection) > 18000) {
				curDirection += (curDirection > 0) ? -36000 : 36000; 
			}
			/* 目標角度に到達するための方向修正 */
			if(curDirection > -500 && curDirection < 500) {
				break;
			} else {
				if(pass == false) {
					/* 熱源検知(未通過のみ) */
					sensor.readAllSensors();
					if(direction < 0 && sensor.irDistRightValue < 80 && sensor.tempRightValue > tempThreshold) {
						if(tmp_cnt >= 2) {
							//被災者検知(左回転, 右側)
							rotate(gyro - 9000, true);//現在の角度-90度回転(被災者に背を向ける)
							leftMotor.stop();
							rightMotor.stop();
							//LED点滅・レスキューキット排出
							Delay.msDelay(2000);
							pass = true;
						}
						tmp_cnt ++;
					} else if(direction > 0 && sensor.irDistLeftValue < 80 && sensor.tempLeftValue > tempThreshold) {
						if(tmp_cnt >= 2) {
							//被災者検知(右回転, 左側)
							rotate(gyro + 9000, true);//現在の角度+90度回転(被災者に背を向ける)
							leftMotor.stop();
							rightMotor.stop();
							//LED点滅・レスキューキット排出
							Delay.msDelay(2000);
							pass = true;
						}
						tmp_cnt ++;
					} else {
						tmp_cnt = 0;
					}
				}
				// 目標角度との差に比例したspeedを設定(最低:50)
				speed = (byte) (50 + Math.abs(curDirection) / 18000 * 50);
				leftMotor.setPower(speed);
				rightMotor.setPower(speed);
				if(curDirection > 0) {
					leftMotor.backward();
					rightMotor.forward();
				} else {
					leftMotor.forward();
					rightMotor.backward();

				}
			}
		}
	}
}