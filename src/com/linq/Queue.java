package com.linq;

import lejos.nxt.LCD;

/**
 * Queue�\������N���X(�T�C�Y�͌Œ�) 
 * 
 * @author ASAHI
 */
public class Queue{
  /* �萔�錾 */
	//Queue�ő�T�C�Y
	private final short SIZE = 256;
  /* �ϐ��錾 */
	//Queue�o�b�t�@
	private short[] queue = new short[SIZE];
	//Queue�T�C�Y
	public short queSize = 0;
	
	/**
	 * Queue��������
	 */
	Queue() {
		try {
			for(short i = 0; i < SIZE; i++) {
				queue[i] = 0;
			}
			queSize = 0;
		} catch(ArrayIndexOutOfBoundsException e) {
			LCD.clear(0);
			LCD.drawString("error", 0, 0);
			while(true);
		}
	}
	
	/**
	 * �G���L���[(Enqueue)
	 * @param data �G���L���[����l
	 * @return�@�G���L���[�ɐ����������ǂ���?
	 */
	public boolean enqueue(short data) {
		try {
			if(queSize >= SIZE) {
				return false;
			}
			queue[queSize] = data;
			queSize ++;
			return true;
		} catch(ArrayIndexOutOfBoundsException e) {
			LCD.clear();
			LCD.drawString("enqueue", 0, 0);
			while(true);
		}
	}
	
	/**
	 * �f�L���[(Dequeue)
	 * @return�@�f�L���[�����l
	 */
	public short dequeue() {
		try {
			short data = queue[0];
			if(!isEmpty()) {
				for(short i = 0; i < queue.length-1; i++) {
					queue[i] = queue[i+1];
				}
			}
			queSize --;
			return data;
		} catch(ArrayIndexOutOfBoundsException e) {
			LCD.clear();
			LCD.drawString("enqueue", 0, 0);
			while(true);
		}
	}
	
	/**
	 * Queue���󂩂ǂ���?
	 * @return ��ł����true
	 */
	public boolean isEmpty() {
		return (!(queSize > 0));
	}
}