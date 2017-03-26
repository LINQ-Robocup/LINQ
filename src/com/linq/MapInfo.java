package com.linq;

import java.io.*;

import lejos.nxt.*;

import javax.microedition.lcdui.Graphics;

public class MapInfo {
  /* 定数宣言 */
	//　部屋の数(坂で接続された部屋の最大数)
	public static final byte ROOM 	= 2;
	// 1部屋のサイズ(縦・横)
	public static final byte HEIGHT = 5 * 2 + 1;
	public static final byte WIDTH  = 9 * 2 + 1;
	// マップ値
	public static final byte WALL 	= 99;
	public static final byte PASS 	= 2;
	public static final byte FLAG 	= 1;
	public static final byte UNKNOWN = 0;
	//方向
	public static final byte NORTH = 0;
	public static final byte EAST  = 1;
	public static final byte SOUTH = 2;
	public static final byte WEST  = 3;
	public static final byte X_D[] = {0, 1, 0, -1};
	public static final byte Y_D[] = {1, 0, -1, 0};
	//相対方向
	public static final byte RIGHT = 0;
	public static final byte FRONT = 1;
	public static final byte LEFT 	= 2;
	public static final byte BACK  = 3;
	//位置情報
	final static byte INITIAL_POS_X = 1;
	final static byte INITIAL_POS_Y = 1;
	final static byte INITIAL_DIREC = 0;
  /* 変数宣言 */
	//マップ情報
	public byte[][][] map = new byte[ROOM][HEIGHT][WIDTH];
	public byte doorwayEntX[] = new byte[2];
	public byte doorwayEntY[] = new byte[2];
	public byte doorwayExtX[] = new byte[2];
	public byte doorwayExtY[] = new byte[2];
	public byte doorwayExtDirec[] = new byte[2];
	// 現在位置(XY座標, 方向, 部屋)
	public byte curX, curY, curDirec, curRoom;
	
  /* Mapクラスのメソッド */
	/**
	 * マップ情報の初期化
	 * 迷路生成(デバック用)
	 */
	MapInfo(int x, int y) {
		/*　マップ情報を初期化 */
		for(byte i = 0; i < ROOM; i++) {
			for(byte j = 0; j < HEIGHT; j++) {
				for(byte k = 0; k < WIDTH; k++) {
					map[i][j][k] = 0;
					if(j % 2 == 0 && k % 2 == 0) {
						//頂点はWALLとして初期化
						map[i][j][k] = WALL;
					} else {
						map[i][j][k] = UNKNOWN;
					}
				}
			}
		}
		this.curX = (byte)x;
		this.curY = (byte)y;
		this.curDirec = INITIAL_DIREC;
		this.curRoom = 0;
	}
	
	/**
	 * 方向転換時における方向情報の修正
	 * @param Clockwise 回転方向) true:右回転, false:左回転 
	 */
	public void changeDirec(boolean Clockwise) {
		curDirec += (Clockwise) ? 1 : 3;
		curDirec %= 4;
	}
	
	/**
	 * タイル間移動(現在位置の更新)
	 */
	public void moveNextPosition() {
		switch(curDirec) {
			case NORTH: this.curY += 2; break;
			case EAST:	this.curX += 2; break;
			case SOUTH: this.curY -= 2; break;
			case WEST:	this.curX -= 2; break;
			default:
		}
	}
	
	/**
	 * 位置情報を初期化
	 */
	public void resetPosition() {
		this.curX = INITIAL_POS_X;
		this.curY = INITIAL_POS_Y;
		this.curDirec = INITIAL_DIREC;
		map[this.curRoom][this.curX][this.curY] = PASS;
	}
		
	/**
	 * 現在位置は通過済みであるか?
	 * @return 通過済み: true 未通過: false
	 */
	boolean isPassedThrough() {
		return (this.getCurTileInfo() > 0) ? true : false; 
	}
	
	/**
	 * 現在位置の情報を取得
	 * @return 現在座標の値
	 */
	byte getCurTileInfo() {
		return this.map[this.curRoom][this.curY][this.curX];
	}
	
