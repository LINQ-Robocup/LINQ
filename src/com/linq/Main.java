package com.linq;
import lejos.nxt.*;
import lejos.util.Delay;

public class Main extends MapInfo {
	public static void main(String[] args) {
		/* make instances */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();
		
		MapInfo map = new MapInfo();
		//map.doorway[0].reset();																																																																	
		final byte x_d[] = {1, 0, -1, 0};
		final byte y_d[] = {0, 1, 0, -1};
		
//		for(byte i = 0; i < map.ROOM; i++) {
//			for(byte j = 0; j < map.HEIGHT; j++) {
//				for(byte k = 0; k < map.WIDTH; k++) {
//					map.map[i][j][k] = 0;
//					if(j % 2 == 0 && k % 2 == 0) {
//						//頂点はWALLとして初期化
//						map.map[i][j][k] = map.WALL;
//					} else {
//						map.map[i][j][k] = map.UNKNOWN;
//					}
//				}
//			}
//		}
		
		/* センサー情報のデバッグ出力 */
		while (Button.ENTER.isDown());
		while (!Button.ENTER.isDown()) {
			sensor.showAllSensors();
		}
		
		/* マップ情報のリロード・リセット */
		while(Button.ENTER.isDown());
		while(true) {
			LCD.drawString("X:" + map.curPos.x + " Y:" + map.curPos.y + " D :" + map.curPos.direc, 0, 0);
			LCD.drawString("RELOAD -> RIGHT", 0, 1);
			LCD.drawString("RESET  -> LEFT",  0, 2);
			LCD.drawString("START  -> ENTER", 0, 3);
			if(Button.RIGHT.isDown()) 
				map.readFile();
			else if(Button.LEFT.isDown())  
				map.resetFile();
			else if(Button.ENTER.isDown()) 
				break;
		}
		LCD.clear();
		while(Button.ENTER.isDown());
		map.dispMapInfo();
		while(!Button.ENTER.isDown());
		
		while(true) {
			if(map.curPos.getWallRight() == map.UNKNOWN) {
				LCD.clear();
				LCD.drawString("RIGHT", 0, 0);
				map.curPos.setWallRight(sensor.isWallRight() ? map.WALL : map.FLAG);
				LCD.drawString("R X:" + map.curPos.x + " Y:" + map.curPos.y + " D :" + map.curPos.direc, 0, 0);
				LCD.drawInt(map.map[map.curPos.room][1][2], 0, 1);
				LCD.drawInt(map.map[map.curPos.room][1][0], 0, 2);
				Delay.msDelay(1000); 
				map.arrangeMap();
				map.dispMapInfo();
				LCD.clear();
				LCD.drawString("R X:" + map.curPos.x + " Y:" + map.curPos.y + " D :" + map.curPos.direc, 0, 0);
				LCD.drawInt(map.map[map.curPos.room][1][2], 0, 1);
				LCD.drawInt(map.map[map.curPos.room][1][0], 0, 2);
				Delay.msDelay(1000);
			}
			if(map.curPos.getWallFront() == map.UNKNOWN) {
				LCD.clear();
				LCD.drawString("FORNT", 0, 0);
				map.curPos.setWallFront(sensor.isWallFront() ? map.WALL : map.FLAG);
				map.arrangeMap();
				map.dispMapInfo();
				LCD.clear();
				LCD.drawString("F X:" + map.curPos.x + " Y:" + map.curPos.y + " D :" + map.curPos.direc, 0, 0);
				Delay.msDelay(1000);
			}
			if(map.curPos.getWallLeft() == map.UNKNOWN) {
				LCD.clear();
				LCD.drawString("LEFT", 0, 0);
				map.curPos.setWallLeft(sensor.isWallLeft() ? map.WALL : map.FLAG);
				map.arrangeMap();
				map.dispMapInfo();
				LCD.drawString("L X:" + map.curPos.x + " Y:" + map.curPos.y + " D :" + map.curPos.direc, 0, 0);
				Delay.msDelay(1000);
			}
			if(map.curPos.getWallBack() == map.UNKNOWN) {
				LCD.drawString("BACK", 0, 0);
				Delay.msDelay(1000);
				if(map.curPos.room == 0) {
					if(sensor.isWallRight()) {
						motion.turnRight(true);
						map.curPos.setWallBack(sensor.isWallRight() ? map.WALL : map.FLAG);
						map.arrangeMap();
						map.dispMap();
						motion.turnLeft(true);
					} else {
						motion.turnLeft(true);
						map.curPos.setWallBack(sensor.isWallLeft() ? map.WALL : map.FLAG);
						map.arrangeMap();
						map.dispMap();
						motion.turnRight(true);
					}
				}
			}
			map.dispMapInfo();
			byte direction = 0;
			while(true) {
				byte maxValue = 0;
				for(byte d = 0; d < 4; d++) {
					if(map.map[map.curPos.room][map.curPos.y+y_d[d]][map.curPos.x+x_d[d]] > maxValue) {
						maxValue = map.map[map.curPos.room][map.curPos.y+y_d[d]][map.curPos.x+x_d[d]];
						direction = d;
					}
				}
				if(maxValue <= map.PASS) {
					map.searchFlag();
				} else {
					break;
				}
			}
			switch(direction) {
				case 0:
					motion.turnRight(map.curPos.isPassedThrough());
					map.curPos.changeDirec(map.RIGHT);
					break;
				case 2:
					motion.turnLeft(map.curPos.isPassedThrough());
					map.curPos.changeDirec(map.LEFT);
					break;
				case 3:
					motion.turnLeft(true);
					map.curPos.changeDirec(map.LEFT);
					motion.turnLeft(true);
					map.curPos.changeDirec(map.LEFT);
					break;
				default:
			}
			if(map.curPos.getCurPos() == map.FLAG-1) {
				map.resetDistanceMap();
			}
			map.dispMapInfo();
			byte result = (byte) motion.tileForward(80, map.curPos.getWallFront() == map.PASS ? true : false);
			map.map[map.curPos.room][map.curPos.y][map.curPos.x] = map.PASS;
			if(result == motion.BLACK) {
				map.curPos.setFrontBlack();
			} else {
				map.curPos.setFrontPass();
				map.curPos.changePos();
			}
//			if(result == motion.SILVER) {
//				map.writeFile();
//			}
//			if(result == motion.RAMP) {
//				map.changeNextRoom();
//			} else {
//				if(map.curPos.x == map.doorway[map.curPos.room].ent_x && map.curPos.y == map.doorway[map.curPos.room].ent_y) {
//					if(map.curPos.room == 0) {
//						break;
//					} else {
//						if(!sensor.isWallRight()) {
//							motion.turnRight(true);
//						} else if(!sensor.isWallLeft()) {
//							motion.turnLeft(true);
//						}
//						while(!Button.ENTER.isDown());
//						map.changePrevRoom();
//					}
//				}
//			}
			map.dispMapInfo();
		}
	}
}
