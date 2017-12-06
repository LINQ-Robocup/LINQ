package com.linq;

import lejos.nxt.Sound;


public class Main {
	enum Direc{RIGHT, LEFT, FRONT, BACK;}
	public static void main(String[] args) {
		/* �C���X�^���X�����E������ */
		MotionToMap map = new MotionToMap();
		map.dispMapInfo();
		map.waitForButtonPress(0);
		
		/* ���H�T�� */
		map.setPathBack(Map.WALL);
		while(true) {
			if (!map.isTilePassed()) {
				// �Ǐ��̎擾(�V�K)
				map.updateRealWallInfo();
				if (map.getPathRight() == Map.UNKNOWN)
					map.setPathRight(map.real.isWallRight() ? Map.WALL : Map.FLAG);
				if (map.getPathFront() == Map.UNKNOWN)
					map.setPathFront(map.real.isWallFront() ? Map.WALL : Map.FLAG);
				if (map.getPathLeft() == Map.UNKNOWN) 
					map.setPathLeft(map.real.isWallLeft() ? Map.WALL : Map.FLAG);
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
					if (path == Map.FLAG)
						map.resetDistanceMap();
					break;
				}
			}
			
			//�����]��
			switch (direc) {
				case RIGHT: map.turnRight(); break;
				case LEFT : map.turnLeft(); break;
				case BACK : map.turn(); break;
				default: break;
			}

			//�^�C���ړ�
			if (!map.isFrontWall()) {
				map.setTilePass();
				map.move();
				if (map.getTile() == Map.FLAG)
					break;
				map.setPathBack(Map.PASS);
				map.dispMapInfo();
				map.waitForButtonPress(3);
			}
		}
	}
}