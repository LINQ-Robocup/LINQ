package com.linq;

import lejos.nxt.Button;
import lejos.nxt.MotorPort;
import lejos.nxt.Sound;

public class MotionToMap extends Map {
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
//		nxt.turnRight(isTilePassed(), 80);
		changeDirec(true);
	}
	
	void turnLeft() {
//		nxt.turnLeft(isTilePassed(), 60);
		changeDirec(false);
	}
	
	void turn() {
//		nxt.turnLeft(isTilePassed(), 60);
		changeDirec(true);
//		nxt.turnLeft(isTilePassed(), 60);
		changeDirec(true);
	}
	
	void move() {
//		nxt.tileMovement(getPathFront() != FLAG, 60);
		moveTile();
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