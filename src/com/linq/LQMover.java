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
	
	//���x�̂������l
	int tempThreshold = 25;
	
	//30cm�i�ނ��߂̃^�R���[�^�J�E���g
	static final int tileTacho = 690;
	
	//�e�퓮��ɂ���������Ǘ��p
	public final byte LEFT		= 0;
	public final byte RIGHT		= 1;
	public final byte FRONT		= 2;
	public final byte BEHIND	= 3;
	
	//���̐F�Ǘ�
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
	 * �p������(�O���̕ǂƕ��s�ɂȂ�悤�ɏC��)
	 */
	public void setParallel(int speed) {
		leftMotor.stop();
		rightMotor.stop();
		sensor.readAllSensors();
		int leftOffset = sensor.irDistFLeftValue;
		int rightOffset = sensor.irDistFRightValue;
		
		if(!sensor.isWallFront()) {
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
			//offset = 0;
		}
		
		int dist = (sensor.irDistFLeftValue + sensor.irDistFRightValue) / 2;
		if(dist < 44 || dist > 46) {
			setDistance(speed-5);
		}
		leftMotor.stop();
		rightMotor.stop();
	}
	
	/**
	 * �^�C���̒��S�Ɉړ�(�O���̕ǂƂ̋������C��)
	 */
	public void setDistance(int speed) {
		while(true) {
			sensor.readAllSensors();
			int dist = sensor.srValue;//(sensor.irDistFLeftValue + sensor.irDistFRightValue) / 2;
			if(!sensor.isWallFront()) {
				break;
			} else if(Math.abs(dist-8) > 3) {
				leftMotor.setPower(speed);
				rightMotor.setPower(speed);
				if(dist > 8) {
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
//		int flatThreshold = -50;
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		int rampCount = 0;
		boolean didRescue = false;
		while (true) {
			sensor.readAllSensors();
			if(sensor.srValue < 7) {
				rampCount++;
				if(rampCount > 3) {
					break;
				}
			}else{
				rampCount = 0;
			}
			/* rescue */
			if(didRescue == false && sensor.tempLeftValue > tempThreshold) {
				leftMotor.stop();
				rightMotor.stop();
				Sound.beepSequence();
				sensor.blinkLED();
				didRescue = true;
			}
			if(didRescue == false && sensor.tempRightValue > tempThreshold) {
				leftMotor.stop();
				rightMotor.stop();
				Sound.beepSequence();
				sensor.blinkLED();
				didRescue = true;
			}
			
			/* avoid wall */
			if(sensor.isLeftTouchPressed()) {
				leftMotor.setPower(speed);
				rightMotor.setPower((int)(speed*0.7));
				leftMotor.forward();
				rightMotor.forward();
				Delay.msDelay(250);
				leftMotor.setPower(speed);
				rightMotor.setPower(speed);
			}
			if(sensor.isRightTouchPressed()) {
				leftMotor.setPower((int)(speed*0.7));
				rightMotor.setPower(speed);
				leftMotor.forward();
				rightMotor.forward();
				Delay.msDelay(250);
				leftMotor.setPower(speed);
				rightMotor.setPower(speed);
			}

			/* forward */
			leftMotor.forward();
			rightMotor.forward();
		}
		Sound.beepSequence();
		while (!sensor.isLeftTouchPressed() || !sensor.isRightTouchPressed()) {
			leftMotor.forward();
			rightMotor.forward();
		}
		
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		
		leftMotor.setPower(50);
		rightMotor.setPower(50);
		while ( (leftMotor.getTachoCount() + rightMotor.getTachoCount()) /2 > -180) {
			leftMotor.backward();
			rightMotor.backward();
		}
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
	}
	
	public void downRamp() {
		int speed = 40;
		int rampCount = 0;
		leftMotor.setPower(speed);
		rightMotor.setPower(speed);
		
		if(sensor.isWallRight()) {
			turnLeft(true);
		}else{
			turnRight(false);
		}
		while (true) {
			if(sensor.getValue(sensor.SRDIST) < 6) {
				rampCount++;
				if(rampCount > 5) break;
			}else{
				rampCount = 0;
			}
			
			/* avoid wall */
			if(sensor.isLeftTouchPressed()) {
				rightMotor.setPower(65);
				leftMotor.stop();
				rightMotor.backward();
				rightMotor.setPower(speed);
				Delay.msDelay(100);
			}
			if(sensor.isRightTouchPressed()) {
				leftMotor.setPower(65);
				leftMotor.backward();
				rightMotor.stop();
				leftMotor.setPower(speed);
				Delay.msDelay(100);
			}
			/* forward */
			leftMotor.forward();
			rightMotor.forward();
		}
		leftMotor.stop();
		rightMotor.stop();
		setParallel(40);
		turnRight(true);
		setParallel(40);
		turnLeft(true);
		setParallel(40);
		
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
		byte blackCount = 0;
		byte tempLeftCount = 0;
		byte tempRightCount = 0;
		int tempThreshold = 30;
		byte rampUpCount = 0;
		int rampUpthreshold = -300;
		
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.resetTachoCount();
		rightMotor.resetTachoCount();
		sensor.resetGyroValue();
		if(pass == false) sensor.ledGreen(true);
		for(int i = 1; i <= 10; i ++) {
			sensor.readAllSensors();
			sensor.getGyroValue();
			
			/* black */
			if(sensor.getLightValue() < 20) {
				blackCount++;
				if(blackCount > 2) {
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

						sensor.blinkLED();
						sensor.servoRight();
						
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
				boolean leftTouch = sensor.isLeftTouchPressed();
				boolean rightTouch = sensor.isRightTouchPressed();
				if(leftTouch) {
					avoidWall(LEFT);
				}else if(rightTouch){
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
			if(sensor.getAccelYValue() < rampUpthreshold) {
				rampUpCount++;
				if(rampUpCount >= 2) {
					climbRamp(speed);
					LCD.clear();
					while (!Button.ENTER.isDown()) {
						LCD.drawString("RAMP", 5, 5);
					}
					return RAMP;
				}
			}else{
				break;
			}
		}
		/* silver */
		int silverThreshold = 28;
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
		sensor.ledGreen(false);
		return WHITE;
	}

	/**
	 * �W���C����offset�l���X�V
	 * @param direction : �ڕW�p�x
	 */
	private void setOffset(int direction) {
		offset = direction - sensor.getGyroValue();
	}
	
	/**
	 * 90�x�E��]
	 * @param pass : true)�ʉߍς�, false)���ʉ߁E��ЎҒT��(����)
	 */
	public void turnRight(boolean pass) {
		final int ANGLE = 9000;//���v���90�x
		leftMotor.stop();
		rightMotor.stop();
		sensor.resetGyroValue();
		if(pass == false)
			sensor.ledGreen(true);
		rotate(ANGLE, pass);
		leftMotor.stop();
		rightMotor.stop();
		setOffset(ANGLE-offset);
		sensor.ledGreen(true);
		if(sensor.isWallFront()) {
			setParallel(45);
			
		}
	}
	
	/**
	 * 90�x����]
	 * @param pass : true)�ʉߍς�, false)���ʉ߁E��ЎҒT��(�E��)
	 */
	public void turnLeft(boolean pass) {
		final int ANGLE = -9000;//�����v���90�x
		leftMotor.stop();
		rightMotor.stop();
		sensor.resetGyroValue();
		if(pass == false) 
			sensor.ledGreen(true);
		rotate(ANGLE, pass);
		leftMotor.stop();
		rightMotor.stop();
		setOffset(ANGLE-offset);
		sensor.ledGreen(false);
		if(sensor.isWallFront()) {
			setParallel(45);
		}
	}
	
	/**
	 * �ڕW�p�x��0�Ƃ��������C��
	 * @param direction : �ڕW�p�x(-18000 ~ 18000)
	 * @param pass : true)�ʉߍς�, false)���ʉ߁E��ЎҒT��
	 */
	private void rotate (int direction, boolean pass) {
		//sensor.ledYellow(true);
		int curDirection = 0;
		byte tmp_cnt = 0;
		byte speed = 40;
		while(true) {
			/* �ڕW�p�x��0�ɂȂ�悤�� curDirection�̒l��ݒ� */
			int gyro = sensor.getGyroValue();
			curDirection = gyro - direction - offset;
			if(Math.abs(curDirection) > 18000) {
				curDirection += (curDirection > 0) ? -36000 : 36000; 
			}
			/* �ڕW�p�x�ɓ��B���邽�߂̕����C�� */
			if(curDirection > -500 && curDirection < 500) {
				break;
			} else {
				if(pass == false) {
					/* �M�����m(���ʉ߂̂�) */
					sensor.readAllSensors();
					if(direction < 0 && sensor.irDistRightValue < 80 && sensor.tempRightValue > tempThreshold) {
						if(tmp_cnt >= 2) {
							//��ЎҌ��m(����], �E��)
							rotate(gyro - 9000, true);//���݂̊p�x-90�x��](��Ў҂ɔw��������)
							leftMotor.stop();
							rightMotor.stop();
							//LED�_�ŁE���X�L���[�L�b�g�r�o
							sensor.blinkLED();
							sensor.servoLeft();
//							Delay.msDelay(2000);
							pass = true;
						}
						tmp_cnt ++;
					} else if(direction > 0 && sensor.irDistLeftValue < 80 && sensor.tempLeftValue > tempThreshold) {
						if(tmp_cnt >= 2) {
							//��ЎҌ��m(�E��], ����)
							rotate(gyro + 9000, true);//���݂̊p�x+90�x��](��Ў҂ɔw��������)
							leftMotor.stop();
							rightMotor.stop();
							//LED�_�ŁE���X�L���[�L�b�g�r�o
							sensor.blinkLED();
							sensor.servoRight();
//							Delay.msDelay(2000);
							pass = true;
						}
						tmp_cnt ++;
					} else {
						tmp_cnt = 0;
					}
				}
				// �ڕW�p�x�Ƃ̍��ɔ�Ⴕ��speed��ݒ�(�Œ�:50)
				speed = (byte) (60 + Math.abs(curDirection) / 18000 * 40);
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
		//sensor.ledYellow(false);
	}
}