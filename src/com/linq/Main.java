package com.linq;

import lejos.nxt.*;
//import lejos.nxt.comm.RConsole;

public class Main {

	public static void main(String[] args) {
		/* インスタンス生成・初期化 */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		MapInfo map = new MapInfo(1, 1);																																																																	
//		RConsole.openUSB(5000);

		/* センサー情報のデバッグ出力 */
		motion.sensorSetup();
		
		/* マップ情報のリロード */
		if(map.reload()) map.setCurPosInfo(MapInfo.UNKNOWN);
		
		/* 迷路探索 */
		map.setWallBack(motion.isWallBack() ? MapInfo.WALL : MapInfo.FLAG);
		while(true) {
			//壁情報の取得(新規)
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
				//マップの整形
				map.arrangeMap();
				map.dispMapInfo();
			}

			byte direction = 0;
			//進行方向の決定
			byte minVal = map.getWallRight();
			byte curVal = 0;
			//通過回数が少ない方向を選択
			curVal = map.getWallRight();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)0;
			}
			curVal = map.getWallFront();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)1;
			}
			curVal = map.getWallLeft();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)2;
			}
			curVal = map.getWallBack();
			if(curVal > 0 && curVal < minVal) {
				minVal = curVal;
				direction = (byte)3;
			}
			
			//方向転換
			switch(direction) {
				case 0: //右
					motion.turnRight(map.isPassedThrough());
					map.changeDirec(true);
					break;
				case 2: //左
					motion.turnLeft(map.isPassedThrough());
					map.changeDirec(false);
					break;
				case 3: //後
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
			
			//タイル移動
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
							if(map.curRoom == 0) {
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