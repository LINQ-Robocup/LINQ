package com.linq;

import lejos.nxt.*;
//import lejos.nxt.comm.RConsole;

public class Main {
	enum Direc{RIGHT, LEFT, FRONT, BACK;}
	
	public static void main(String[] args) {
		/* インスタンス生成・初期化 */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		MotionToMap map = new MotionToMap();
//		RConsole.openUSB(5000);

		/* センサー情報のデバッグ出力 */
		motion.sensorSetup();
//		motion.mbed.debugLeds();
//		motion.mbed.debugServo();
//		motion.mbed.debugCamera();
		
		/* マップ情報のリロード */
		if(map.reload()) map.setCurPosInfo(Map.UNKNOWN);
		
		/* 迷路探索 */
		map.setPathBack(motion.isWallBack() ? Map.WALL : Map.FLAG);
		map.dispMap();
		while(true) {
			//壁情報の取得(新規)
			if (!map.isTilePassed()) {
				// 壁情報の取得(新規)
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
				// マップの整形
				map.arrangeMap();
				map.dispMapInfo();
//				map.waitForButtonPress(0);
			}
			
			// 進行方向の決定
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
				// 全て通過済みの場合(最短のFLAGまでの距離を配列に代入)
				if (path <= Map.PASS) {
					Sound.buzz();
					map.searchFlag();
					map.dispMapInfo();
				} else {
					if (path == Map.FLAG) map.resetDistanceMap();
					break;
				}
			}
			//方向転換
			switch (direc) {
				case RIGHT: map.turnRight(); break;
				case LEFT : map.turnLeft(); break;
				case BACK : map.turn(); break;
				default: break;
			}
			map.dispMapInfo();
			
			//タイル移動
			if (!map.isFrontWall()) {
				map.move();
				map.dispMapInfo();
				if (map.getTile() == Map.FLAG) {
					if (map.isFirstRoom()) break;
					map.movePrevRoom();
				}
			}
		}
	}
}