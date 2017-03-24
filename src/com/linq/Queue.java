package com.linq;

import lejos.nxt.LCD;

/**
 * Queue表現するクラス(サイズは固定) 
 * 
 * @author ASAHI
 */
public class Queue{
  /* 定数宣言 */
	//Queue最大サイズ
	private final short SIZE = 256;
  /* 変数宣言 */
	//Queueバッファ
	private short[] queue = new short[SIZE];
	//Queueサイズ
	public short queSize = 0;
	
	/**
	 * Queueを初期化
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
	 * エンキュー(Enqueue)
	 * @param data エンキューする値
	 * @return　エンキューに成功したかどうか?
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
	 * デキュー(Dequeue)
	 * @return　デキューした値
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
	 * Queueが空かどうか?
	 * @return 空であればtrue
	 */
	public boolean isEmpty() {
		return (!(queSize > 0));
	}
}