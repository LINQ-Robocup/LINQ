package com.linq;


import com.linq.LQMover;
import com.linq.LQMotor2;

import lejos.nxt.Button;
import lejos.nxt.ButtonListener;
import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.addon.LDCMotor;
import lejos.nxt.addon.LServo;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class Main
{
	private static final int TEMP1 = 1;
	private static final int TEMP2 = 2;
	private static final int IRDIST1 = 3;
	private static final int IRDIST2 = 4;
	private static final int IRDIST3 = 5;
	private static final int IRDIST4 = 6;
	private static final int SRDIST = 7;

	
	public void run() {
		//ボーレートとバッファサイズの設定
		RS485.hsEnable(9600, 0);
		//データ格納用の配列
		byte send[] = new byte[7];
		//データ受信用の配列
		byte get[] = new byte[7];
		send[0] = 0;
		
		while(true) {
			RS485.hsWrite(send, 0, 1);
			RS485.hsRead(get, 0, 7);

			LCD.clear();
			for(int i = 0; i < 7; i++) {
				LCD.drawInt(get[i], 0, i);
			}
			Delay.msDelay(10);
		}
	}
	
	public static void main(String args[]) {
		
		//モーターの宣言
//		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
//		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
//		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		Main obj = new Main();
		obj.run();
		
	}

}
