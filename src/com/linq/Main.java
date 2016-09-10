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
		
		//���[�^�[�̐錾
		LQMotor2 leftMotor = new LQMotor2(MotorPort.A);
		LQMotor2 rightMotor = new LQMotor2(MotorPort.C);
		LQMover mover = new LQMover(MotorPort.A, MotorPort.B);
		
		//�{�[���[�g�ƃo�b�t�@�T�C�Y�̐ݒ�
		RS485.hsEnable(115200, 64);
		//�f�[�^�i�[�p�̔z��
		byte buffer[] = new byte[1];
		
		//�f�[�^��M���[�v
		while(true) {
			LCD.clear();
			//�z��Ƀf�[�^��1�i�[
			//RS485.hsRead(�i�[�z��, �I�t�Z�b�g, �f�[�^�̌�);
			RS485.hsRead(buffer, 0, 1);
			//��M�����f�[�^�̕`��(byte)
			LCD.drawInt(buffer[0], 0, 0);
			Delay.msDelay(20);
		}
	}

}
