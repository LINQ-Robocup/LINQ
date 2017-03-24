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
	
		LCD.clear();
		while(!Button.ENTER.isDown()) LCD.drawInt(motion.tileForward(false) , 0, 0);
		
		/* マップ情報のリロード */
		map.reload();
		
		/* 迷路探索 */
		map.setWallBack(motion.isWallBack() ? MapInfo.WALL : MapInfo.FLAG);
		while(true) {
			//壁情報の取得(新規)
			if(map.getCurTileInfo() == MapInfo.UNKNOWN) {
				while(Button.ENTER.isDown());
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
			if(map.getWallRight() == MapInfo.FLAG || // 必然的に右 
			   (map.getWallLeft() == MapInfo.WALL && map.getWallFront() == MapInfo.WALL)) {
				direction = 0;
			} else if(map.getWallFront() == MapInfo.FLAG || //必然的に前
					   (map.getWallLeft() == MapInfo.WALL && map.getWallRight() == MapInfo.WALL)) {
				direction = 1;
			} else if(map.getWallLeft() == MapInfo.FLAG || //必然的に左
					   (map.getWallFront() == MapInfo.WALL && map.getWallRight() == MapInfo.WALL)) {
				direction = 2;
			} else {
				byte maxVal = 0, curVal = 0;
				//(FLAG-距離)が大きい方向を選択
				while(true) {
					for(byte d = 0; d < 4; d++) {
						switch(d) {
							case 0:	curVal = map.getWallRight(); break;
							case 1:	curVal = map.getWallFront(); break;
							case 2:	curVal = map.getWallLeft(); break;
							case 3:	curVal = map.getWallBack(); break;
							default:
						}
						if(curVal > maxVal) {
							maxVal = curVal;
							direction = d;
						}
					}
					break;
//					if(maxVal <= MapInfo.PASS) {
//						map.searchFlag();
//					} else {
//						break;
//					}
				}
			}
			
//			LCD.clear();
//			LCD.drawString("("+map.curX+","+map.curY+")", 0, 0);
//			LCD.drawString("RIGHT: "+map.getWallRight(), 0, 1);
//			LCD.drawString("FRONT: "+map.getWallFront(), 0, 2);
//			LCD.drawString("LEFT : "+map.getWallLeft(),  0, 3);
//			LCD.drawString("BACK : "+map.getWallBack(),  0, 4);
//			while(!Button.ENTER.isDown());
//			while(!Button.ENTER.isDown());

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
						map.changeDirec(false);
					} else {
						motion.turnRight(map.isPassedThrough());
						map.changeDirec(true);
					}
					break;
				default:
			}
			map.dispMapInfo();
			
			//タイル移動
			if(map.getWallFront() != MapInfo.WALL) {
				byte result = (byte)(motion.tileForward(map.getWallFront()==MapInfo.FLAG ? true : false));
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
						if(map.getWallFront() != MapInfo.PASS)
							map.setWallFront(MapInfo.PASS);
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