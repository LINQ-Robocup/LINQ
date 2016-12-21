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
		
		/* make instances */
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.B);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		LQSensor sensor = new LQSensor();
		
		/* motor's init */
		leftMotor.stop();
		rightMotor.stop();
		leftMotor.setPower(70);
		rightMotor.setPower(70);

		/* debug sensors */
		Delay.msDelay(1000);
		while (!Button.ENTER.isDown()) {
			sensor.showAllSensors();
		}
		
		/* blinking LEDs */
		{
			Delay.msDelay(1000);
			LCD.drawString("BLINKING LED", 2, 5);
			while (!Button.ENTER.isDown()) {
				sensor.ledGreen(true);
				sensor.ledYellow(false);
				sensor.ledRed(true);
				Delay.msDelay(300);
				sensor.ledGreen(false);
				sensor.ledYellow(true);
				sensor.ledRed(false);
				Delay.msDelay(300);
			}
			sensor.ledGreen(false);
			sensor.ledYellow(false);
			sensor.ledRed(false);
			
		}
		
		
		/* main */
		int ans = mover.tileForward(70, false);
		
		if(ans == mover.BLACK) {
			LCD.drawString("BLACK", 5, 5);
			Delay.msDelay(2000);
		}else if(ans == mover.RAMP) {
			LCD.drawString("RAMP", 5, 5);
			Delay.msDelay(2000);
		}else if(ans == mover.SILVER) {
			LCD.drawString("SILVER", 5, 5);
			Delay.msDelay(2000);
		}else if(ans == mover.WHITE) {
			LCD.drawString("WHITE", 5, 5);
			Delay.msDelay(2000);
		}
	}
}	
