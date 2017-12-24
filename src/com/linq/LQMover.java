package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.Sound;
import lejos.nxt.TachoMotorPort;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class LQMover {
	// Create Fields
	static LQMotor2 leftMotor;
	static LQMotor2 rightMotor;
	LQMbedSensors mbed;
	LQNXTSensors sensor;

	// offset 
	int offset = 0;
	
	//センサ閾値 
	private static final byte TEMP_THRESHOLD = 50;
	private static final byte LIGHT_BLACK_THRESHOLD = 24;
	private static final byte LIGHT_SILVER_THRESHOLD = 30;
	
	//30cm進むためのタコメータカウント
	private static final int TILE_TACHO = 690;
	
	//各種動作における方向管理用
	public static final byte LEFT	= 0;
	public static final byte RIGHT	= 1;
	public static final byte FRONT	= 2;
	public static final byte BACK	= 3;
	
	//床の種類管理
	public static final byte WHITE		= 0;
	public static final byte BLACK		= 1;
	public static final byte RAMP		= 2;
	public static final byte SILVER		= 3;
	public static final byte WALL		= 4;
	public static final byte UP_RAMP	= 5;
	public static final byte DOWN_RAMP	= 6;
	
	public LQMover(TachoMotorPort left, TachoMotorPort right) {
		//Create Instances
		leftMotor = new LQMotor2(left);
		rightMotor = new LQMotor2(right);
		mbed = new LQMbedSensors();
		sensor = new LQNXTSensors();
	}
	
	/**
	 * mbedにセンサ情報を要求
	 */
	public void requestToMbedSensors() {
//		mbed.resetBuffer();
		for (int i = 0; i < 10; i++) {
			mbed.readAllSensors();
			Delay.msDelay(30);
		}
	}
	
	/**
	 * 右側に壁があるか?
	 * @return 壁がある: true 壁がない: false
	 */
	public boolean isWallRight() {
		return (mbed.distRightValue < 100) ? true : false;
	}

	public void debugWall() {
		while(!Button.ESCAPE.isDown()) {
			LCD.clear();
			mbed.readAllSensors();
			LCD.drawString("FRONT: ", 1, 0); LCD.drawString(this.isWallFront() ? "TRUE" : "FLASE", 8, 0);
			LCD.drawString("RIGHT: ", 1, 1); LCD.drawString(this.isWallRight() ? "TRUE" : "FLASE", 8, 1);
			LCD.drawString("LEFT: ", 1, 2); LCD.drawString(this.isWallLeft() ? "TRUE" : "FLASE", 8, 2);
			Delay.msDelay(20);
		}
		while(Button.ESCAPE.isDown());
	}
	/**
	 * 前方に壁があるか?
	 * @return 壁がある: true 壁がない: false
	 */
	public boolean isWallFront() {
		return (mbed.distFrontLeftValue < 100 && mbed.distFrontRightValue < 100);
	}
	
	/**
	 * 左側に壁があるか?
	 * @return 壁がある: true 壁がない: false
	 */
	public boolean isWallLeft() {
		return (mbed.distLeftValue < 100) ? true : false;
	}
	
	/**
	 * 後ろに壁があるか(90度回転して確認)
	 * @return
	 */
	public boolean isWallBack() {
		boolean back = false;
		stop();
//		sensor.resetGyroValue();
		int firstDirec = sensor.getGyroValue();
		if(compSideDist() > 0) {
			changeDirectionUsingGyro(firstDirec, -9000);
			mbed.readAllSensors();
			back = isWallLeft() ? true : false;
			if(isWallFront()) setParallel();
		} else {
			changeDirectionUsingGyro(firstDirec, 9000);
			mbed.readAllSensors();
			back = isWallRight() ? true : false;
			if(isWallFront()) setParallel();
		}
		changeDirectionUsingGyro(firstDirec, 0);
//		if(back) backWall();
		return back;
	}
	
	/**
	 * 駆動モータを停止
	 */
	public void stop() {
		rightMotor.setPower(0);
		leftMotor.setPower(0);
	}
	
	/**
	 * 左右の距離の比率(どちらが壁に近いか)
	 * @return 右側の距離センサの値-左側の距離センサの値
	 */
	public byte compSideDist() {
		mbed.readAllSensors();
		return (byte)(mbed.distRightValue - mbed.distLeftValue);
	}
	
	private boolean isTiltUp() {
		return (sensor.getAccelYValue() > 300 /*&& sensor.getAccelZValue() > -1000*/) ? true : false;
	}
	
	private boolean isTiltDown() {
		return (sensor.getAccelYValue() < -200 /*&& sensor.getAccelZValue() < -98*/) ? true : false;
	}
	
	/**
	 * 前方の壁と平行になるように修正
	 */
	public void setParallel() {
		//壁にタッチ
		stop();
		requestToMbedSensors();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		if (mbed.distFrontLeftValue > 100 || mbed.distFrontRightValue > 100) return;
		leftMotor.setPower(80);
		rightMotor.setPower(80);
		leftMotor.forward();
		rightMotor.forward();
		while(true){
			if (sensor.isLeftTouchPressed() && sensor.isRightTouchPressed()) break;
//			else if ((leftMotor.getTachoCount() + rightMotor.getTachoCount())/2 > 120) break; 
		}
		stop();
		//中心に移動(回転)
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		leftMotor.setPower(70);
		rightMotor.setPower(70);
		leftMotor.backward();
		rightMotor.backward();
		while((leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2 > -120){}
		stop();
		//ジャイロオフセットのリセット
		this.offset = 0;
	}
	
	public void backWall() {
		byte speed = 80;
		if(sensor.getLightValue() > LIGHT_SILVER_THRESHOLD) {
			return;
		}
		//壁に押しつけ(時間) 
//		leftMotor.resetTachoCount();
//		rightMotor.resetTachoCount();
//		while((leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2 >- 120) {
//			leftMotor.setPower(-speed);
//			rightMotor.setPower(-speed);
//		}
//		stop();
		leftMotor.setPower(-speed);
		rightMotor.setPower(-speed);
		Delay.msDelay(600);
		stop();
		//傾き防止
//		leftMotor.setPower(50);
//		rightMotor.setPower(50);
//		Delay.msDelay(100);
//		stop();
		//中心に移動(回転)
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		while((leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2 < 120) {
			leftMotor.setPower(speed);
			rightMotor.setPower(speed);
		}
		stop();
		//ジャイロオフセットのリセット
		this.offset = 0;
	}
	
	public void avoidWall(boolean direction, int pwr) {
		final int ANGLE = 120;
		stop();
		int leftOffset	= leftMotor.getTachoCount();
		int rightOffset = rightMotor.getTachoCount();
		leftMotor.setPower(pwr);
		rightMotor.setPower(pwr);
		if(direction) {
			//right to left
			while(rightMotor.getTachoCount() - rightOffset > -ANGLE) {
				leftMotor.stop();
				rightMotor.backward();
			}
//			stop();
			while(leftMotor.getTachoCount() - leftOffset > -ANGLE) {
				leftMotor.backward();
				rightMotor.stop();
			}
			while(rightMotor.getTachoCount() < leftOffset) {
				leftMotor.stop();
				rightMotor.forward();
			}
//			stop();
			while(leftMotor.getTachoCount() < leftOffset) {
				leftMotor.forward();
				rightMotor.stop();
			}
//			stop();
		} else {
			//left to right
			while(leftMotor.getTachoCount() - leftOffset > -ANGLE) {
				leftMotor.backward();
				rightMotor.stop();
			}
//			stop();
			while(rightMotor.getTachoCount() - rightOffset > -ANGLE) {
				leftMotor.stop();;
				rightMotor.backward();
			}
//			stop();
			while(leftMotor.getTachoCount() < leftOffset) {
				leftMotor.forward();
				rightMotor.stop();
			}
//			stop();
			while(rightMotor.getTachoCount() < leftOffset) {
				leftMotor.stop();
				rightMotor.forward();
			}
//			stop();
		}
		stop();
	}

	void ramp(boolean up) {
		if (up) upRamp();
		else downRamp();
	}
	
	public void upRamp() {
		int speed = 100;
		byte not_tilt_cnt = 0;
		if (!isTiltUp()) {
			tileForward(true, false);
		}
		while(true) {
			if(!isTiltUp()) {
				not_tilt_cnt ++;
				if(not_tilt_cnt > 10) {
					stop();
					Delay.msDelay(300);
					if (sensor.getAccelYValue() < 50) break;  
				}
			} else {
				not_tilt_cnt = 0;
			}
			if(sensor.isLeftTouchPressed() && sensor.isRightTouchPressed()) {
				break;
			} else if(sensor.isLeftTouchPressed()) {
				leftMotor.setPower(speed);
				rightMotor.setPower(0);
			} else if(sensor.isRightTouchPressed()) {
				leftMotor.setPower(0);
				rightMotor.setPower(speed);
			} else {
				mbed.readAllSensors();
				if(mbed.distLeftValue >= 70) mbed.distLeftValue = 40;
				else if(mbed.distRightValue >= 70) mbed.distRightValue = 40;
				leftMotor.setPower(speed-compSideDist()/2);
				rightMotor.setPower(speed-compSideDist()/2);
			}
			Delay.msDelay(10);
		}
		stop();
	}
	
	public void downRamp() {
		Sound.beepSequenceUp();
		Stopwatch timer = new Stopwatch();
		int speed = 50;
		byte not_tilt_cnt = 0;
		timer.reset();
		while(true) {
			if(timer.elapsed() > 3000) {
				if(!isTiltDown()) {
					not_tilt_cnt ++;
					if(not_tilt_cnt > 10) {
						stop();
						Delay.msDelay(100);
						if (!isTiltDown()) break;
						else not_tilt_cnt = 0;
					}
				} else {
					not_tilt_cnt = 0;
				}
			}
			leftMotor.setPower(speed);
			rightMotor.setPower(speed);
			if(sensor.isLeftTouchPressed() && sensor.isRightTouchPressed()) {
				break;
			} else if(sensor.isLeftTouchPressed()) {
				leftMotor.forward();
				rightMotor.backward();;
			} else if(sensor.isRightTouchPressed()) {
				leftMotor.backward();
				rightMotor.forward();
			} else {
				mbed.readAllSensors();
				if(mbed.distLeftValue >= 127) mbed.distLeftValue = 40;
				else if(mbed.distRightValue >= 127) mbed.distRightValue = 40;
				leftMotor.setPower(speed-compSideDist()/2);
				rightMotor.setPower(speed-compSideDist()/2);
				leftMotor.forward();
				rightMotor.forward();
			}
			Delay.msDelay(10);
			stop();
		}
		stop();
	}
	
	public byte tileForward(boolean pass, boolean first_floor) {
		int rotate = 0;
		byte temp_left_cnt	= 0;
		byte temp_right_cnt = 0;
		byte black_tile_cnt = 0;
		boolean front_wall_flag = false;
		boolean victim_search_flag = (!pass) ? true : false; 
		int touch_right_cnt = 0;
		int touch_left_cnt = 0;
		final byte BLACK_CNT = 5;
		int pwr = 80;
		stop();
//		sensor.resetGyroValue();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		if(!pass) mbed.toggleLedGreen(true);
		int firstDirec = sensor.getGyroValue();
		if (!pass && first_floor) {
			mbed.readAllSensors();
			if (!isWallFront()) {
				mbed.readSonicSensor();
				Delay.msDelay(30);
//				requestToMbedSensors();
				mbed.readAllSensors();
				if (mbed.sonicValue > 80) {
					downRamp();
					return DOWN_RAMP;
				}
			}
		}
		while(true) {
			//回転角の取得
			rotate = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
			if(rotate >= TILE_TACHO) break;
			mbed.readAllSensors();
			//壁衝突
			if(sensor.isLeftTouchPressed() && sensor.isRightTouchPressed()) {
				if (!pass) front_wall_flag = true;
				break;
			} else if(sensor.isLeftTouchPressed()) {
				touch_left_cnt ++;
				if (touch_left_cnt > 0) {
					avoidWall(false, 100);
					if(rotate >= (TILE_TACHO-180)) break;
					touch_left_cnt = 0;
				}
			} else if(sensor.isRightTouchPressed()) {
				touch_right_cnt ++;
				if (touch_right_cnt > 0) {
					avoidWall(true, 100);
					if(rotate >= (TILE_TACHO-180)) break;
					touch_right_cnt = 0;
				}
			}
//			else {
				touch_left_cnt = 0;
				touch_right_cnt = 0;
				//未通過の場合
				if(!pass) {
					//黒タイル
					if(sensor.getLightValue() < LIGHT_BLACK_THRESHOLD) {
						black_tile_cnt ++;
						if(black_tile_cnt >= BLACK_CNT) break;
					} else {
						black_tile_cnt = 0;
					}
					if (rotate > (TILE_TACHO-180)) {
						//被災者検知
						if(victim_search_flag) {
							if(mbed.distLeftValue < 100 && mbed.tempLeftValue > TEMP_THRESHOLD) {
								temp_right_cnt = 0;
								temp_left_cnt ++;
								if(temp_left_cnt > 0) {
									/* 被災者発見(左側) */
									changeDirectionUsingGyro(firstDirec, 9000);
									mbed.dropRescueKit();
									changeDirectionUsingGyro(firstDirec, 0);
									victim_search_flag = false;
								}
							} else if(mbed.distRightValue < 100 && mbed.tempRightValue > TEMP_THRESHOLD) {
								temp_left_cnt = 0;
								temp_right_cnt ++;
								if(temp_right_cnt > 0) {
									/* 被災者発見(右側) */
									changeDirectionUsingGyro(firstDirec, -9000);
									mbed.dropRescueKit();
									changeDirectionUsingGyro(firstDirec, 0);
									victim_search_flag = false;
								}
							} else {
								temp_left_cnt = 0;
								temp_right_cnt = 0;
							}
						}
					}
				}
				//左右距離制御
//				if(mbed.distLeftValue > 0 && mbed.distRightValue > 0) {
					//直進補正(壁が無い時)
					int gyro = sensor.getGyroValue();
					if (gyro < 0) gyro += 36000;
					gyro -= firstDirec;
					if (gyro < 0) gyro += 36000;
					gyro -= this.offset;
					if (gyro < 0) gyro += 36000;
					pwr = (gyro > 33000 || gyro < 3000) ? 60 : 
						  (gyro > 31500 || gyro < 4500) ? 70 : 100;
					leftMotor.setPower(pwr);
					rightMotor.setPower(pwr);
					if (gyro < 300 || gyro > 35700) {
						leftMotor.setPower(80);
						rightMotor.setPower(80);
						
						leftMotor.forward();
						rightMotor.forward();
						Delay.msDelay(30);
					}else if(gyro <= 18000) {
						leftMotor.backward();
						rightMotor.forward();
					} else {
						leftMotor.forward();
						rightMotor.backward();	
					}
//				} else {
//					leftMotor.setPower(80);
//					rightMotor.setPower(80);
//					leftMotor.forward();
//					rightMotor.forward();
//				}
//				Delay.msDelay(10);
//			}
		}
		stop();
		mbed.toggleLedGreen(false);
		if (((rotate > TILE_TACHO/2) || !front_wall_flag) && black_tile_cnt < BLACK_CNT) {
//			if (temp_right_cnt > 0) {
//				Delay.msDelay(30);
//				mbed.readAllSensors();
//				if (isWallRight()) {
//					changeDirectionUsingGyro(firstDirec, -9000);
//					mbed.dropRescueKit();
//					changeDirectionUsingGyro(firstDirec, 0);
//				}
//			} else if (temp_left_cnt > 0) {
//				Delay.msDelay(30);
//				mbed.readAllSensors();
//				if (isWallLeft()) {
//					changeDirectionUsingGyro(firstDirec, 9000);
//					mbed.dropRescueKit();
//					changeDirectionUsingGyro(firstDirec, 0);
//				}
//			}
			if(!pass && first_floor) {
				Delay.msDelay(500);
				if(isTiltUp()) {
//					Sound.beepSequence();
					upRamp();
					return UP_RAMP;
				}
			}
			if(sensor.getLightValue() > LIGHT_SILVER_THRESHOLD && sensor.getLightValue() < 90) {
				Sound.buzz();
				return SILVER;
			} else {
				return WHITE;
			}
		}
		while(true) {
			rotate = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
			if(rotate <= 0) break;
			leftMotor.setPower(60);
			rightMotor.setPower(60);
			leftMotor.backward();
			rightMotor.backward();
		}
		stop();
		if (front_wall_flag) return WALL;
		return BLACK;
	}

	/**
	 * ジャイロのoffset値を更新
	 * @param direction : 目標角度
	 */
	private void setGyroOffset(int direction) {
		offset = 0; //direction - sensor.getGyroValue();
	}
	
	/**
	 * 90度右回転
	 * @param pass : true)通過済み, false)未通過・被災者探索(左側)
	 */
	public void turnRight(boolean pass) {
		final int speed = 80;
//		boolean back_wall_flag = false;
		int temp_cnt = 0;
		mbed.toggleLedGreen(false);
		if(!pass) mbed.toggleLedGreen(true);
		setParallel();
//		if(isWallLeft()) back_wall_flag = true;
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		int firstDirec = sensor.getGyroValue();
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		while(!pass) {
			mbed.readAllSensors();
			if (leftMotor.getTachoCount() > 360) break;
			if(mbed.distLeftValue < 100 && mbed.tempLeftValue >= TEMP_THRESHOLD) {
				temp_cnt ++;
				if(temp_cnt > 0) {
					/* 被災者発見 */
					changeDirectionUsingGyro(sensor.getGyroValue(), 9000);
					mbed.dropRescueKit();
					pass = true;
				} 
			} else {
				temp_cnt = 0;
			}
			leftMotor.forward();
			rightMotor.backward();
			Delay.msDelay(10);
		}
		changeDirectionUsingGyro(firstDirec, 9000);
		mbed.toggleLedGreen(false);
		this.setGyroOffset(9000);
//		if(back_wall_flag) backWall();
	}
	
	/**
	 * 90度左回転
	 * @param pass : true)通過済み, false)未通過・被災者探索(右側)
	 */
	public void turnLeft(boolean pass) {
		final int speed = 80;
//		boolean back_wall_flag = false;
		int temp_cnt = 0;
		mbed.toggleLedGreen(false);
		if(!pass) mbed.toggleLedGreen(true);
		setParallel();
//		if(isWallRight()) back_wall_flag = true;
		int firstDirec = sensor.getGyroValue();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		while(!pass) {
			mbed.readAllSensors();
			if (rightMotor.getTachoCount() > 360) break;
			if(mbed.distRightValue < 100 && mbed.tempRightValue >= TEMP_THRESHOLD) {
				temp_cnt ++;
				if(temp_cnt > 0) {
					/* 被災者発見 */
					changeDirectionUsingGyro(sensor.getGyroValue(), -9000);
					mbed.dropRescueKit();
					pass = true;
				} 
			} else {
				temp_cnt = 0;
			}
			leftMotor.backward();
			rightMotor.forward();
			Delay.msDelay(10);
		}
		changeDirectionUsingGyro(firstDirec, -9000);
		mbed.toggleLedGreen(false);
		this.setGyroOffset(-9000);
//		if(back_wall_flag) backWall();
	}
	
	/**
	 * 目標角度を0とした方向修正
	 * @param direction : 目標角度(-18000 ~ 18000)
	 * @param pass : true)通過済み, false)未通過・被災者探索
	 */
	public void changeDirectionUsingGyro (int offset, int direction) {
		int gyro = 0;
		if (direction < 0) direction += 36000;
		while(true) {
			gyro = sensor.getGyroValue();	if (gyro < 0) gyro += 36000;
			gyro -= offset;						if (gyro < 0) gyro += 36000;
			gyro -= direction;					if (gyro < 0) gyro += 36000;
			int pwr = (gyro > 33000 || gyro < 3000) ? 60 : 
					  (gyro > 31500 || gyro < 4500) ? 80 : 100;
//			LCD.clear();
//			LCD.drawInt((int)gyro, 0, 0);
			leftMotor.setPower(pwr);
			rightMotor.setPower(pwr);
			if (gyro < 400 || gyro > 35600) break;
			if(gyro <= 18000) {
				leftMotor.backward();
				rightMotor.forward();
			} else {
				leftMotor.forward();
				rightMotor.backward();	
			}
		}
		stop();
		this.offset = gyro;
	}
	
	public void end() {
		while(true) {
			mbed.toggleLedBlue(true);
			Delay.msDelay(500);
			mbed.toggleLedBlue(false);
			Delay.msDelay(500);
		}
	}
		
	/**
	 * 全てのセンサーの値表示・LED、サーボお試し
	 */
	public void sensorSetup() {
//		mbed.debugLeds();
//		mbed.debugServo();
		mbed.debugAllSensors();
//		mbed.readAllSensors();
		sensor.debugSensors();
//		debugWall();
	}
}