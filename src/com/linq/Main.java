package com.linq;

import lejos.nxt.*;
//import lejos.nxt.comm.RConsole;

public class Main {

	public static void main(String[] args) {
		/* �C���X�^���X�����E������ */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		MapInfo map = new MapInfo(1, 1);																																																																	
//		RConsole.openUSB(5000);

		/* �Z���T�[���̃f�o�b�O�o�� */
		motion.sensorSetup();
		
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
				byte result = (byte)(motion.tileForward(map.getWallFront() == 1 ? false : true));
				map.setCurPosInfo(MapInfo.PASS);
				if(result == LQMover.WALL) {
					map.setWallFront(MapInfo.WALL);
				} else if(result == LQMover.BLACK) {
					map.setFrontBlack();
				} else { 
					if(result == LQMover.RAMP) {
						map.setDoorwayExit();
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
								motion.downRamp();
								map.changePrevRoom();
							}
						}
					}
				}
			}
			map.dispMapInfo();
		}
	}
}