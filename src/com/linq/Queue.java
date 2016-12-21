package com.linq;

/**
 * Queue表現するクラス(サイズは固定) 
 * 
 * @author ASAHI
 */
public class Queue{
  /* 定数宣言 */
	//Queue最大サイズ
	private static final byte SIZE = 64;
  /* 変数宣言 */
	//Queueバッファ
	private static byte[] queue = new byte[SIZE];
	//Queueサイズ
	private static byte queSize = 0;
	
	/**
	 * Queueを初期化
	 */
	Queue() {
		for(short i: queue) {
			queue[i] = 0;
		}
		queSize = 0;
	}
	
	/**
	 * エンキュー(Enqueue)
	 * @param data エンキューする値
	 * @return　エンキューに成功したかどうか?
	 */
	public boolean enqueue(byte data) {
		if(queSize > queue.length) {
			System.out.println("Queue Error");
			return false;
		}
		queue[queSize] = data;
		queSize ++;
		return true;
	}
	
	/**
	 * デキュー(Dequeue)
	 * @return　デキューした値
	 */
	public byte dequeue() {
		byte data = queue[0];
		if(!isEmpty()) {
			for(byte i = 0; i < queue.length-1; i++) {
				queue[i] = queue[i+1];
			}
		}
		queSize --;
		return data;
	}
	
	/**
	 * Queueが空かどうか?
	 * @return 空であればtrue
	 */
	public boolean isEmpty() {
		return (!(queSize > 0));
	}
}