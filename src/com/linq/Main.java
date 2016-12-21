package com.linq;
import lejos.nxt.*;

public class Main extends MapInfo {
	public static void main(String[] args) {
		/* make instances */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();
		
		MapInfo map = new MapInfo();
		final byte x_d[] = {1, 0, -1, 0};
		final byte y_d[] = {0, 1, 0, -1};
		
		/* マップ情報のリロード・リセット */
		while(Button.ENTER.isDown());
		while(true) {
			LCD.drawString("X:" + map.curPos.x + " Y:" + map.curPos.y + " D :" + map.curPos.direc, 0, 0);
			LCD.drawString("RELOAD -> RIGHT", 1, 0);
			LCD.drawString("RESET  -> LEFT",  2, 0);
			LCD.drawString("START  -> ENTER", 3, 0);
			if(Button.RIGHT.isDown()) 
				map.readFile();
			else if(Button.LEFT.isDown())  
				map.resetFile();
			else if(Button.ENTER.isDown()) 
				break;
		}
		while(Button.ENTER.isDown());
		while(!Button.ENTER.isDown()) {
			map.dispMapInfo();
		}
		
		while(true) {
			if(map.curPos.getWallRight() == map.UNKNOWN) {
				map.curPos.setWallRight(sensor.isWallRight() ? map.WALL : map.FLAG);
			}
			if(map.curPos.getWallFront() == map.UNKNOWN) {
				map.curPos.setWallFront(sensor.isWallFront() ? map.WALL : map.FLAG);
			}
			if(map.curPos.getWallLeft() == map.UNKNOWN) {
				map.curPos.setWallLeft(sensor.isWallLeft() ? map.WALL : map.FLAG);
			}
			if(map.curPos.getWallBack() == map.UNKNOWN) {
				if(map.curPos.room == 0) {
					if(sensor.isWallRight()) {
						motion.turnRight(true);
						map.curPos.setWallBack(sensor.isWallRight() ? map.WALL : map.FLAG);
						motion.turnLeft(true);
					} else {
						motion.turnLeft(true);
						map.curPos.setWallBack(sensor.isWallLeft() ? map.WALL : map.FLAG);
						motion.turnRight(true);
					}
				}
			}
			map.arrangeMap();
		
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
			byte result = (byte) motion.tileForward(80, map.curPos.getWallFront() == map.PASS ? true : false);
			map.map[map.curPos.room][map.curPos.y][map.curPos.x] = map.PASS;
			if(result == motion.BLACK) {
				map.curPos.setFrontBlack();
			} else if(result == motion.SILVER) {
				map.writeFile();
			}
			if(result == motion.RAMP) {
				map.changeNextRoom();
			} else {
				if(result != motion.BLACK) {
					map.curPos.setFrontPass();
					map.curPos.changePos();
				}
				if(map.curPos.x == map.doorway[map.curPos.room].ent_x && map.curPos.y == map.doorway[map.curPos.room].ent_y) {
					if(map.curPos.room == 0) {
						break;
					} else {
						if(!sensor.isWallRight()) {
							motion.turnRight(true);
						} else if(!sensor.isWallLeft()) {
							motion.turnLeft(true);
						}
						while(!Button.ENTER.isDown());
						map.changePrevRoom();
					}
				}
			}
		}
	}
}
