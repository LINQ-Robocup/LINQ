package com.linq;

import lejos.nxt.MotorPort;

public class Motion {
	// Create Fields
	static NXTMotors leftMotor;
	static NXTMotors rightMotor;
	MbedSensors mbed;
	NXTSensors sensor;

	// offset 
	int gyroOffset = 0;
	
	//30cm進むためのタコメータカウント
	private static final int TILE_TACHO = 720;
	
	//各種動作における方向管理用
	public static final byte LEFT	= 0;
	public static final byte RIGHT	= 1;
	public static final byte FRONT	= 2;
	public static final byte BACK	= 3;
	
	enum Tile{WHITE, BLACK, RAMP, CHECK_POINT, WALL;}
	
	byte temp_left_cnt	= 0;
	byte temp_right_cnt = 0;
	
	public Motion() {
		//Create Instances
		rightMotor = new NXTMotors(MotorPort.B);
		leftMotor = new NXTMotors(MotorPort.A);
		mbed = new MbedSensors();
		sensor = new NXTSensors();
		sensorSetup();
	}
	
	/**
	 * mbedにセンサ情報を要求
	 */
	public void requestToMbedSensors() {
		mbed.readAllSensors();
	}
	
	
	public boolean isWallRight() {
		return (mbed.distRightValue < 127);
	}
	public boolean isWallFront() {
		return (mbed.distFrontLeftValue < 127 && mbed.distFrontRightValue < 127);
	}
	public boolean isWallLeft() {
		return (mbed.distLeftValue < 127);
	}
	
	
	public void stop() {
		rightMotor.setPower(0);
		leftMotor.setPower(0);
	}
	
	Tile tileMovement(boolean pass, int speed) {
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		while ((rightMotor.getTachoCount()+leftMotor.getTachoCount())/2 < TILE_TACHO) {
			rightMotor.forward();
			leftMotor.forward();
		}
		stop();
		gyroOffset = sensor.getGyroValue() - gyroOffset;
		return Tile.WHITE;
	}

	void turnRight(boolean pass, int speed) {
		final int target = 8000;
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		while(true) {
			int gyro = sensor.getGyroValue() - gyroOffset;
			if (gyro <= -18000) gyro += 36000;
			else if (gyro > 18000) gyro -= 36000;
			if (gyro > target) break;
			rightMotor.backward();
			leftMotor.forward();
		}
		stop();
		setGyroOffset(target);
	}
	
	void turnLeft(boolean pass, int speed) {
		final int target = -8000;
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		while(true) {
			int gyro = sensor.getGyroValue() - gyroOffset;
			if (gyro <= -18000) gyro += 36000;
			else if (gyro > 18000) gyro -= 36000;
			if (gyro < target) break;
			rightMotor.forward();
			leftMotor.backward();
		}
		setGyroOffset(target);
	}
	
	void turn(boolean pass, int speed) {
		turnLeft(pass, speed);
		turnLeft(pass, speed);
	}
	
	void resetGyroOffset() {
		sensor.resetGyroValue();
		gyroOffset = 0;
	}
	
	void setGyroOffset(int target) {
		gyroOffset = sensor.getGyroValue() - target - gyroOffset;
		if (gyroOffset <= -18000) gyroOffset += 36000;
		else if (gyroOffset > 18000) gyroOffset -= 36000;
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
	}
}
