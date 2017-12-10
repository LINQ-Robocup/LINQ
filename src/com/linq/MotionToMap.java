package com.linq;

import java.util.Queue;

import lejos.nxt.Button;
import lejos.nxt.MotorPort;
import lejos.nxt.Sound;

public class MotionToMap extends Map {
	enum Tile{NORMAL, CHECK_POINT, BLACK, WALL, UP_RAMP, DOWN_RAMP;}
	Queue<Boolean> ramp = new Queue<Boolean>();
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
//		byte result = (byte) nxt.tileForward((getPathFront()==Map.PASS), false);
		nxt.tileForward(false, false);
//		switch (result) {
//			case LQMover.UP_RAMP:
//			case LQMover.DOWN_RAMP:
//				setExt();
//				moveNextRoom(result == LQMover.UP_RAMP);
//				break;
//			case LQMover.BLACK: 
//				setFrontBlack(); 
//				break;
//			case LQMover.WALL:	
//				setPathFront(WALL); 
//				break;
//			default:
				moveTile();
//				break;
//		}
//		if (result == LQMover.SILVER) writeFile();
	}
	
	boolean isFirstRoom() {
		return (this.room == 0);
	}
	
	void faceInDirection(byte direc) {
		byte d = (byte) (direc - this.direc);
		if (d < 0) d += 4;
		switch (d) {
			case 1: turnRight(); break;
			case 2: turn(); break;
			case 3: turnLeft();	break;
			default:
		}
	}
	
	void moveNextRoom(boolean up) {
		super.moveNextRoom();
		ramp.push(up);
	}
	
	void movePrevRoom() {
		super.movePrevRoom();
		faceInDirection((byte)((INIT_DIREC + 2) % 4));
		nxt.ramp(!(Boolean)ramp.pop());
	}
	
	void updateRealWallInfo() {
		nxt.mbed.resetBuffer();
		for(int i = 0; i < 10; i++) nxt.requestToMbedSensors();
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