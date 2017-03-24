package com.linq;

import java.lang.Math;

import lejos.nxt.Button;
import lejos.nxt.TachoMotorPort;
import lejos.util.Delay;

public class LQMover {
	// Create Fields
	static LQMotor2 leftMotor;
	static LQMotor2 rightMotor;
	static LQMbedSensors mbed;
	static LQNXTSensors sensor;

	// offset 
	int offset = 0;
	
	//センサ閾値 
	private static final byte TEMP_THRESHOLD = 30;
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
	public static final byte WHITE	= 0;
	public static final byte BLACK	= 1;
	public static final byte RAMP	= 2;
	public static final byte SILVER	= 3;
	public static final byte WALL   = 4;
	
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
		mbed.readSonicSensor();
		Delay.msDelay(10);
		mbed.readAllSensors();
	}
	
	/**
	 * 右側に壁があるか?
	 * @return 壁がある: true 壁がない: false
	 */
	public boolean isWallRight() {
		return (mbed.distRightValue < 127) ? true : false;
	}
	
	/**
	 * 前方に壁があるか?
	 * @return 壁がある: true 壁がない: false
	 */
	public boolean isWallFront() {
		return (mbed.distFrontLeftValue < 127 &&
				mbed.distFrontRightValue < 127) ? true : false;
	}
	
	/**
	 * 左側に壁があるか?
	 * @return 壁がある: true 壁がない: false
	 */
	public boolean isWallLeft() {
		return (mbed.distLeftValue < 127) ? true : false;
	}
	
	/**
	 * 後ろに壁があるか(90度回転して確認)
	 * @return
	 */
	public boolean isWallBack() {
		boolean back = false;
		stop();
		sensor.resetGyroValue();
		if(compSideDist() > 0) {
			changeDirectionUsingGyro(-90);
			mbed.readAllSensors();
			back = (mbed.distLeftValue > 0 && mbed.distLeftValue < 127) ? true : false;
			if(isWallFront()) setParallel();
		} else {
			changeDirectionUsingGyro(90);
			mbed.readAllSensors();
			back = (mbed.distRightValue > 0 && mbed.distLeftValue < 127) ? true : false;
			if(isWallFront()) setParallel();
		}
		changeDirectionUsingGyro(0);
		if(back) backWall();
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
		return (sensor.getAccelYValue() > 1000 && sensor.getAccelZValue() < -98) ? true : false;
	}
	
