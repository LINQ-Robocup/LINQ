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
	
	private static byte convRSSignal(int data) {
	    if(data >= 64) {
	        return (byte)((-2 * data) -1);
	    }else{
	        return (byte)((-2 * data) + 255);
	    }
	}
	
	public void run() {
		int sendData = 1;
		//ボーレートとバッファサイズの設定
		RS485.hsEnable(9600, 0);
		//データ格納用の配列
		byte send[] = new byte[7];
		//データ受信用の配列
		byte get[] = new byte[7];
		byte get2[] = new byte[1];
		send[0] = 0;
		
		boolean flag = false;
		while(true) {
			
			RS485.hsWrite(send, 0, 1);
			RS485.hsRead(get, 0, 7);
//			RS485.hsRead(get, 1, 1);

			LCD.clear();
			for(int i = 0; i < 7; i++) {
				LCD.drawInt(get[i], 0, i);
			}
			Delay.msDelay(5);
//			Delay.msDelay(50);
			
			if(flag == true) break;
		}
		
		while(true) {
			LCD.clear();
			//===============================
			sendData = TEMP1;
			send[0] = convRSSignal(sendData);
			RS485.hsWrite(send, 0, 1);

			get[0] = -1;
			while(get[0] == -1) {
				RS485.hsRead(get, 0, 1);
			}

			LCD.drawInt(sendData, 0, 0);
			LCD.drawInt(get[0], 4, 0);

			Delay.msDelay(50);
			//===============================
			sendData = TEMP2;
			send[0] = convRSSignal(sendData);
			RS485.hsWrite(send, 0, 1);

			get[0] = -1;
			while(get[0] == -1) {
				RS485.hsRead(get, 0, 1);
			}
			LCD.drawInt(sendData, 0, 1);
			LCD.drawInt(get[0], 4, 1);
			Delay.msDelay(50);
			//===============================
			sendData = IRDIST1;
			send[0] = convRSSignal(sendData);
			RS485.hsWrite(send, 0, 1);

			get[0] = -1;
			while(get[0] == -1) {
				RS485.hsRead(get, 0, 1);
			}
			LCD.drawInt(sendData, 0, 2);
			LCD.drawInt(get[0], 4, 2);
			Delay.msDelay(50);
			//===============================
			sendData = IRDIST2;
			send[0] = convRSSignal(sendData);
			RS485.hsWrite(send, 0, 1);

			get[0] = -1;
			while(get[0] == -1) {
				RS485.hsRead(get, 0, 1);
			}
			LCD.drawInt(sendData, 0, 3);
			LCD.drawInt(get[0], 4, 3);
			Delay.msDelay(50);
			//===============================
			sendData = IRDIST3;
			send[0] = convRSSignal(sendData);
			RS485.hsWrite(send, 0, 1);

			get[0] = -1;
			while(get[0] == -1) {
				RS485.hsRead(get, 0, 1);
			}
			LCD.drawInt(sendData, 0, 4);
			LCD.drawInt(get[0], 4, 4);
			Delay.msDelay(50);
			//===============================
			sendData = IRDIST4;
			send[0] = convRSSignal(sendData);
			RS485.hsWrite(send, 0, 1);

			get[0] = -1;
			while(get[0] == -1) {
				RS485.hsRead(get, 0, 1);
			}
			LCD.drawInt(sendData, 0, 5);
			LCD.drawInt(get[0], 4, 5);
			Delay.msDelay(50);
			//===============================
			Delay.msDelay(500);
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
