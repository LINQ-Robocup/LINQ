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
	
	//�Z���T臒l 
	private static final byte TEMP_THRESHOLD = 30;
	private static final byte LIGHT_BLACK_THRESHOLD = 24;
	private static final byte LIGHT_SILVER_THRESHOLD = 30;
	
	//30cm�i�ނ��߂̃^�R���[�^�J�E���g
	private static final int TILE_TACHO = 690;
	
	//�e�퓮��ɂ���������Ǘ��p
	public static final byte LEFT	= 0;
	public static final byte RIGHT	= 1;
	public static final byte FRONT	= 2;
	public static final byte BACK	= 3;
	
	//���̎�ފǗ�
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
	 * mbed�ɃZ���T����v��
	 */
	public void requestToMbedSensors() {
		mbed.readSonicSensor();
		Delay.msDelay(10);
		mbed.readAllSensors();
	}
	
	/**
	 * �E���ɕǂ����邩?
	 * @return �ǂ�����: true �ǂ��Ȃ�: false
	 */
	public boolean isWallRight() {
		return (mbed.distRightValue < 127) ? true : false;
	}
	
	/**
	 * �O���ɕǂ����邩?
	 * @return �ǂ�����: true �ǂ��Ȃ�: false
	 */
	public boolean isWallFront() {
		return (mbed.distFrontLeftValue < 127 &&
				mbed.distFrontRightValue < 127) ? true : false;
	}
	
	/**
	 * �����ɕǂ����邩?
	 * @return �ǂ�����: true �ǂ��Ȃ�: false
	 */
	public boolean isWallLeft() {
		return (mbed.distLeftValue < 127) ? true : false;
	}
	
	/**
	 * ���ɕǂ����邩(90�x��]���Ċm�F)
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
	 * �쓮���[�^���~
	 */
	public void stop() {
		rightMotor.setPower(0);
		leftMotor.setPower(0);
	}
	
	/**
	 * ���E�̋����̔䗦(�ǂ��炪�ǂɋ߂���)
	 * @return �E���̋����Z���T�̒l-�����̋����Z���T�̒l
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
	 * �O���̕ǂƕ��s�ɂȂ�悤�ɏC��
	 */
	public void setParallel() {
		//�ǂɃ^�b�`
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
		//���S�Ɉړ�(��])
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		while((leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2 > -120) {
			leftMotor.setPower(-50);
			rightMotor.setPower(-50);
		}
		stop();
		//�W���C���I�t�Z�b�g�̃��Z�b�g
		this.offset = 0;
	}
	
	public void backWall() {
		byte speed = 80;
		//�ǂɉ�����(����) 
		leftMotor.setPower(-speed);
		rightMotor.setPower(-speed);
		Delay.msDelay(500);
		stop();
		//�X���h�~
//		leftMotor.setPower(50);
//		rightMotor.setPower(50);
//		Delay.msDelay(100);
//		stop();
		//���S�Ɉړ�(��])
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		while((leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2 < 180) {
			leftMotor.setPower(speed);
			rightMotor.setPower(speed);
		}
		stop();
		//�W���C���I�t�Z�b�g�̃��Z�b�g
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
			//��]�p�̎擾
			int rotate = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
			if(rotate > TILE_TACHO) break;
			//�X�ΘH
			if(isTiltUp()) {
				tilt_up_cnt ++;
				if(tilt_up_cnt > 2) return RAMP;
			} else {
				tilt_up_cnt = 0;
			}
			//���^�C���E�ǏՓ�
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
					//��ЎҌ��m(���ʉ߂̂�)
					if(mbed.tempLeftValue > TEMP_THRESHOLD) {
						/* ��ЎҔ���(����) */
						changeDirectionUsingGyro(90);
						mbed.dropRescueKit();
						changeDirectionUsingGyro(0);
						pass = true;
					} else if(mbed.tempRightValue > TEMP_THRESHOLD) {
						/* ��ЎҔ���(�E��) */
						changeDirectionUsingGyro(-90);
						mbed.dropRescueKit();
						changeDirectionUsingGyro(0);
						pass = true;
					}
				}
				//���E��������
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
					//���i�␳
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
			//���i�␳
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
	 * �W���C����offset�l���X�V
	 * @param direction : �ڕW�p�x
	 */
	private void setGyroOffset(int direction) {
		offset = direction - sensor.getGyroValue();
	}
	
	/**
	 * 90�x�E��]
	 * @param pass : true)�ʉߍς�, false)���ʉ߁E��ЎҒT��(����)
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
					/* ��ЎҔ��� */
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
	 * 90�x����]
	 * @param pass : true)�ʉߍς�, false)���ʉ߁E��ЎҒT��(�E��)
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
					/* ��ЎҔ��� */
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
	 * �ڕW�p�x��0�Ƃ��������C��
	 * @param direction : �ڕW�p�x(-18000 ~ 18000)
	 * @param pass : true)�ʉߍς�, false)���ʉ߁E��ЎҒT��
	 */
	public void changeDirectionUsingGyro (int direction) {
		while(true) {
			/* �ڕW�p�x��0�ɂȂ�悤�� curDirection�̒l��ݒ� */
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
	 * �S�ẴZ���T�[�̒l�\���ELED�A�T�[�{������
	 */
	public void sensorSetup() {
		while(Button.ENTER.isDown());
//		mbed.debugLeds();
//		mbed.debugServo();
		mbed.debugAllSensors();
		sensor.debugSensors();
	}
}