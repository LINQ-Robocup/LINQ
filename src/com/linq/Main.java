package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.util.Delay;

import java.lang.Math;

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
		
		mover.rotate(mover.LEFT);
		Delay.msDelay(1000);
		mover.rotate(mover.LEFT);
		Delay.msDelay(1000);
		mover.rotate(mover.LEFT);
		Delay.msDelay(1000);
		mover.rotate(mover.LEFT);
		Delay.msDelay(1000);
		
		LCD.clear();
		mover.tileForward(70);
		Delay.msDelay(500);
		mover.tileForward(70);
		Delay.msDelay(500);
		mover.setParallel();
		mover.setDistance();
		
	}
}	
