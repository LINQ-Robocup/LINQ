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
		
		/* にくまんのあそびば */
		motion.mbed.debugCamera();
//		while(!Button.ESCAPE.isDown()){
//			motion.mbed.readRaspi(motion.mbed.cameraLeft);
//			LCD.clear();
//			LCD.drawInt(motion.mbed.cameraLeftValue, 1, 1);
//			LCD.drawInt(motion.mbed.cameraRightValue, 1, 2);
//			LCD.drawInt(motion.mbed.distFrontLeftValue, 1, 5);
//			LCD.drawInt(motion.mbed.dummyValue, 1, 7);
//			Delay.msDelay(1000);
//			
//			motion.mbed.readRaspi(motion.mbed.cameraRight);
//			LCD.clear();
//			LCD.drawInt(motion.mbed.cameraLeftValue, 1, 1);
//			LCD.drawInt(motion.mbed.cameraRightValue, 1, 2);
//			LCD.drawInt(motion.mbed.distFrontLeftValue, 1, 5);
//			LCD.drawInt(motion.mbed.dummyValue, 1, 7);
//			Delay.msDelay(1000);
//		}
//		while(!Button.ESCAPE.isDown()){
//			byte get[] = new byte[1];
//			RS485.hsRead(get, 0, 1);
//			LCD.clear();
//			LCD.drawInt(get[0], 1, 1);
//			Delay.msDelay(10);
//		}



		/* マップ情報のリロード */
		if(map.reload()) map.setCurPosInfo(Map.UNKNOWN);
		
		/* 迷路探索 */
		map.setPathBack(motion.isWallBack() ? Map.WALL : Map.FLAG);
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
			map.dispMapInfo();
			
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
				if (map.getTile() == Map.FLAG) break;
				map.setPathBack(Map.PASS);
				map.dispMapInfo();
			}
		}
	}
}