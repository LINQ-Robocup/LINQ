package com.linq;

import lejos.nxt.*;
import lejos.util.Delay;

public class Main extends MapInfo {
	public static void main(String[] args) {
		/* make instances */
		LQMover motion = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();
		
		MapInfo map = new MapInfo();																																																																	
//		final byte x_d[] = {1, 0, -1, 0};
//		final byte y_d[] = {0, 1, 0, -1};
		
		//nikuman
		LQMbedSensors test = new LQMbedSensors();
		
		test.toggleLedBlue(true);
		test.toggleLedGreen(true);
		test.toggleLedYellow(true);
		
		test.rotateServo();
		test.debugLeds();
		
		while (Button.ENTER.isDown());
		while (!Button.ENTER.isDown()) {
			for(int i = 0;;i++) {
				test.readMbedSensorsValue();
				test.showSensorValues();
////				LCD.drawString("ERROR", 10, 5); LCD.drawInt(i, 10, 6);
//				if(test.dummyValue != 111) {
//					Sound.beep();
//					LCD.clear();
//					LCD.drawString("ERROR", 10, 5); LCD.drawInt(i, 10, 6);
//					while (Button.ENTER.isDown());
//					while (!Button.ENTER.isDown());
//				}
			}
		}
		
		
		
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
//			LCD.clear();
//			LCD.drawString("RIHGT: " + map.curPos.getWallRight() , 0, 1);
//			LCD.drawString("FRONT: " + map.curPos.getWallFront() , 0, 2);
//			LCD.drawString("LEFT : " + map.curPos.getWallLeft() ,  0, 3);
//			LCD.drawString("BACK : " + map.curPos.getWallBack() , 0, 4);
//			Delay.msDelay(1000);
			if(map.curPos.getWallRight() == map.UNKNOWN) {
				map.curPos.setWallRight(sensor.isWallRight() ? map.WALL : map.FLAG);
				map.arrangeMap();
			}
			if(map.curPos.getWallFront() == map.UNKNOWN) {
				map.curPos.setWallFront(sensor.isWallFront() ? map.WALL : map.FLAG);
				map.arrangeMap();
			}
			if(map.curPos.getWallLeft() == map.UNKNOWN) {
				map.curPos.setWallLeft(sensor.isWallLeft() ? map.WALL : map.FLAG);
				map.arrangeMap();
			}
			if(map.curPos.getWallBack() == map.UNKNOWN) {
				if(map.curPos.room == 0) {
					map.curPos.setWallBack(map.WALL);
//					if(sensor.isWallRight()) {
//						motion.turnRight(true, false);
//						map.curPos.setWallBack(sensor.isWallRight() ? map.WALL : map.FLAG);
//						//map.arrangeMap();
//						motion.turnLeft(true, false);
//					} else {
//						motion.turnLeft(true, false);
//						map.curPos.setWallBack(sensor.isWallLeft() ? map.WALL : map.FLAG);
//						//map.arrangeMap();
//						motion.turnRight(true, false);
//					}
				}
			}
			map.arrangeMap();
			map.dispMapInfo();
			byte direction = 1;
			byte maxValue = 0;
			byte curValue = 0;
			while(true) {
				for(byte d = 0; d < 4; d++) {
					switch(d) {
						case 0:
							curValue = map.curPos.getWallRight();
							break;
						case 1:
							curValue = map.curPos.getWallFront();
							break;
						case 2:
							curValue = map.curPos.getWallLeft();
							break;
						case 3:
							curValue = map.curPos.getWallBack();
							break;
							
					}
					if(curValue > maxValue) {
						maxValue = curValue;
						direction = d;
					}
				}
				if(maxValue <= map.PASS) {
					map.searchFlag();
				} else {
					break;
				}
			}
			if(maxValue >= map.FLAG - 1) {
				sensor.ledYellow(true);
				map.resetDistanceMap();
				Delay.msDelay(100);
				sensor.ledYellow(false);
			}
//			LCD.clear();
//			LCD.drawString("RIHGT: " + map.curPos.getWallRight() , 0, 1);
//			LCD.drawString("FRONT: " + map.curPos.getWallFront() , 0, 2);
//			LCD.drawString("LEFT : " + map.curPos.getWallLeft() ,  0, 3);
//			LCD.drawString("BACK : " + map.curPos.getWallBack() , 0, 4);
//			Delay.msDelay(1000);
			switch(direction) {
				case 0:
					motion.turnRight(map.curPos.isPassedThrough(), map.curPos.getWallLeft() == map.WALL ? true : false);
					map.curPos.changeDirec(map.RIGHT);
					break;
				case 2:
					motion.turnLeft(map.curPos.isPassedThrough(), map.curPos.getWallRight() == map.WALL ? true : false);
					map.curPos.changeDirec(map.LEFT);
					break;
				case 3:
					motion.turnLeft(true, map.curPos.getWallRight() == map.WALL ? true : false);
					map.curPos.changeDirec(map.LEFT);
					motion.turnLeft(true, map.curPos.getWallRight() == map.WALL ? true : false);
					map.curPos.changeDirec(map.LEFT);
					break;
				default:
			}
			
			map.dispMapInfo();
			if(sensor.isWallFront()) {
				map.curPos.setWallFront(map.WALL);
				Sound.beepSequenceUp();
				Delay.msDelay(100);
			} else {
				byte result = (byte) motion.tileForward(80, map.curPos.getWallFront() == map.PASS ? true : false);
				if(map.curPos.getCurPos() == map.UNKNOWN) {
					map.map[map.curPos.room][map.curPos.y][map.curPos.x] = map.PASS;
				}
				if(result == motion.BLACK) {
					map.curPos.setFrontBlack();
				} else {
					map.curPos.setFrontPass();
					map.curPos.changePos();
				}
				if(result == motion.SILVER) {
					map.writeFile();
					sensor.ledYellow(true);
					Delay.msDelay(500);
					sensor.ledYellow(false);
				}
				if(result == motion.RAMP) {
					map.changeNextRoom();
				} else {
					if(map.curPos.x == map.doorway_ent_x[map.curPos.room] && map.curPos.y == map.doorway_ent_y[map.curPos.room]) {
						if(map.curPos.room == 0) {
							break;
						} else {
							if(!sensor.isWallRight()) {
								motion.turnRight(true, map.curPos.getWallLeft() == map.WALL ? true : false);
							} else if(!sensor.isWallLeft()) {
								motion.turnLeft(true, map.curPos.getWallRight() == map.WALL ? true : false);
							}
							motion.downRamp();
							map.changePrevRoom();
						}
					}
				}
			}
			map.dispMapInfo();
		}
	}
}