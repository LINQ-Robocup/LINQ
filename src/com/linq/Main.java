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

		Delay.msDelay(1000);

		int speed = 70;
		mover.tileForward(speed);
		
		mover.tileForward(speed);

		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();
		mover.rotate(mover.LEFT);
		
		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();
		mover.rotate(mover.LEFT);
		
		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();
		mover.rotate(mover.RIGHT);
		
		mover.tileForward(speed);
		mover.rotate(mover.RIGHT);
		
		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();
		mover.rotate(mover.LEFT);
		
		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();
		mover.rotate(mover.LEFT);
		
		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();
		mover.rotate(mover.RIGHT);
		
		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();
		mover.rotate(mover.RIGHT);
		
		mover.tileForward(speed);
		mover.setParallel();
		mover.setDistance();

		
		
		

	}
}	
