package com.linq;

import java.io.*;

import lejos.nxt.*;
import lejos.util.Delay;

import javax.microedition.lcdui.Graphics;

public class MapInfo {
  /* �萔�錾 */
	//�@�����̐�(��Őڑ����ꂽ�����̍ő吔)
	public static final byte ROOM 	= 2;
	// 1�����̃T�C�Y(�c�E��)
	public static final byte HEIGHT = 5 * 2 + 1;
	public static final byte WIDTH  = 9 * 2 + 1;
	// �}�b�v�l
	public final byte WALL 	= 1;
	public final byte PASS 	= 2;
	public final byte FLAG 	= 99;
	public final byte UNKNOWN = 0;
	//����
	public final byte NORTH = 0;
	public final byte EAST  = 1;
	public final byte SOUTH = 2;
	public final byte WEST  = 3;
	public final byte X_D[] = {0, 1, 0, -1};
	public final byte Y_D[] = {1, 0, -1, 0};
	//���Ε���
	public final boolean RIGHT = true;
	public final boolean LEFT = false;
  /* �ϐ��錾 */
	//�}�b�v���
	public byte[][][] map = new byte[ROOM][HEIGHT][WIDTH];
  
  /* �����N���X */
	/**
	 * ���݈ʒu�̑�����s���N���X
	 * (XY���W, ����, ����)
	 */
	public class CurrentPosition {
	  /* �萔�錾 */
		final byte INITIAL_POS_X = 1;
		final byte INITIAL_POS_Y = 1;
		final byte INITIAL_DIREC = 0;
	  /* �ϐ��錾 */
		// ���݈ʒu(XY���W, ����, ����)
		byte x = 1, y = 1, room = 0, direc = 0;
		
		// �R���X�g���N�^(�����ʒu�ݒ�)
		CurrentPosition() {
			this.x = INITIAL_POS_X;
			this.y = INITIAL_POS_Y;
			this.direc = INITIAL_DIREC;
			this.room = 0;
		}
		
		/**
		 * X������(������)�̃}�b�v�z��̒l��1�������V�t�g
		 * @param which �V�t�g�����̑I��) true:�E�V�t�g+2, false:���V�t�g-2 
		 */
		public void shiftX(boolean which) {
			x += (which) ? 2 : -2;
		}
		
		/**
		 * Y������(�c����)�̃}�b�v�z��̒l��1�������V�t�g
		 * @param which �V�t�g�����̑I��) true:��V�t�g+2, false:���V�t�g-2
		 */
		public void shiftY(boolean which) {
			y += (which) ? 2 : -2;
		}
		
		/**
		 * �����]�����ɂ�����������̏C��
		 * @param Clockwise ��]����) true:�E��], false:����] 
		 */
		public void changeDirec(boolean Clockwise) {
			direc += (Clockwise) ? 1 : 3;
			direc %= 4;
		}
		
		public void changePos() {
			switch(direc) {
				case 0:
					curPos.y += 2; 
					break;
				case 1:
					curPos.x += 2;
					break;
				case 2:
					curPos.y -= 2;
					break;
				case 3:
					curPos.x -= 2;
					break;
				default:
			}
		}
		
		public void resetPosition() {
			x = INITIAL_POS_X;
			y = INITIAL_POS_Y;
			direc = INITIAL_DIREC;
			map[room][x][y] = PASS;
		}
		
		void setFrontPass() {
			switch(direc) {
				case 0:
					map[room][y+1][x] = PASS;
					break;
				case 1:
					map[room][y][x+1] = PASS;
					break;
				case 2:
					map[room][y-1][x] = PASS;
					break;
				case 3:
					map[room][y][x-1] = PASS;
					break;
				default:
			}
//			map[room][y+Y_D[direc]][x+X_D[direc]] = PASS;
		}
		
		boolean isPassedThrough() {
			return (map[room][y][x] == PASS) ? true : false; 
		}
		
		byte getCurPos() {
			return map[room][y][x];
		}
		