//	private boolean isTiltDown() {
//		return (sensor.getAccelYValue() < -1000 && sensor.getAccelZValue() < -98) ? true : false;
//	}
	
	/**
	 * 前方の壁と平行になるように修正
	 */
	public void setParallel() {
		//壁にタッチ
		mbed.toggleLedBlue(false);
		while(true) {
			if(!sensor.isLeftTouchPressed() && !sensor.isLeftTouchPressed()) {
				leftMotor.setPower(50);
				rightMotor.setPower(50);
			} else if(sensor.isLeftTouchPressed() && !sensor.isRightTouchPressed()) {
				leftMotor.setPower(0);
				rightMotor.setPower(80);
			} else if(!sensor.isLeftTouchPressed() && sensor.isRightTouchPressed()) {
				leftMotor.setPower(80);
				rightMotor.setPower(0);
			} else {
				break;
			}
		}
		stop();
		//中心に移動(回転)
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		while((leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2 > -120) {
			leftMotor.setPower(-50);
			rightMotor.setPower(-50);
		}
		stop();
		//ジャイロオフセットのリセット
		this.offset = 0;
	}
	
	public void backWall() {
		byte speed = 80;
		//壁に押しつけ(時間) 
		leftMotor.setPower(-speed);
		rightMotor.setPower(-speed);
		Delay.msDelay(500);
		stop();
		//傾き防止
//		leftMotor.setPower(50);
//		rightMotor.setPower(50);
//		Delay.msDelay(100);
//		stop();
		//中心に移動(回転)
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		while((leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2 < 180) {
			leftMotor.setPower(speed);
			rightMotor.setPower(speed);
		}
		stop();
		//ジャイロオフセットのリセット
		this.offset = 0;
	}
	
	public void avoidWall(boolean direction) {
		final int ANGLE = 120;
		final int POWER = 80;
		stop();
		int leftOffset	= leftMotor.getTachoCount();
		int rightOffset = rightMotor.getTachoCount();
		if(direction) {
			//right to left
			while(rightMotor.getTachoCount() - rightOffset > -ANGLE) {
				leftMotor.setPower(0);
				rightMotor.setPower(-POWER);
			}
			while(leftMotor.getTachoCount() - leftOffset > -ANGLE) {
				leftMotor.setPower(-POWER);
				rightMotor.setPower(0);
			}
			while(rightMotor.getTachoCount() < leftOffset) {
				leftMotor.setPower(0);
				rightMotor.setPower(POWER);
			}
			while(leftMotor.getTachoCount() < leftOffset) {
				leftMotor.setPower(POWER);
				rightMotor.setPower(0);
			}
		} else {
			//left to right
			while(leftMotor.getTachoCount() - leftOffset > -ANGLE) {
				leftMotor.setPower(-POWER);
				rightMotor.setPower(0);
			}
			while(rightMotor.getTachoCount() - rightOffset > -ANGLE) {
				leftMotor.setPower(0);
				rightMotor.setPower(-POWER);
			}
			while(leftMotor.getTachoCount() < leftOffset) {
				leftMotor.setPower(POWER);
				rightMotor.setPower(0);
			}
			while(rightMotor.getTachoCount() < leftOffset) {
				leftMotor.setPower(0);
				rightMotor.setPower(POWER);
			}
		}
		stop();
	}

	
	public void upRamp() {}
	public void downRamp() {}
	
	public int tileForward(boolean pass) {
		final int speed = pass ? 90 : 80;
		byte temp_left_cnt	= 0;
		byte temp_right_cnt = 0;
		byte black_tile_cnt = 0;
		byte tilt_up_cnt = 0;
		boolean front_wall_flag = false;
		if(!pass) mbed.toggleLedBlue(true); 
		stop();
		sensor.resetGyroValue();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		while(true) {
			//回転角の取得
			int rotate = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
			if(rotate > TILE_TACHO) break;
			//傾斜路
			if(isTiltUp()) {
				tilt_up_cnt ++;
				if(tilt_up_cnt > 2) return RAMP;
			} else {
				tilt_up_cnt = 0;
			}
			//黒タイル・壁衝突
			if(sensor.getLightValue() < LIGHT_BLACK_THRESHOLD) {
				black_tile_cnt ++;
				if(black_tile_cnt > 2) break;
				leftMotor.setPower(speed);
				rightMotor.setPower(speed);
			} else if(sensor.isLeftTouchPressed() && sensor.isRightTouchPressed()) {
				front_wall_flag = true;
				break;
			} else if(sensor.isLeftTouchPressed()) {
				avoidWall(false);
			} else if(sensor.isRightTouchPressed()) {
				avoidWall(true);
			} else {
				black_tile_cnt = 0;
				mbed.readAllSensors();
				if(pass == false) {
					//被災者検知(未通過のみ)
					if(mbed.tempLeftValue > TEMP_THRESHOLD) {
						/* 被災者発見(左側) */
						changeDirectionUsingGyro(90);
						mbed.dropRescueKit();
						changeDirectionUsingGyro(0);
						pass = true;
					} else if(mbed.tempRightValue > TEMP_THRESHOLD) {
						/* 被災者発見(右側) */
						changeDirectionUsingGyro(-90);
						mbed.dropRescueKit();
						changeDirectionUsingGyro(0);
						pass = true;
					}
				}
				//左右距離制御
				if(mbed.distLeftValue >= 127) mbed.distLeftValue = 40;
				if(mbed.
						
						
						distRightValue >= 127) mbed.distRightValue = 40;
				if(compSideDist() > 20) {
					leftMotor.setPower(speed);
					rightMotor.setPower(speed-10);
				} else if(compSideDist() < -20) {
					leftMotor.setPower(speed-10);
					rightMotor.setPower(speed);
				} else {
					//直進補正
					float gyro = sensor.getGyroValue() / 100 - this.offset;
					int pwr = (Math.abs((int)gyro) > 30) ? (Math.abs((int)gyro) > 45) ? 100 : 80 : 50;
					if(gyro > 3.0) {
						leftMotor.setPower(-pwr);
						rightMotor.setPower(pwr);
					} else if(gyro < -3.0) {
						leftMotor.setPower(pwr);
						rightMotor.setPower(-pwr);	
					} else {
						leftMotor.setPower(speed);
						rightMotor.setPower(speed);
					}
				}
			}
		}
		stop();
		mbed.toggleLedBlue(false);
		if(front_wall_flag == true) {
			return WALL;
		} else if(black_tile_cnt <= 2) {
			if(sensor.getLightValue() > LIGHT_SILVER_THRESHOLD) return SILVER;
			return WHITE;
		}
		mbed.toggleLedGreen(true);
		while(true) {
			int rotate = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
			if(rotate < 0) break;
			//直進補正
			float gyro = sensor.getGyroValue() / 100 - this.offset;
			int pwr = (Math.abs((int)gyro) > 30) ? (Math.abs((int)gyro) > 45) ? 100 : 80 : 50;
			if(gyro > 3.0) {
				leftMotor.setPower(-pwr);
				rightMotor.setPower(pwr);
			} else if(gyro < -3.0) {
				leftMotor.setPower(pwr);
				rightMotor.setPower(-pwr);	
			} else {
				leftMotor.setPower(-speed);
				rightMotor.setPower(-speed);
			}
		}
		stop();
		mbed.toggleLedGreen(false);
		return BLACK;
	}

	/**
	 * ジャイロのoffset値を更新
	 * @param direction : 目標角度
	 */
	private void setGyroOffset(int direction) {
		offset = direction - sensor.getGyroValue();
	}
	
	/**
	 * 90度右回転
	 * @param pass : true)通過済み, false)未通過・被災者探索(左側)
	 */
	public void turnRight(boolean pass) {
		final int speed = 80;
		boolean back_wall_flag = false;
		int temp_cnt = 0;
		if(!pass) mbed.toggleLedBlue(true);
		if(isWallLeft()) back_wall_flag = true;
		if(isWallFront()) setParallel(); 
		while(pass == false && sensor.getGyroValue() < 8000) {
			mbed.readAllSensors();
			if(mbed.tempLeftValue > TEMP_THRESHOLD) {
				temp_cnt ++;
				if(temp_cnt > 1) {
					/* 被災者発見 */
					changeDirectionUsingGyro(sensor.getGyroValue()/100 + 90);
					pass = true;
				} 
			} else {
				temp_cnt = 0;
			}
			leftMotor.setPower(speed);
			rightMotor.setPower(-speed);
			Delay.msDelay(10);
		}
		changeDirectionUsingGyro(90);
		mbed.toggleLedBlue(false);
		this.setGyroOffset(9000);
		if(back_wall_flag) backWall();
	}
	
	/**
	 * 90度左回転
	 * @param pass : true)通過済み, false)未通過・被災者探索(右側)
	 */
	public void turnLeft(boolean pass) {
		final int speed = 80;
		boolean back_wall_flag = false;
		int temp_cnt = 0;
		if(!pass) mbed.toggleLedBlue(true); 
		if(isWallRight()) back_wall_flag = true;
		if(isWallFront()) setParallel();
		while(pass == false && sensor.getGyroValue() > -8000) {
			mbed.readAllSensors();
			if(mbed.tempRightValue > TEMP_THRESHOLD) {
				temp_cnt ++;
				if(temp_cnt > 1) {
					/* 被災者発見 */
					changeDirectionUsingGyro(sensor.getGyroValue()/100 - 90);
					pass = true;
				} 
			} else {
				temp_cnt = 0;
			}
			leftMotor.setPower(-speed);
			rightMotor.setPower(speed);
			Delay.msDelay(10);
		}
		changeDirectionUsingGyro(-90);
		mbed.toggleLedBlue(false);
		this.setGyroOffset(-9000);
		if(back_wall_flag) backWall();
	}
	
	/**
	 * 目標角度を0とした方向修正
	 * @param direction : 目標角度(-18000 ~ 18000)
	 * @param pass : true)通過済み, false)未通過・被災者探索
	 */
	public void changeDirectionUsingGyro (int direction) {
		while(true) {
			/* 目標角度が0になるように curDirectionの値を設定 */
			float gyro = sensor.getGyroValue() / 100 - direction - this.offset;
			int pwr = (Math.abs((int)gyro) > 30) ? ((Math.abs((int)gyro) > 45) ? 100 : 80) : 50;
			if(gyro > 2.5) {
				leftMotor.setPower(-pwr);
				rightMotor.setPower(pwr);
			} else if(gyro < -2.5){
				leftMotor.setPower(pwr);
				rightMotor.setPower(-pwr);	
			} else {
				break;
			}
		}
		stop();
	}
		
	/**
	 * 全てのセンサーの値表示・LED、サーボお試し
	 */
	public void sensorSetup() {
		while(Button.ENTER.isDown());
//		mbed.debugLeds();
//		mbed.debugServo();
		mbed.debugAllSensors();
		sensor.debugSensors();
	}
}