package com.linq;

import lejos.nxt.*;
//import lejos.nxt.comm.RConsole;

public class Main {
	enum Direc{RIGHT, LEFT, FRONT, BACK;}
	
	public static void main(String[] args) {
		/* �C���X�^���X�����E������ */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		MotionToMap map = new MotionToMap();
//		RConsole.openUSB(5000);

		/* �Z���T�[���̃f�o�b�O�o�� */
		motion.sensorSetup();
		
		/* �}�b�v���̃����[�h */
		if(map.reload()) map.setCurPosInfo(Map.UNKNOWN);
		
		/* ���H�T�� */
		map.setPathBack(motion.isWallBack() ? Map.WALL : Map.FLAG);
		while(true) {
			//�Ǐ��̎擾(�V�K)
			if (!map.isTilePassed()) {
				// �Ǐ��̎擾(�V�K)
				map.updateRealWallInfo();
				if (map.getPathRight() == Map.UNKNOWN) {
					map.setPathRight(map.real.isWallRight() ? Map.WALL : Map.FLAG);
				}
				if (map.getPathFront() == Map.UNKNOWN) {
					map.setPathFront(map.real.isWallFront() ? Map.WALL : Map.FLAG);
				}
				if (map.getPathLeft() == Map.UNKNOWN) {
					map.setPathLeft(map.real.isWallLeft() ? Map.WALL : Map.FLAG);
				}
				// �}�b�v�̐��`
				map.arrangeMap();
				map.dispMapInfo();
			}
			
			// �i�s�����̌���
			Direc direc;
			while (true) {
				direc = Direc.BACK;
				byte path = map.getPathBack();
				if(map.getPathLeft() >= path) {
					path = map.getPathLeft();
					direc = Direc.LEFT;
				}
				if(map.getPathFront() >= path) {
					path = map.getPathFront();
					direc = Direc.FRONT;
				}
				if(map.getPathRight() >= path) {
					path = map.getPathRight();
					direc = Direc.RIGHT;
				}
				// �S�Ēʉߍς݂̏ꍇ(�ŒZ��FLAG�܂ł̋�����z��ɑ��)
				if (path <= Map.PASS) {
					Sound.buzz();
					map.searchFlag();
					map.dispMapInfo();
				} else {
					if (path == Map.FLAG) map.resetDistanceMap();
					break;
				}
			}
			map.dispMapInfo();
			
			//�����]��
			switch (direc) {
				case RIGHT: map.turnRight(); break;
				case LEFT : map.turnLeft(); break;
				case BACK : map.turn(); break;
				default: break;
			}
			map.dispMapInfo();

			//�^�C���ړ�
			if (!map.isFrontWall()) {
				map.move();
				map.setPathBack(Map.PASS);
				map.dispMapInfo();
				if (map.getTile() == Map.FLAG) {
					if (map.isFirstRoom()) break;
					map.movePrevRoom();
				}
			}
		}
	}
}