		byte getWallFront() {
			switch(direc) {
				case 0:
					return map[room][y+1][x];
				case 1:
					return map[room][y][x+1];
				case 2:
					return map[room][y-1][x];
				case 3:
					return map[room][y][x-1];
				default:
			}
			return WALL;
		}
		
		byte getWallRight() {
			switch(direc) {
				case 0:
					return map[room][y][x+1];
				case 1:
					return map[room][y-1][x];
				case 2:
					return map[room][y][x-1];
				case 3:
					return map[room][y+1][x];
				default:
			}
			return WALL;
		}
		
		byte getWallLeft() {
			switch(direc) {
				case 0:
					return map[room][y][x-1];
				case 1:
					return map[room][y+1][x];
				case 2:
					return map[room][y][x+1];
				case 3:
					return map[room][y-1][x];
				default:
			}
			return WALL;
		}
		
		byte getWallBack() {
			switch(direc) {
				case 0:
					return map[room][y-1][x];
				case 1:
					return map[room][y][x-1];
				case 2:
					return map[room][y+1][x];
				case 3:
					return map[room][y][x+1];
				default:
			}
			return WALL;
		}
		
		void setWallFront(byte info) {
			switch(direc) {
				case 0:
					map[room][y+1][x] = info;
				case 1:
					map[room][y][x+1] = info;
				case 2:
					map[room][y-1][x] = info;
				case 3:
					map[room][y][x-1] = info;
				default:
			}
		}
		
		void setWallRight(byte info) {
			switch(direc) {
				case 0:
					map[room][y][x+1] = info;
				case 1:
					map[room][y-1][x] = info;
				case 2:
					map[room][y][x-1] = info;
				case 3:
					map[room][y+1][x] = info;
				default:
			}
		}
		
		void setWallLeft(byte info) {
			switch(direc) {
				case 0:
					map[room][y][x-1] = info;
				case 1:
					map[room][y+1][x] = info;
				case 2:
					map[room][y][x-1] = info;
				case 3:
					map[room][y-1][x] = info;
				default:
			}
		}
		
		void setWallBack(byte info) {
			switch(direc) {
				case 0:
					map[room][y-1][x] = info;
				case 1:
					map[room][y][x-1] = info;
				case 2:
					map[room][y+1][x] = info;
				case 3:
					map[room][y][x+1] = info;
				default:
			}
		}
		
		void setFrontBlack() {
			final byte d_x[] = {0, 2, 0, -2};
			final byte d_y[] = {2, 0, -2, 0};
			map[room][x+d_x[direc]][y+d_y[direc]] = WALL;
			map[room][x+d_x[direc]+1][y+d_y[direc]] = WALL;
			map[room][x+d_x[direc]][y+d_y[direc]+1] = WALL;
			map[curPos.room][x+d_x[direc]-1][y+d_y[direc]] = WALL;
			map[curPos.room][x+d_x[direc]][y+d_y[direc]-1] = WALL;
		}
	}
	CurrentPosition curPos = new CurrentPosition();
	
	/**
	 * �o������̈ʒu���𑀍삷��N���X
	 *
	 */
	public class DoorwayPosition {
	  /* �萔�錾 */
		final byte INITIAL_X = 1;
		final byte INITIAL_Y = 1;
		final byte INITIAL_DIREC = 0;
	  /* �ϐ��錾 */
		// �o������ʒu(XY���W, ����)
		byte ent_x, ent_y, ent_direc;
		byte ext_x, ext_y, ext_direc;
		
		//�R���X�g���N�^(�����ʒu�ݒ�)
		DoorwayPosition() {
			this.ent_x = this.ext_x = INITIAL_X;
			this.ent_y = this.ext_y = INITIAL_Y;
			this.ent_direc = this.ext_direc = INITIAL_DIREC;
		}
		
		void reset() {
			this.ent_x = this.ext_x = INITIAL_X;
			this.ent_y = this.ext_y = INITIAL_Y;
			this.ent_direc = this.ext_direc = INITIAL_DIREC;
		}
		
		/**
		 * �o��(��)�̈ʒu�������
		 * @param x X���W
		 * @param y Y���W
		 * @param d ����(direction)
		 */
		public void setExit(byte x, byte y, byte d) {
			this.ext_x = x;
			this.ext_y = y;
			this.ext_direc = d;
		}
		