	boolean isStartTile() {
		return (this.curX == this.doorwayEntX[this.curRoom] && this.curY == this.doorwayEntY[this.curRoom]) ?  true : false;
	}
	
	boolean isReachingFlag() {
		return (getCurTileInfo() == FLAG-1) ? true : false;
	}
	
	/**
	 * 現在位置の情報を取得
	 * @return 現在座標の値
	 */
	void setCurPosInfo(byte info) {
		map[this.curRoom][this.curY][this.curX] = info;
	}

	/**
	 * 前方の壁情報の取得
	 * @return　前方座標の値
	 */
	byte getWallFront() {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH: y += 1; break;
			case EAST:	x += 1; break;
			case SOUTH: y -= 1; break;
			case WEST:	x -= 1; break;
			default:
		}
		return map[this.curRoom][y][x];
	}
	
	/**
	 * 右側の壁情報の取得
	 * @return 右側座標の値
	 */
	byte getWallRight() {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH:	x += 1; break;
			case EAST:	y -= 1; break;
			case SOUTH:	x -= 1; break;
			case WEST:	y += 1; break;
			default:
		}
		return map[this.curRoom][y][x];
	}
	
	/**
	 * 左側の壁情報の取得
	 * @return　左側座標の取得
	 */
	byte getWallLeft() {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH:	x -= 1; break;
			case EAST:	y += 1; break;
			case SOUTH:	x += 1; break;
			case WEST:	y -= 1; break;
			default:
		}
		return map[this.curRoom][y][x];
	}
	
	/**
	 * 後方の壁情報の取得
	 * @return 後方座標の値
	 */
	byte getWallBack() {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH:	y -= 1; break;
			case EAST:	x -= 1; break;
			case SOUTH:	y += 1; break;
			case WEST:	x += 1; break;
			default:
		}
		return map[this.curRoom][y][x];
	}
	
	/**
	 * 前方の壁情報の入力
	 * @param info　
	 */
	void setWallFront(byte info) {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH: y += 1; break;
			case EAST:	x += 1; break;
			case SOUTH:	y -= 1; break;
			case WEST:	x -= 1; break;
			default:
		}
		map[this.curRoom][y][x] = info;
	}
	
	/**
	 * 右側の壁情報の取得
	 * @param info
	 */
	void setWallRight(byte info) {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH:	x += 1; break;
			case EAST:	y -= 1; break;
			case SOUTH:	x -= 1; break;
			case WEST:	y += 1; break;
			default:
		}
		map[this.curRoom][y][x] = info;
	}
	
	/**
	 * 左側の壁情報の取得
	 * @param info
	 */
	void setWallLeft(byte info) {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH: x -= 1; break;
			case EAST:	y += 1; break;
			case SOUTH:	x += 1; break;
			case WEST:	y -= 1; break;
			default:
		}
		map[this.curRoom][y][x] = info;
	}
	
	/**
	 * 後方の壁情報の取得
	 * @param info
	 */
	void setWallBack(byte info) {
		byte x = this.curX;
		byte y = this.curY;
		switch(this.curDirec) {
			case NORTH:	y -= 1; break;
			case EAST:	x -= 1; break;
			case SOUTH:	y += 1; break;
			case WEST:	x += 1; break;
			default:
		}
		map[this.curRoom][y][x] = info;
	}
	
	/**
	 * 前方のタイル座標を黒タイルに設定
	 */
	void setFrontBlack() {
		byte x = this.curX, y = this.curY;
		switch (this.curDirec) {
			case NORTH:	y += 2; break;
			case EAST:	x += 2; break;
			case SOUTH:	y -= 2; break;
			case WEST:	x -= 2; break;
			default:
		}
		map[this.curRoom][y][x] = WALL;
		map[this.curRoom][y+1][x] = WALL;
		map[this.curRoom][y][x+1] = WALL;
		map[this.curRoom][y-1][x] = WALL;
		map[this.curRoom][y][x-1] = WALL;
	}
	
	/**
	 * 自己位置に合わせたマップの整形(配列外参照の防止)
	 */
	public void arrangeMap() {
		/* 横シフト　(X座標の0と末端がUNKOWNの場合) */
		for(byte i = 0; i < HEIGHT; i++) {
			if(this.map[curRoom][i][0] == FLAG) {
				//右シフト 
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = WIDTH-1; k > 1; k--) {
						map[curRoom][j][k] = map[curRoom][j][k-2];
					}
					map[curRoom][j][0] = UNKNOWN;
					map[curRoom][j][1] = UNKNOWN;
				}
				this.curX += 2;
				this.doorwayEntX[curRoom] +=  2;
				this.doorwayExtX[curRoom] +=  2;
				break;
			}
		}
		/* 縦シフト (Y座標の0と末端がUNKOWNの場合)*/
		for(byte i = 0; i < WIDTH; i++) {
			if(map[curRoom][0][i] == FLAG) {
				//上シフト
				for(byte j = 0; j < WIDTH; j++) {
					for(byte k = HEIGHT-1; k > 1; k--) {
						map[curRoom][k][j] = map[curRoom][k-2][j];
					}
					map[curRoom][0][j] = UNKNOWN;
					map[curRoom][1][j] = UNKNOWN;
				}
				this.curY += 2;
				this.doorwayEntY[curRoom] += 2;
				this.doorwayExtY[curRoom] += 2;
				break;
			}
		}
	}
	
	public void setDoorwayExit() {
		doorwayExtX[curRoom] = this.curX;
		doorwayExtY[curRoom] = this.curY;
		doorwayExtDirec[curRoom] = this.curDirec;
	}

	/**
	 * 位置情報のリセット(部屋移動時)
	 */
	public void changeNextRoom() {
		setDoorwayExit();
		setWallFront(WALL);
		this.curRoom = 1;
		this.curX = doorwayEntX[this.curRoom] = INITIAL_POS_X;
		this.curY = doorwayEntY[this.curRoom] = INITIAL_POS_Y;
		this.curDirec = INITIAL_DIREC;
		setCurPosInfo(PASS);
		setWallLeft(WALL);
		setWallRight(WALL);
		setWallBack(WALL);
		setWallFront(FLAG);
	}
	
	/**
	 * 位置情報のリセット(部屋帰還時)
	 */
	public void changePrevRoom() {
		this.curRoom = 0;
		this.curX = doorwayExtX[this.curRoom];
		this.curY = doorwayExtY[this.curRoom];
		this.curDirec = (byte) ((doorwayExtDirec[this.curRoom] + 3) % 4);
	}
	
	/**
	 * 歩数マップのリセット
	 */
	public void resetDistanceMap() {
		for(byte i = 0; i < HEIGHT; i++) {
			for(byte j = 0; j < WIDTH; j++) {
				if(map[curRoom][i][j] > PASS && map[curRoom][i][j] < FLAG) {
					map[curRoom][i][j] = PASS;
				}
			}
		}
	}
	
	/**
	 * 目的位置までの歩数マップの作成
	 * @param x 目的位置のX座標
	 * @param y　目的位置のY座標
	 */	
	public void makeDistanceMap(int x, int y) {
		Queue queue = new Queue();
		queue.enqueue((byte)x);
		queue.enqueue((byte)y);
		map[curRoom][y][x] = FLAG;
		byte i = 0;
		try {
			while(!queue.isEmpty()) {
				x = queue.dequeue();
				y = queue.dequeue();
				for(i = 0; i < 4; i ++) {
					if(map[curRoom][y+Y_D[i]][x+X_D[i]] == PASS) {
						queue.enqueue((byte)(x+X_D[i]));
						queue.enqueue((byte)(y+Y_D[i]));
						map[curRoom][y+Y_D[i]][x+X_D[i]] = (byte) (map[curRoom][y][x] - 1);
					}
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			LCD.clear();
			LCD.drawString("EXCEPTION 28", 0, 0);
			LCD.drawString("in makeDistMap()", 0, 1);
			LCD.drawString(i+","+x+" "+X_D[i] + "," + y+" "+Y_D[i], 0, 2);
			while(true);
		}
	}
	
	public void searchFlag() {
		Queue queue = new Queue();
		byte x = 0, y = 0;
		byte i = 0;
		boolean flag = false;
		for(i = 0; i < HEIGHT; i++) {
			for(byte j = 0; j < WIDTH; j++) {
				if(map[curRoom][i][j] == FLAG) {
					flag = true;
					break;
				}
			}
		}
		if(flag == false) {
			makeDistanceMap(doorwayEntX[curRoom], doorwayEntY[curRoom]);
			return;
		}
		try {
			resetDistanceMap();
			queue.enqueue(this.curX);
			queue.enqueue(this.curY);
			while(!queue.isEmpty()) {
				x = (byte)queue.dequeue();
				y = (byte)queue.dequeue();
//				LCD.clear(0);
//				LCD.drawString("("+ x + "," + y + ")", 0, 0);
//				Delay.msDelay(100);
				for(i = 0; i < 4; i ++) {
					if(this.map[this.curRoom][y+Y_D[i]][x+X_D[i]] == PASS) {
						queue.enqueue((byte)(x+X_D[i]));
						queue.enqueue((byte)(y+Y_D[i]));
					} else if(this.map[this.curRoom][y+Y_D[i]][x+X_D[i]] == FLAG) {
						makeDistanceMap(x+X_D[i], y+Y_D[i]);
						return;
					}
				}
				
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			LCD.clear();
			LCD.drawString("EXCEPTION 28", 0, 0);
			LCD.drawString("in searchFlag()", 0, 1);
			LCD.drawString(i+","+x+" "+X_D[i] + "," + y+" "+Y_D[i]+","+queue.queSize , 0, 2);
			while(true);
		}
	}
	
	/**
	 * マップのリロード
	 */
	public boolean reload() {
		boolean flag = false;
		LCD.clear();
		while(true) {
			LCD.drawString("X:" + this.curX + " Y:" + this.curY + " D :" + this.curDirec, 0, 0);
			LCD.drawString("RELOAD -> RIGHT", 0, 1);
			LCD.drawString("RESET  -> LEFT",  0, 2);
			LCD.drawString("START  -> ENTER", 0, 3);
			if(Button.RIGHT.isDown()) {
				this.readFile();
				flag = true;
			} else if(Button.LEFT.isDown()) {  
				this.resetFile();
			} else if(Button.ENTER.isDown()) { 
				break;
			}
		}
		LCD.clear();
		while(Button.ENTER.isDown());
		this.dispMapInfo();
		while(!Button.ENTER.isDown());
		return flag;
	}
	
	/**
	 * マップ情報の保存(書き込み)
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
	    		dataOut.write(doorwayEntX[i]);
	    		dataOut.write(doorwayEntY[i]);
	    		dataOut.write(doorwayExtX[i]);
	    		dataOut.write(doorwayExtY[i]);
	    		dataOut.write(doorwayExtDirec[i]);
	    	}
	    	dataOut.write(this.curX);
    		dataOut.write(this.curY);
    		dataOut.write(this.curDirec);
    		dataOut.write(this.curRoom);
	    	out.close(); // flush the buffer and write the file
	    } catch (IOException e) {
	    	System.err.println("Failed to write to output stream");
	    }
	}
	
	/**
	 * ファイルのリセット
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
							//頂点はWALLとして初期化
							map[i][j][k] = WALL;
						} else {
							map[i][j][k] = UNKNOWN;
						}
						dataOut.write(map[i][j][k]);
					}
				}
			}
	    	for(byte i = 0; i < ROOM; i++) {
	    		dataOut.write(INITIAL_POS_X);
	    		dataOut.write(INITIAL_POS_Y);
	    		dataOut.write(INITIAL_POS_X);
	    		dataOut.write(INITIAL_POS_Y);
	    		dataOut.write(INITIAL_DIREC);
	    	}
	    	dataOut.write(INITIAL_POS_X);
    		dataOut.write(INITIAL_POS_Y);
    		dataOut.write(INITIAL_DIREC);
    		dataOut.write(0);
	    	out.close(); // flush the buffer and write the file
	    } catch (IOException e) {
	    	System.err.println("Failed to write to output stream");
	    }
	}
	
	
	/**
	 * マップ情報の読み込み
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
	    		doorwayEntX[i] = din.readByte();
	    		doorwayEntY[i] = din.readByte();
	    		doorwayExtX[i] = din.readByte();
	    		doorwayExtY[i] = din.readByte();
	    		doorwayExtDirec[i] = din.readByte();
	    	}
	    	this.curX = din.readByte();
    		this.curY = din.readByte();
    		this.curDirec = din.readByte();
    		this.curRoom = din.readByte();
			din.close();
		} catch (IOException ioe) {
			System.err.println("Read Exception");
		}
	}
	
	/**
	 * 作成したマップと自己位置情報をLCDに表示
	 * (dispMapとdispPositionを統合)
	 */
	public void dispMapInfo() {
		LCD.clear();
		dispMap();
		dispPosition();
	}
	
	/**
	 * マップ情報をLCDに表示
	 */
	public void dispMap() {
		Graphics g = new Graphics();
		final byte TILE_WIDTH = (byte) (WIDTH - 1) / 2;
		final byte TILE_HEIGHT = (byte) (HEIGHT - 1) / 2;
		/* 縦壁の描画 */
		for(byte i = 0; i < TILE_HEIGHT; i++) {
			for(byte j = 0; j <= TILE_WIDTH; j++) {
				if(map[curRoom][i*2+1][j*2] == WALL) {
					g.drawLine(j*10, 63-i*10-1, j*10, 63-i*10-9);
				} else if(map[curRoom][i*2+1][j*2] == FLAG) {
					for(int l = 63-i*10-1; l < 63-i*10-9; l += 2) {
						g.drawLine(j*10, l, j*10, l);
					}
				}
			}
		}
		/* 横壁の描画 */
		for(byte i = 0; i <= TILE_HEIGHT; i++) {
			for(int j = 0; j < TILE_WIDTH; j++) {
				if(map[curRoom][i*2][j*2+1] == WALL) {
					g.drawLine(j*10+1, 63-i*10, j*10+9, 63-i*10);
				} else if(map[curRoom][i*2][j*2+1] == FLAG) {
					for(int l = j*10+1; l < j*10+9; l += 2) {
						g.drawLine(l, 63-i*10, l, 63-i*10);
					}
				}
			}
		}
		/* タイル描画 */
		for(byte i = 0; i < TILE_HEIGHT; i++) {
		    for(byte j = 0; j < TILE_WIDTH; j++) {
		    	if(map[curRoom][i*2+1][j*2+1] == WALL) {
		    		/* 黒タイルの描画 */
		    		for(byte k = 2; k <= 8; k ++) {
		    			g.drawLine(j * 10 + 2, 63-(i * 10 + k), j * 10 + 8, 63-(i * 10 + k));
		    		}
		    	} else if(map[curRoom][i*2+1][j*2+1] == UNKNOWN && !(j*2+1 == this.curX && i*2+1 == this.curY)) {
		    		/* バツ印の描画 */
		    		g.drawLine(j * 10 + 4, 63-(i * 10 + 6), j * 10 + 6, 63-(i * 10 + 4));
		    		g.drawLine(j * 10 + 4, 63-(i * 10 + 4), j * 10 + 6, 63-(i * 10 + 6));
		    	}
		    }
		}
	}
	
	/**
	 * 自己位置情報(座標,矢印)をLCDに表示
	 */
	public void dispPosition() {
//		LCD.clear(1);
		Graphics g = new Graphics();
		String posInfo = "X:" + this.curX + " Y:" + this.curY + " D :" + this.curDirec;
//		String refInfo = "F:" + getWallFront() + 
//						" B:" + getWallBack() + 
//						" R:" + getWallRight() +
//						" L:" + getWallLeft();
		LCD.drawString(posInfo, 0, 0);
//		LCD.drawString(refInfo, 0, 1);
		final byte x = (byte) (this.curX - (this.curX / 2) - 1);
		final byte y = (byte) (this.curY - (this.curY / 2) - 1);
		switch(this.curDirec) {
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