package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.util.Delay;
import lejos.util.Stopwatch;

import java.lang.Math;
import java.util.Timer;

public class Main
{

	public static void main(String args[]) {
		
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.B);
		LQMotor2 servo = new LQMotor2(MotorPort.C);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();
		servo.setPower(100);
		servo.forward();
		
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setPower(35);
		rightMotor.setPower(35);

	
		Delay.msDelay(500);
		sensor.showAllSensors();

		Delay.msDelay(500);
		while(!Button.ENTER.isDown()) {
			LCD.clear();
			LCD.drawInt(leftMotor.getTachoCount(), 0, 0);
			LCD.drawInt(rightMotor.getTachoCount(), 0, 1);
			
			Delay.msDelay(20);
			
		}
		sensor.resetGyroValue();
		for(int i = 0; i < 100; i++) {
			int speed = 85;
//			sensor.resetGyroValue();
			double offset = sensor.getGyroValue();
			
			leftMotor.setPower(speed);
			rightMotor.setPower(speed);
			leftMotor.forward();
			rightMotor.backward();
			while (Math.abs(sensor.getGyroValue() - offset) < 8700) {
				if(Math.abs(sensor.getGyroValue() - offset) > 7000) {
					leftMotor.setPower(speed/2);
					rightMotor.setPower(speed/2);
				}
			}
			leftMotor.stop();
			rightMotor.stop();
			Delay.msDelay(500);
		}
//		while (true) {
//			LCD.drawInt((int)Math.abs(sensor.getGyroValue() - offset), 0, 5);
//		}
	}
}	