		/**
		 * X������(������)�̃}�b�v�z��̒l��1�������V�t�g
		 * @param which �V�t�g�����̑I��) true:�E�V�t�g+2, false:���V�t�g-2 
		 */
		public void shiftX(boolean which) {
			ent_x += (which) ? 2 : -2;
			ext_x += (which) ? 2 : -2;
		}
		
		/**
		 * Y������(�c����)�̃}�b�v�z��̒l��1�������V�t�g
		 * @param which �V�t�g�����̑I��) true:��V�t�g+2, false:���V�t�g-2
		 */
		public void shiftY(boolean which) {
			ent_y += (which) ? 2 : -2;
			ext_y += (which) ? 2 : -2;
		}
	}
	DoorwayPosition[] doorway = new DoorwayPosition[ROOM]; 
	
  /* Map�N���X�̃��\�b�h */
	/**
	 * �}�b�v���̏�����
	 * ���H����(�f�o�b�N�p)
	 */
	MapInfo() {
		/*�@�}�b�v���������� */
		for(byte i = 0; i < ROOM; i++) {
			for(byte j = 0; j < HEIGHT; j++) {
				for(byte k = 0; k < WIDTH; k++) {
					map[i][j][k] = 0;
					if(j % 2 == 0 && k % 2 == 0) {
						//���_��WALL�Ƃ��ď�����
						map[i][j][k] = WALL;
					} else {
						map[i][j][k] = UNKNOWN;
					}
				}
			}
		}
		/* ���Ȉʒu����������:1������, ���W(1,1), �k���� */
		curPos.resetPosition();
	}
	
