package com.linq;

import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.TachoMotorPort;

public class LQMotor extends NXTRegulatedMotor {
	static NXTMotor motor;
	public LQMotor(TachoMotorPort port) {
		super(port);
		motor = new NXTMotor(port);

	}
	
	/*
	 * NXTRegulatedMotor can't setSpeed(1~100).
	 * So setSpeed is redirected to NXTMotor setPower. 
	 */
	
    public void setSpeed(int speed)
    {
		motor.setPower(speed);
    }

	
    public void setSpeed(float speed)
    {
		motor.setPower((int)speed);
    } 
}
