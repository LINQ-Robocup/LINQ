package com.linq;

/**
 * Queue�\������N���X(�T�C�Y�͌Œ�) 
 * 
 * @author ASAHI
 */
public class Queue{
  /* �萔�錾 */
	//Queue�ő�T�C�Y
	private static final byte SIZE = 64;
  /* �ϐ��錾 */
	//Queue�o�b�t�@
	private static byte[] queue = new byte[SIZE];
	//Queue�T�C�Y
	private static byte queSize = 0;
	
	/**
	 * Queue��������
	 */
	Queue() {
		for(short i: queue) {
			queue[i] = 0;
		}
		queSize = 0;
	}
	
	/**
	 * �G���L���[(Enqueue)
	 * @param data �G���L���[����l
	 * @return�@�G���L���[�ɐ����������ǂ���?
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
	 * �f�L���[(Dequeue)
	 * @return�@�f�L���[�����l
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
	 * Queue���󂩂ǂ���?
	 * @return ��ł����true
	 */
	public boolean isEmpty() {
		return (!(queSize > 0));
	}
}