package com.linq;

import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.Sound;
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
		leftMotor.setPower(70);
		rightMotor.setPower(70);

		Delay.msDelay(1000);
		while (!Button.ENTER.isDown()) {
			sensor.showAllSensors();
		}
		sensor.blinkLED();
		
		/*
		
		int wallAvoidAngle = 200;
		
		
		*/
		mover.tileForward(70, false);
		Sound.beepSequence();
		
		

	}
}	
