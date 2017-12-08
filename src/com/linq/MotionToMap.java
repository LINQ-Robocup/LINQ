package com.linq;

import lejos.nxt.Button;
import lejos.nxt.MotorPort;
import lejos.nxt.NXT;
import lejos.nxt.Sound;

public class MotionToMap extends Map {
	enum Tile{NORMAL, CHECK_POINT, BLACK, WALL, UP_RAMP, DOWN_RAMP;}
	LQMover nxt = new LQMover(MotorPort.A, MotorPort.B);
	
	class RealWall {
		boolean isWallRight() {
			return nxt.isWallRight();
		}
		boolean isWallFront() {
			return nxt.isWallFront();
		}
		boolean isWallLeft() {
			return nxt.isWallLeft();
		}
	}
	RealWall real = new RealWall();
	
	void turnRight() {
		nxt.turnRight(isTilePassed(), 80);
		changeDirec(true);
	}
	
	void turnLeft() {
		nxt.turnLeft(isTilePassed(), 60);
		changeDirec(false);
	}
	
	void turn() {
		nxt.turnLeft(isTilePassed(), 60);
		changeDirec(true);
		nxt.turnLeft(isTilePassed(), 60);
		changeDirec(true);
	}
	
	void move() {
		setTilePass();
		byte result = (byte) nxt.tileForward((getPathFront()==Map.PASS), false);
		switch(result) {
//		case LQMover.DOWN_RAMP: break;
//		case LQMover.UP_RAMP: break;
		case LQMover.BLACK:	setFrontBlack(); break;
		case LQMover.WALL:		setPathFront(WALL); break;
		case LQMover.SILVER:	writeFile(); break;
		default: moveTile(); 
		}
	}
	
	void updateRealWallInfo() {
		for(int i = 0; i < 10; i++) 
		nxt.requestToMbedSensors();
	}
	
	void waitForButtonPress(int i) {
		Sound.beep();
		while(!Button.ENTER.isDown());
		while(Button.ENTER.isDown());
	}
	
	MotionToMap() {
		super();
	}
}