package com.linq;

import javax.xml.stream.events.Namespace;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.util.Delay;
import lejos.util.Stopwatch;
import lejos.util.Timer;

public class Main {
	static NXTRegulatedMotor tachoLeft = new NXTRegulatedMotor(MotorPort.A);
	static NXTRegulatedMotor tachoRight = new NXTRegulatedMotor(MotorPort.B);
	static NXTMotor motorLeft = new NXTMotor(MotorPort.A);
	static NXTMotor motorRight = new NXTMotor(MotorPort.B);
	
	final static int tileTacho = 690;
	
	public static void main(String[] args) {
		
		for(int i = 0; i > 2; i++) {
			forwardTile(60);
			motorLeft.stop();
			motorRight.stop();
			Delay.msDelay(1000);
			
		}
	}
	public static void forwardTile(int speed){
		motorLeft.setPower(speed);
		motorRight.setPower(speed);
		
		long thresholdLeft = tachoLeft.getTachoCount();
		long thresholdRight = tachoRight.getTachoCount();
		motorLeft.forward();
		motorRight.forward();
		
		while(true) {
			LCD.drawInt(tachoLeft.getTachoCount()-(int)thresholdLeft, 0, 0);
			LCD.drawInt(tachoRight.getTachoCount()-(int)thresholdRight, 0, 1);
			if (tachoLeft.getTachoCount()-thresholdLeft >= tileTacho) {
				if (tachoRight.getTachoCount()-thresholdRight >= tileTacho) {
					motorLeft.stop();
					motorRight.stop();
					break;
				}else {
					motorLeft.stop();
				}
			}else {
				if (tachoRight.getTachoCount()-thresholdRight >= tileTacho) {
					motorRight.stop();
				}
			}
		}
	}
}
