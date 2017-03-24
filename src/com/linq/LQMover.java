package com.linq;

import java.lang.Math;

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
	static LQNXTSensors sensor;

	// offset 
	int offset = 0;
	
	//�Z���T臒l 
	private static final byte TEMP_THRESHOLD = 30;
	private static final byte LIGHT_BLACK_THRESHOLD = 25;
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
		this.mbed.readSonicSensor();
		Delay.msDelay(10);
		this.mbed.readAllSensors();
	}
	
	/**
	 * �E���ɕǂ����邩?
	 * @return �ǂ�����: true �ǂ��Ȃ�: false
	 */
	public boolean isWallRight() {
		return (mbed.distRightValue < 127) ? true : false;
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
			back = isWallLeft() ? true : false;
			if(isWallFront()) setParallel();
		} else {
			changeDirectionUsingGyro(90);
			mbed.readAllSensors();
			back = isWallRight() ? true : false;
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
		return (sensor.getAccelYValue() > 200 && sensor.getAccelZValue() > -1000) ? true : false;
	}
	
	private boolean isTiltDown() {
		return (sensor.getAccelYValue() < -1000 && sensor.getAccelZValue() < -98) ? true : false;
	}
	
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
				rightMotor.setPower(80);
				leftMotor.setPower(0);
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

	
	public void upRamp() {
		int speed = 80;
		byte not_tilt_cnt = 0;
		while(true) {
			if(!isTiltUp()) {
				not_tilt_cnt ++;
				if(not_tilt_cnt > 10) break;
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
				if(mbed.distLeftValue >= 127) mbed.distLeftValue = 40;
				else if(mbed.distRightValue >= 127) mbed.distRightValue = 40;
				leftMotor.setPower(speed-compSideDist()/2);
				rightMotor.setPower(speed-compSideDist()/2);
			}
			Delay.msDelay(10);
		}
		setParallel();
	}
	
	public void downRamp() {
		Stopwatch timer = new Stopwatch();
		int speed = 50;
		byte not_tilt_cnt = 0;
		turnLeft(true);
		timer.reset();
		while(true) {
			if(timer.elapsed() > 3000) {
				if(!isTiltDown()) {
					not_tilt_cnt ++;
					if(not_tilt_cnt > 10) break;
				} else {
					not_tilt_cnt = 0;
				}
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
				if(mbed.distLeftValue >= 127) mbed.distLeftValue = 40;
				else if(mbed.distRightValue >= 127) mbed.distRightValue = 40;
				leftMotor.setPower(speed-compSideDist()/2);
				rightMotor.setPower(speed-compSideDist()/2);
			}
			Delay.msDelay(10);
			leftMotor.setPower(0);
			rightMotor.setPower(0);
		}
		setParallel();
	}
	
	public int tileForward(boolean pass) {
		final int speed = pass ? 90 : 80;
		int rotate = 0;
		byte temp_left_cnt	= 0;
		byte temp_right_cnt = 0;
		byte black_tile_cnt = 0;
		boolean front_wall_flag = false;
		boolean victim_search_flag = (!pass) ? true : false;
		if(!pass) mbed.toggleLedBlue(true); 
		stop();
		sensor.resetGyroValue();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		while(true) {
			//��]�p�̎擾
			rotate = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
			if(rotate > TILE_TACHO) break;
			//�ǏՓ�
			if(sensor.isLeftTouchPressed() && sensor.isRightTouchPressed()) {
				front_wall_flag = true;
				break;
			} else if(sensor.isLeftTouchPressed()) {
				avoidWall(false);
			} else if(sensor.isRightTouchPressed()) {
				avoidWall(true);
			} else {
				mbed.readAllSensors();
				//���ʉ߂̏ꍇ
				if(pass == false) {
					//���^�C��
					if(sensor.getLightValue() < LIGHT_BLACK_THRESHOLD) {
						black_tile_cnt ++;
						if(black_tile_cnt > 3) break;
					} else {
						black_tile_cnt = 0;
					}
					//��ЎҌ��m
					if(victim_search_flag) {
						if(mbed.tempLeftValue > TEMP_THRESHOLD) {
							temp_left_cnt ++;
							if(temp_left_cnt > 0) {
								/* ��ЎҔ���(����) */
								changeDirectionUsingGyro(90);
								mbed.dropRescueKit();
								changeDirectionUsingGyro(0);
								victim_search_flag = false;
							}
						} else if(mbed.tempRightValue > TEMP_THRESHOLD) {
							temp_left_cnt = 0;
							temp_right_cnt ++;
							if(temp_right_cnt > 0) {
								/* ��ЎҔ���(�E��) */
								changeDirectionUsingGyro(-90);
								mbed.dropRescueKit();
								changeDirectionUsingGyro(0);
								victim_search_flag = false;
							}
						} else {
							temp_left_cnt = 0;
							temp_right_cnt = 0;
						}
					}	
				}
				//���E��������
				if(mbed.distLeftValue >= 127 && mbed.distRightValue >= 127) {
					//���i�␳(�ǂ�������)
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
				} else {
					leftMotor.setPower(80);
					rightMotor.setPower(80);
//					if(mbed.distLeftValue >= 127) mbed.distLeftValue = 40;
//					else if(mbed.distRightValue >= 127) mbed.distRightValue = 40;
//					leftMotor.setPower(speed-compSideDist()/2);
//					rightMotor.setPower(speed-compSideDist()/2);
				}
			}
		}
		stop();
		mbed.toggleLedBlue(false);
		if(black_tile_cnt <= 3) {
//			if(pass != true) {
//				if(isTiltUp()) {
//					upRamp();
//					return RAMP;
//				}
//			}
			if(sensor.getLightValue() > LIGHT_SILVER_THRESHOLD) {
				Sound.buzz();
				return SILVER;
			} else {
				return WHITE;
			}
		}
		mbed.toggleLedGreen(true);
		while(true) {
			rotate = (leftMotor.getTachoCount() + rightMotor.getTachoCount()) / 2;
			if(rotate < 0) break;
			leftMotor.setPower(-speed);
			rightMotor.setPower(-speed);
		}
		stop();
		mbed.toggleLedGreen(false);
		Sound.beep();
		return BLACK;
	}

	/**
	 * �W���C����offset�l���X�V
	 * @param direction : �ڕW�p�x
	 */
	private void setGyroOffset(int direction) {
		offset = 0; //direction - sensor.getGyroValue();
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
				if(temp_cnt > 0) {
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
				if(temp_cnt > 0) {
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
		debugWall();
	}
}