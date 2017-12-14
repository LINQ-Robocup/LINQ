package com.linq;

import java.util.Queue;

import lejos.nxt.Button;
import lejos.nxt.LCD;
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
		nxt.turnRight(isTilePassed());
		changeDirec(true);
	}
	
	void turnLeft() {
		nxt.turnLeft(isTilePassed());
		changeDirec(false);
	}
	
	void turn() {
		boolean right = (nxt.compSideDist() < 0) ? true : false;
		if (right) nxt.turnRight(isTilePassed());
		else nxt.turnLeft(isTilePassed());
		changeDirec(right);
		if (right) nxt.turnRight(isTilePassed());
		else nxt.turnLeft(isTilePassed());
		changeDirec(right);
	}
	
	void move() {
		setTilePass();
		byte tile = nxt.tileForward(getPathFront() == PASS);
		switch (tile) {
			case LQMover.BLACK: setFrontBlack(); break;
			case LQMover.WALL:  setPathFront(WALL); break;
			case LQMover.UP_RAMP: moveNextRoom(true); break;
			case LQMover.DOWN_RAMP: moveNextRoom(false); break;
			default: moveTile(); setPathBack(PASS);
		}
		if (tile == LQMover.SILVER) writeFile();
//		LCD.clear();
//		LCD.drawInt(tile, 0, 1);
//		waitForButtonPress(0);
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
//		nxt.mbed.resetBuffer();
//		for(int i = 0; i < 10; i++) 
		nxt.requestToMbedSensors();
	}
	
	void waitForButtonPress(int i) {
		Sound.beep();
		while(!Button.ENTER.isDown());
		while(Button.ENTER.isDown());
	}
	
	void setup() {
		nxt.sensorSetup();
//		nxt.mbed.debugLeds();
//		nxt.mbed.debugServo();
//		nxt.mbed.debugCamera();
//		nxt.turnRight(false);
	}
	
	MotionToMap() {
		super();
	}
}