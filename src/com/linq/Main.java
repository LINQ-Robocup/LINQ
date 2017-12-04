package com.linq;

import lejos.nxt.*;
import lejos.nxt.comm.RS485;

import java.lang.Math;

//import lejos.nxt.comm.RConsole;
import lejos.util.Delay;

public class Main {

	public static void main(String[] args) {
		/* �C���X�^���X�����E������ */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		MapInfo map = new MapInfo(1, 1);																																																																	
//		RConsole.openUSB(5000);

		/* �Z���T�[���̃f�o�b�O�o�� */
		motion.sensorSetup();
		
		/* �ɂ��܂�̂����т� */
		while(!Button.ESCAPE.isDown()){
			motion.mbed.readRaspi(1);
			LCD.clear();
			LCD.drawInt(motion.mbed.cameraLeftValue, 1, 1);
			LCD.drawInt(motion.mbed.cameraRightValue, 1, 2);
			LCD.drawInt(motion.mbed.distFrontLeftValue, 1, 5);
			LCD.drawInt(motion.mbed.dummyValue, 1, 7);
			Delay.msDelay(1000);
			
			motion.mbed.readRaspi(2);
			LCD.clear();
			LCD.drawInt(motion.mbed.cameraLeftValue, 1, 1);
			LCD.drawInt(motion.mbed.cameraRightValue, 1, 2);
			LCD.drawInt(motion.mbed.distFrontLeftValue, 1, 5);
			LCD.drawInt(motion.mbed.dummyValue, 1, 7);
			Delay.msDelay(1000);
		}
//		while(!Button.ESCAPE.isDown()){
//			byte get[] = new byte[1];
//			RS485.hsRead(get, 0, 1);
//			LCD.clear();
//			LCD.drawInt(get[0], 1, 1);
//			Delay.msDelay(10);
//		}

//		motion.leftMotor.setPower(50);
//		motion.rightMotor.setPower(50);
//		motion.leftMotor.backward();
//		motion.rightMotor.forward();
//		while(!Button.ENTER.isDown()) {
//			Delay.msDelay(1);
//			motion.mbed.readAllSensors();
//
////			LCD.clear();
////			LCD.drawInt(motion.mbed.distFrontLeftValue, 1, 1);
////			LCD.drawInt(motion.mbed.distFrontRightValue, 1, 2);
//			int a = motion.mbed.distFrontLeftValue - motion.mbed.distFrontRightValue + 4;
//			a = a*2;
////			LCD.drawInt(a, 1, 3);
//			double d = a / 120.0;
//			int val = (int) (Math.tan(d)*180/3.14);
////			LCD.drawInt(val, 1, 5);
//			if(val > 30) val = 30;
//			else if(val < -30) val = -30;
//			int power = 40 + val / 30 * 40;
//			motion.leftMotor.setPower(power);
//			motion.rightMotor.setPower(power);
//			if(val < -3 || val > 3) {
//				if(val < 0) {
//					motion.leftMotor.backward();
//					motion.rightMotor.forward();	
//				}else{
//					motion.leftMotor.forward();
//					motion.rightMotor.backward();	
//				}
//			}else{
//				motion.leftMotor.stop();
//				motion.rightMotor.stop();
//			}
//		}

		/* �}�b�v���̃����[�h */
		if(map.reload()) map.setCurPosInfo(MapInfo.UNKNOWN);
		
		/* ���H�T�� */
		map.setWallBack(motion.isWallBack() ? MapInfo.WALL : MapInfo.FLAG);
		while(true) {
			//�Ǐ��̎擾(�V�K)
			if(map.getCurTileInfo() == MapInfo.UNKNOWN) {
				motion.requestToMbedSensors();
				if(map.getWallRight() == MapInfo.UNKNOWN) {
					map.setWallRight(motion.isWallRight() ? MapInfo.WALL : MapInfo.FLAG);
				}
				if(map.getWallFront() == MapInfo.UNKNOWN) {
					map.setWallFront(motion.isWallFront() ? MapInfo.WALL : MapInfo.FLAG);
				}
				if(map.getWallLeft() == MapInfo.UNKNOWN) {
					map.setWallLeft(motion.isWallLeft() ? MapInfo.WALL : MapInfo.FLAG);
				}
				//�}�b�v�̐��`
				map.arrangeMap();
				map.dispMapInfo();
			}

			byte direction = 0;
			//�i�s�����̌���
			byte minVal = map.getWallRight();
			byte curVal = 0;
			//�ʉ߉񐔂����Ȃ�������I��
			curVal = map.getWallLeft();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)2;
			}
			curVal = map.getWallFront();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)1;
			}
			curVal = map.getWallRight();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)0;
			}
			curVal = map.getWallBack();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)3;
			}
			
			//�����]��
			switch(direction) {
				case 0: //�E
					motion.turnRight(map.isPassedThrough());
					map.changeDirec(true);
					break;
				case 2: //��
					motion.turnLeft(map.isPassedThrough());
					map.changeDirec(false);
					break;
				case 3: //��
					if(motion.compSideDist() > 0) {
						motion.turnLeft(map.isPassedThrough());
						motion.sensor.resetGyroValue();
						motion.turnLeft(map.isPassedThrough());
						map.changeDirec(false);
						map.changeDirec(false);
					} else {
						motion.turnRight(map.isPassedThrough());
						motion.sensor.resetGyroValue();
						motion.turnRight(map.isPassedThrough());
						map.changeDirec(true);
						map.changeDirec(true);
					}
					break;
				default:
			}
			map.dispMapInfo();
			
			//�^�C���ړ�
			if(map.getWallFront() != MapInfo.WALL) {
				byte result = (byte)(motion.tileForward(map.getWallFront() == 1 ? false : true, map.curRoom == 0 ? true : false));
				map.setCurPosInfo(MapInfo.PASS);
				if(result == LQMover.WALL) {
					map.setWallFront(MapInfo.WALL);
				} else if(result == LQMover.BLACK) {
					map.setFrontBlack();
				} else { 
					if(result == LQMover.RAMP) {
						map.setDoorwayExit();
						Sound.beepSequenceUp();
						map.changeNextRoom();
					} else {
						map.setWallFront((byte)(map.getWallFront()+1));
						map.moveNextPosition();
						if(result == LQMover.SILVER) {
							map.writeFile();
						}
						if(map.isReachingFlag()) {
							map.resetDistanceMap();
						} else if(map.isStartTile()){
							map.resetDistanceMap();
							if(map.curRoom == 0 && map.getCurTileInfo() > 0) {
								break;
							} else {
								map.changePrevRoom();
								motion.setParallel();
								motion.turnRight(true);
								motion.tileForward(true, false);
								motion.upRamp();
							}
						}
					}
				}
			}
			map.dispMapInfo();
		}
	}
}