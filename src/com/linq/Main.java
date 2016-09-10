package com.linq;


import com.linq.LQMover;
import com.linq.LQMotor2;

import lejos.nxt.LCD;
import lejos.nxt.MotorPort;
import lejos.nxt.addon.LDCMotor;
import lejos.nxt.addon.LServo;
import lejos.nxt.comm.RS485;
import lejos.util.Delay;
import lejos.util.Stopwatch;

public class Main {
	
	public static void main(String args[]) {
		
		//モーターの宣言
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		
		//ボーレートとバッファサイズの設定
		RS485.hsEnable(115200, 64);
		//データ格納用の配列
		byte buffer[] = new byte[1];
		
		//データ受信ループ
		while(true) {
			LCD.clear();
			//配列にデータを1つ格納
			//RS485.hsRead(格納配列, オフセット, データの個数);
			RS485.hsRead(buffer, 0, 1);
			//受信したデータの描画(byte)
			LCD.drawInt(buffer[0], 0, 0);
			Delay.msDelay(20);
		}
	}

}