	/**
	 * ���Ȉʒu�ɍ��킹���}�b�v�̐��`(�z��O�Q�Ƃ̖h�~)
	 */
	public void arrangeMap() {
		/* ���V�t�g�@(X���W��0�Ɩ��[��UNKOWN�̏ꍇ) */
		for(byte i = 0; i < HEIGHT; i++) {
			if(map[curPos.room][i][0] == FLAG) {
				//�E�V�t�g 
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = WIDTH-1; k > 1; k--) {
						map[curPos.room][j][k] = map[curPos.room][j][k-2];
					}
					map[curPos.room][j][0] = UNKNOWN;
					map[curPos.room][j][1] = UNKNOWN;
				}
				//curPos.shiftX(true);
				//doorway[curPos.room].shiftX(true);
				break;
			}
			if(map[curPos.room][i][WIDTH-1] == FLAG) {
				//���V�t�g
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = 0; k < WIDTH-2; k++) {
						map[curPos.room][j][k] = map[curPos.room][j][k+2];
					}
					map[curPos.room][j][WIDTH-1] = UNKNOWN;
					map[curPos.room][j][WIDTH-2] = UNKNOWN;
				}
				curPos.shiftX(false);
				//doorway[curPos.room].shiftX(false);
				break;
			}
		}
		/* �c�V�t�g (Y���W��0�Ɩ��[��UNKOWN�̏ꍇ)*/
		for(byte i = 0; i < WIDTH; i++) {
			if(map[curPos.room][HEIGHT-1][i] == FLAG) {
				//��V�t�g
				for(byte j = 0; j < WIDTH; j++) {
					for(byte k = 0; k < HEIGHT-2; k++) {
						map[curPos.room][k][j] = map[curPos.room][k+2][j];
					}
					map[curPos.room][HEIGHT-1][j] = UNKNOWN;
					map[curPos.room][HEIGHT-2][j] = UNKNOWN;
				}
				curPos.shiftY(true);
				//doorway[curPos.room].shiftY(true);
				break;
			}
			if(map[curPos.room][0][i] == FLAG) {
				//���V�t�g
				for(byte j = 0; j < WIDTH; j++) {
					for(byte k = HEIGHT-1; k > 1; k--) {
						map[curPos.room][k][j] = map[curPos.room][k-2][j];
					}
					map[curPos.room][0][j] = UNKNOWN;
					map[curPos.room][1][j] = UNKNOWN;
				}
				curPos.shiftY(false);
				//doorway[curPos.room].shiftY(false);
				break;
			}
		}
	}
	
	/**
	 * �ʒu���̃��Z�b�g(�����ړ���)
	 */
	public void changeNextRoom() {
		doorway[curPos.room].ext_x = curPos.x;
		doorway[curPos.room].ext_y = curPos.y;
		doorway[curPos.room].ext_direc = curPos.direc;
		curPos.room += 1;
		curPos.x = doorway[curPos.room].ent_x = curPos.INITIAL_POS_X;
		curPos.y = doorway[curPos.room].ent_y = curPos.INITIAL_POS_Y;
		curPos.direc = curPos.INITIAL_DIREC;
		map[curPos.room][curPos.y][curPos.x] = PASS;
	}
	
	/**
	 * �ʒu���̃��Z�b�g(�����A�Ҏ�)
	 */
	public void changePrevRoom() {
		curPos.room -= 1;
		curPos.x = doorway[curPos.room].ext_x;
		curPos.y = doorway[curPos.room].ext_y;
		curPos.direc = (byte) ((doorway[curPos.room].ext_direc + 3) % 4);
	}
	
	/**
	 * �����}�b�v�̃��Z�b�g
	 */
	public void resetDistanceMap() {
		for(byte i = 0; i < HEIGHT; i++) {
			for(byte j = 0; j < WIDTH; j++) {
				if(map[curPos.room][i][j] > PASS && map[curPos.room][i][j] < FLAG) {
					map[curPos.room][i][j] = PASS;
				}
			}
		}
	}
	
	/**
	 * �ړI�ʒu�܂ł̕����}�b�v�̍쐬
	 * @param x �ړI�ʒu��X���W
	 * @param y�@�ړI�ʒu��Y���W
	 */	
	public void makeDistanceMap(int x, int y) {
		Queue queue = new Queue();
		queue.enqueue((byte)x);
		queue.enqueue((byte)y);
		map[curPos.room][y][x] = FLAG;//=99
		while(!queue.isEmpty()) {
			x = queue.dequeue();
			y = queue.dequeue();
			for(byte i = 0; i < 4; i ++) {
				if(map[curPos.room][y+Y_D[i]][x+X_D[i]] == PASS) {
					queue.enqueue((byte)(x+X_D[i]));
					queue.enqueue((byte)(y+Y_D[i]));
					map[curPos.room][y+Y_D[i]][x+X_D[i]] = (byte) (map[curPos.room][y][x] - 1);
				}
			}
		}
	}
	
	public void searchFlag() {
		Queue queue = new Queue();
		byte x, y;
		queue.enqueue(curPos.x);
		queue.enqueue(curPos.y);
		while(!queue.isEmpty()) {
			x = queue.dequeue();
			y = queue.dequeue();
			for(byte i = 0; i < 4; i ++) {
				if(map[curPos.room][y+Y_D[i]][x+X_D[i]] == PASS) {
					queue.enqueue((byte)(x+X_D[i]));
					queue.enqueue((byte)(y+Y_D[i]));
				} else if(map[curPos.room][y+Y_D[i]][x+X_D[i]] == FLAG) {
					makeDistanceMap(x+X_D[i], y+Y_D[i]);
					return;
				}
			}
		}
		makeDistanceMap(doorway[curPos.room].ent_x, doorway[curPos.room].ent_y);
	}
	
	/**
	 * �}�b�v���̕ۑ�(��������)
	 */
	public void writeFile() {
		FileOutputStream out = null; // declare outside the try block
	    File data = new File("log.dat");
	    try {
	    	out = new FileOutputStream(data);
	    } catch(IOException e) {
	    	System.err.println("Failed to create output stream");
	    	System.exit(1);
	    }
	    DataOutputStream dataOut = new DataOutputStream(out);
	    
	    try { // write
	    	for(byte i = 0; i < ROOM; i++) {
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = 0; k < WIDTH; k++) {
						dataOut.write(map[i][j][k]);
					}
				}
			}
	    	for(byte i = 0; i < ROOM; i++) {
	    		dataOut.write(doorway[i].ent_x);
	    		dataOut.write(doorway[i].ent_y);
	    		dataOut.write(doorway[i].ent_direc);
	    		dataOut.write(doorway[i].ext_x);
	    		dataOut.write(doorway[i].ext_y);
	    		dataOut.write(doorway[i].ext_direc);
	    	}
	    	dataOut.write(curPos.x);
    		dataOut.write(curPos.y);
    		dataOut.write(curPos.direc);
    		dataOut.write(curPos.room);
	    	out.close(); // flush the buffer and write the file
	    } catch (IOException e) {
	    	System.err.println("Failed to write to output stream");
	    }
	}
	
	/**
	 * �t�@�C���̃��Z�b�g
	 */
	public void resetFile() {
		FileOutputStream out = null; // declare outside the try block
	    File data = new File("log.dat");
	    try {
	    	out = new FileOutputStream(data);
	    } catch(IOException e) {
	    	System.err.println("Failed to create output stream");
	    	System.exit(1);
	    }
	    DataOutputStream dataOut = new DataOutputStream(out);
	    
	    try { // write
	    	for(byte i = 0; i < ROOM; i++) {
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = 0; k < WIDTH; k++) {
						if(j % 2 == 0 && k % 2 == 0) {
							//���_��WALL�Ƃ��ď�����
							map[i][j][k] = WALL;
						} else {
							map[i][j][k] = UNKNOWN;
						}
						dataOut.write(map[i][j][k]);
					}
				}
			}
	    	for(byte i = 0; i < ROOM; i++) {
	    		dataOut.write(doorway[i].INITIAL_X);
	    		dataOut.write(doorway[i].INITIAL_Y);
	    		dataOut.write(doorway[i].INITIAL_DIREC);
	    		dataOut.write(doorway[i].INITIAL_X);
	    		dataOut.write(doorway[i].INITIAL_Y);
	    		dataOut.write(doorway[i].INITIAL_DIREC);
	    	}
	    	dataOut.write(curPos.INITIAL_POS_X);
    		dataOut.write(curPos.INITIAL_POS_Y);
    		dataOut.write(curPos.INITIAL_DIREC);
    		dataOut.write(0);
	    	out.close(); // flush the buffer and write the file
	    } catch (IOException e) {
	    	System.err.println("Failed to write to output stream");
	    }
	}
	
	
	/**
	 * �}�b�v���̓ǂݍ���
	 */
	public void readFile() {
		File data = new File("log.dat");
		try {
			InputStream is = new FileInputStream(data);
			DataInputStream din = new DataInputStream(is);
			for(byte i = 0; i < ROOM; i++) {
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = 0; k < WIDTH; k++) {
						map[i][j][k] = din.readByte(); 
					}
				}
			}
	    	for(byte i = 0; i < ROOM; i++) {
	    		doorway[i].ent_x = din.readByte();
	    		doorway[i].ent_y = din.readByte();
	    		doorway[i].ent_direc = din.readByte();
	    		doorway[i].ext_x = din.readByte();
	    		doorway[i].ext_y = din.readByte();
	    		doorway[i].ext_direc = din.readByte();
	    	}
	    	curPos.x = din.readByte();
    		curPos.y = din.readByte();
    		curPos.direc = din.readByte();
    		curPos.room = din.readByte();
			din.close();
		} catch (IOException ioe) {
			System.err.println("Read Exception");
		}
	}
	
	/**
	 * �쐬�����}�b�v�Ǝ��Ȉʒu����LCD�ɕ\��
	 * (dispMap��dispPosition�𓝍�)
	 */
	public void dispMapInfo() {
		LCD.clear();
		//dispMap();
		//dispPosition();
	}
	
	/**
	 * �}�b�v����LCD�ɕ\��
	 */
	public void dispMap() {
		Graphics g = new Graphics();
		final byte TILE_WIDTH = (byte) (WIDTH - 1) / 2;
		final byte TILE_HEIGHT = (byte) (HEIGHT - 1) / 2;
		/* �c�ǂ̕`�� */
		for(byte i = 0; i < TILE_HEIGHT; i++) {
			for(byte j = 0; j <= TILE_WIDTH; j++) {
				if(map[curPos.room][i*2+1][j*2] == WALL) {
					g.drawLine(j*10, 63-i*10-1, j*10, 63-i*10-9);
				} 
			}
		}
		/* ���ǂ̕`�� */
		for(byte i = 0; i <= TILE_HEIGHT; i++) {
			for(int j = 0; j < TILE_WIDTH; j++) {
				if(map[curPos.room][i*2][j*2+1] == WALL) {
					g.drawLine(j*10+1, 63-i*10, j*10+9, 63-i*10);
				}
			}
		}
		/* �^�C���`�� */
//		for(byte i = 0; i < TILE_HEIGHT; i++) {
//		    for(byte j = 0; j < TILE_WIDTH; j++) {
//		    	if(map[curPos.room][i*2+1][j*2+1] == WALL) {
//		    		/* ���^�C���̕`�� */
//		    		/*
//		    		for(byte k = 2; k <= 8; k ++) {
//		    			g.drawLine(i * 10 + 2, j * 10 + k, j * 10 + 8, i * 10 + k);
//		    		}
//		    		*/
//		    	} else if(map[curPos.room][j*2+1][i*2+1] == UNKNOWN && !(i*2+1 == curPos.x && j*2+1 == curPos.y)) {
//		    		/* �o�c��̕`�� */
//		    		g.drawLine(j * 10 + 4, i * 10 + 6, j * 10 + 6, i * 10 + 4);
//		    		g.drawLine(j * 10 + 4, i * 10 + 4, j * 10 + 6, i * 10 + 6);
//		    	}
//		    }
//		}
	}
	
	/**
	 * ���Ȉʒu���(���W,���)��LCD�ɕ\��
	 */
	public void dispPosition() {
		Graphics g = new Graphics();
		String posInfo = "X:" + curPos.x + " Y:" + curPos.y + " D :" + curPos.direc;
		LCD.drawString(posInfo, 0, 0);
		final byte x = (byte) (curPos.x - (curPos.x / 2) - 1);
		final byte y = (byte) (curPos.y - (curPos.y / 2) - 1);
		switch(curPos.direc) {
			case NORTH:
				g.drawLine(x * 10 + 5, 63 - (y * 10 + 2), x * 10 + 5, 63 - (y * 10 + 8));
				g.drawLine(x * 10 + 2, 63 - (y * 10 + 5), x * 10 + 5, 63 - (y * 10 + 8));
				g.drawLine(x * 10 + 5, 63 - (y * 10 + 8), x * 10 + 8, 63 - (y * 10 + 5));
				break;
			case EAST:
				g.drawLine(x * 10 + 2, 63 - (y * 10 + 5), x * 10 + 8, 63 - (y * 10 + 5));
				g.drawLine(x * 10 + 5, 63 - (y * 10 + 8), x * 10 + 8, 63 - (y * 10 + 5));
				g.drawLine(x * 10 + 5, 63 - (y * 10 + 2), x * 10 + 8, 63 - (y * 10 + 5));
				break;
			case SOUTH:
				g.drawLine(x * 10 + 5, 63 - (y * 10 + 2), x * 10 + 5, 63 - (y * 10 + 8));
				g.drawLine(x * 10 + 2, 63 - (y * 10 + 5), x * 10 + 5, 63 - (y * 10 + 2));
				g.drawLine(x * 10 + 8, 63 - (y * 10 + 5), x * 10 + 5, 63 - (y * 10 + 2));
				break;
			case WEST:
				g.drawLine(x * 10 + 2, 63 - (y * 10 + 5), x * 10 + 8, 63 - (y * 10 + 5));
				g.drawLine(x * 10 + 5, 63 - (y * 10 + 8), x * 10 + 2, 63 - (y * 10 + 5));
				g.drawLine(x * 10 + 2, 63 - (y * 10 + 5), x * 10 + 5, 63 - (y * 10 + 2));
				break;
			default:
		}
	}
}