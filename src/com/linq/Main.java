package com.linq;

import lejos.nxt.*;
//import lejos.nxt.comm.RConsole;

public class Main {
	enum Direc{RIGHT, LEFT, FRONT, BACK;}
	
	public static void main(String[] args) {
		/* �C���X�^���X�����E������ */
//		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		MotionToMap map = new MotionToMap();
//		RConsole.openUSB(5000);

		/* �Z���T�[���̃f�o�b�O�o�� */
		map.setup();
		
		/* �}�b�v���̃����[�h */
//		if(!map.reload()) map.setPathBack(motion.isWallBack() ? Map.WALL : Map.FLAG);
//		map.setCurPosInfo(Map.UNKNOWN);
		if(!map.reload()) map.setPathBack(Map.WALL);
		LCD.clear();
		boolean right_hand = true;
		while (true) {
			if (Button.RIGHT.isDown()) {
				right_hand = true;
				while (Button.RIGHT.isDown());
				break;
			}
			else if (Button.LEFT.isDown()) {
				right_hand = false;
				while (Button.LEFT.isDown());
				break;
			}
		}
		/* ���H�T�� */
		map.dispMap();
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
				int comp;
				comp = right_hand ? map.getPathLeft() : map.getPathRight(); 
				if(comp >= path) {
					path = right_hand ? map.getPathLeft() : map.getPathRight(); 
					direc = right_hand ? Direc.LEFT : Direc.RIGHT;
				}
				if(map.getPathFront() >= path) {
					path = map.getPathFront();
					direc = Direc.FRONT;
				}
				comp = right_hand ? map.getPathRight() : map.getPathLeft();
				if(comp >= path) {
					path = right_hand ? map.getPathRight() : map.getPathLeft(); 
					direc = right_hand ? Direc.RIGHT : Direc.LEFT;
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
			if (map.getTile() == Map.FLAG) {
				if (map.isFirstRoom()) break;
				map.movePrevRoom();
			}
			
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
//				map.waitForButtonPress(0);
				map.move();
				map.dispMapInfo();
				if (map.getTile() == Map.FLAG) {
					if (map.isFirstRoom()) break;
					map.movePrevRoom();
				}
//				map.waitForButtonPress(0);
			}
		}
	}
}