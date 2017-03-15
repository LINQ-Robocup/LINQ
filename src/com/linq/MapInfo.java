package com.linq;

import java.io.*;
import lejos.nxt.*;
//import lejos.util.Delay;

import javax.microedition.lcdui.Graphics;

public class MapInfo {
  /* 定数宣言 */
	//　部屋の数(坂で接続された部屋の最大数)
	public static final byte ROOM 	= 2;
	// 1部屋のサイズ(縦・横)
	public static final byte HEIGHT = 5 * 2 + 1;
	public static final byte WIDTH  = 9 * 2 + 1;
	// マップ値
	public final byte WALL 	= 1;
	public final byte PASS 	= 2;
	public final byte FLAG 	= 99;
	public final byte UNKNOWN = 0;
	//方向
	public final byte NORTH = 0;
	public final byte EAST  = 1;
	public final byte SOUTH = 2;
	public final byte WEST  = 3;
	public final byte X_D[] = {0, 1, 0, -1};
	public final byte Y_D[] = {1, 0, -1, 0};
	//相対方向
	public final boolean RIGHT = true;
	public final boolean LEFT = false;
  /* 変数宣言 */
	//マップ情報
	public byte[][][] map = new byte[ROOM][HEIGHT][WIDTH];
	public byte doorway_ent_x[] = new byte[2];
	public byte doorway_ent_y[] = new byte[2];
	public byte doorway_ext_x[] = new byte[2];
	public byte doorway_ext_y[] = new byte[2];
	public byte doorway_ext_direc[] = new byte[2];
  
  /* 内部クラス */
	/**
	 * 現在位置の操作を行うクラス
	 * (XY座標, 部屋, 方向)
	 */
	public class CurrentPosition {
	  /* 定数宣言 */
		final byte INITIAL_POS_X = 1;
		final byte INITIAL_POS_Y = 1;
		final byte INITIAL_DIREC = 0;
	  /* 変数宣言 */
		// 現在位置(XY座標, 部屋, 方向)
		byte x = INITIAL_POS_X;
		byte y = INITIAL_POS_Y;
		byte direc = INITIAL_DIREC;
		byte room = 0;
		
		// コンストラクタ(初期位置設定)
//		CurrentPosition() {
//			this.x = INITIAL_POS_X;
//			this.y = INITIAL_POS_Y;
//			this.direc = INITIAL_DIREC;
//			this.room = 0;
//		}
		
		/**
		 * X軸方向(横方向)のマップ配列の値を1部屋分シフト
		 * @param which シフト方向の選択) true:右シフト+2, false:左シフト-2 
		 */
		public void shiftX(boolean which) {
			this.x += (which) ? 2 : -2;
		}
		
		/**
		 * Y軸方向(縦方向)のマップ配列の値を1部屋分シフト
		 * @param which シフト方向の選択) true:上シフト+2, false:下シフト-2
		 */
		public void shiftY(boolean which) {
			this.y += (which) ? 2 : -2;
		}
		
		/**
		 * 方向転換時における方向情報の修正
		 * @param Clockwise 回転方向) true:右回転, false:左回転 
		 */
		public void changeDirec(boolean Clockwise) {
			direc += (Clockwise) ? 1 : 3;
			direc %= 4;
		}
		
		public void changePos() {
			switch(direc) {
				case 0:
					this.y += 2; 
					break;
				case 1:
					this.x += 2;
					break;
				case 2:
					this.y -= 2;
					break;
				case 3:
					this.x -= 2;
					break;
				default:
			}
		}
		
		public void resetPosition() {
			this.x = INITIAL_POS_X;
			this.y = INITIAL_POS_Y;
			this.direc = INITIAL_DIREC;
			map[this.room][this.x][this.y] = PASS;
		}
		
		void setFrontPass() {
			switch(direc) {
				case 0:
					map[this.room][this.y+1][this.x] = PASS;
					break;
				case 1:
					map[this.room][this.y][this.x+1] = PASS;
					break;
				case 2:
					map[this.room][this.y-1][this.x] = PASS;
					break;
				case 3:
					map[this.room][this.y][this.x-1] = PASS;
					break;
				default:
			}
		}
		
		boolean isPassedThrough() {
			return (map[this.room][this.y][this.x] == PASS) ? true : false; 
		}
		
		byte getCurPos() {
			return map[this.room][this.y][this.x];
		}

		byte getWallFront() {
			switch(this.direc) {
				case 0:
					return map[this.room][this.y+1][this.x];
				case 1:
					return map[this.room][this.y][this.x+1];
				case 2:
					return map[this.room][this.y-1][this.x];
				case 3:
					return map[this.room][this.y][this.x-1];
				default:
			}
			return WALL;
		}
		
		byte getWallRight() {
			switch(this.direc) {
				case 0:
					return map[this.room][this.y][this.x+1];
				case 1:
					return map[this.room][this.y-1][this.x];
				case 2:
					return map[this.room][this.y][this.x-1];
				case 3:
					return map[this.room][this.y+1][this.x];
				default:
			}
			return WALL;
		}
		
		byte getWallLeft() {
			switch(this.direc) {
				case 0:
					return map[this.room][this.y][this.x-1];
				case 1:
					return map[this.room][this.y+1][this.x];
				case 2:
					return map[this.room][this.y][this.x+1];
				case 3:
					return map[this.room][this.y-1][this.x];
				default:
			}
			return WALL;
		}
		
		byte getWallBack() {
			switch(this.direc) {
				case 0:
					return map[this.room][this.y-1][this.x];
				case 1:
					return map[this.room][this.y][this.x-1];
				case 2:
					return map[this.room][this.y+1][this.x];
				case 3:
					return map[this.room][this.y][this.x+1];
				default:
			}
			return WALL;
		}
		
		void setWallFront(byte info) {
			switch(this.direc) {
				case 0:
					map[this.room][this.y+1][this.x] = info;
					break;
				case 1:
					map[this.room][this.y][this.x+1] = info;
					break;
				case 2:
					map[this.room][this.y-1][this.x] = info;
					break;
				case 3:
					map[this.room][this.y][this.x-1] = info;
					break;
				default:
			}
		}
		
		void setWallRight(byte info) {
			switch(this.direc) {
				case 0:
					map[this.room][this.y][this.x+1] = info;
					break;
				case 1:
					map[this.room][this.y-1][this.x] = info;
					break;
				case 2:
					map[this.room][this.y][this.x-1] = info;
					break;
				case 3:
					map[this.room][this.y+1][this.x] = info;
					break;
				default:
			}
		}
		
		void setWallLeft(byte info) {
			switch(this.direc) {
				case 0:
					map[this.room][this.y][this.x-1] = info;
					break;
				case 1:
					map[this.room][this.y+1][this.x] = info;
					break;
				case 2:
					map[this.room][this.y][this.x+1] = info;
					break;
				case 3:
					map[this.room][this.y-1][this.x] = info;
					break;
				default:
			}
		}
		
		void setWallBack(byte info) {
			switch(this.direc) {
				case 0:
					map[this.room][this.y-1][this.x] = info;
					break;
				case 1:
					map[this.room][this.y][this.x-1] = info;
					break;
				case 2:
					map[this.room][this.y+1][this.x] = info;
					break;
				case 3:
					map[this.room][this.y][this.x+1] = info;
					break;
				default:
			}
		}
		
		void setFrontBlack() {
			byte x = this.x;
			byte y = this.y;
			switch (this.direc) {
				case 0:
					y += 2;
					break;
				case 1:
					x += 2;
					break;
				case 2:
					y -= 2;
					break;
				case 3:
					x -= 2;
					break;
				default:
					break;
			}
			map[this.room][y][x] = WALL;
			map[this.room][y+1][x] = WALL;
			map[this.room][y][x+1] = WALL;
			map[this.room][y-1][x] = WALL;
			map[this.room][y][x-1] = WALL;
		}
		
	}
	CurrentPosition curPos = new CurrentPosition();
		
  /* Mapクラスのメソッド */
	/**
	 * マップ情報の初期化
	 * 迷路生成(デバック用)
	 */
	MapInfo() {
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
		/* 自己位置情報を初期化:1部屋目, 座標(1,1), 北方向 */
		curPos.resetPosition();
	}
	
	/**
	 * 自己位置に合わせたマップの整形(配列外参照の防止)
	 */
	public void arrangeMap() {
		/* 横シフト　(X座標の0と末端がUNKOWNの場合) */
		for(byte i = 0; i < HEIGHT; i++) {
			if(map[curPos.room][i][0] == FLAG) {
				//右シフト 
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = WIDTH-1; k > 1; k--) {
						map[curPos.room][j][k] = map[curPos.room][j][k-2];
					}
					map[curPos.room][j][0] = UNKNOWN;
					map[curPos.room][j][1] = UNKNOWN;
				}
				curPos.shiftX(true);
				shiftDoorwayX(true);
				break;
			}
			if(map[curPos.room][i][WIDTH-1] == FLAG) {
				//左シフト
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = 0; k < WIDTH-2; k++) {
						map[curPos.room][j][k] = map[curPos.room][j][k+2];
					}
					map[curPos.room][j][WIDTH-1] = UNKNOWN;
					map[curPos.room][j][WIDTH-2] = UNKNOWN;
				}
				curPos.shiftX(false);
				shiftDoorwayX(false);;
				break;
			}
		}
		/* 縦シフト (Y座標の0と末端がUNKOWNの場合)*/
		for(byte i = 0; i < WIDTH; i++) {
			if(map[curPos.room][0][i] == FLAG) {
				//上シフト
				for(byte j = 0; j < WIDTH; j++) {
					for(byte k = HEIGHT-1; k > 1; k--) {
						map[curPos.room][k][j] = map[curPos.room][k-2][j];
					}
					map[curPos.room][0][j] = UNKNOWN;
					map[curPos.room][1][j] = UNKNOWN;
				}
				curPos.shiftY(false);
				shiftDoorwayY(false);
				break;
			}
			if(map[curPos.room][HEIGHT-1][i] == FLAG) {
				//下シフト
				Sound.beep();
				for(byte j = 0; j < WIDTH; j++) {
					for(byte k = 0; k < HEIGHT-2; k++) {
						map[curPos.room][k][j] = map[curPos.room][k+2][j];
					}
					map[curPos.room][HEIGHT-1][j] = UNKNOWN;
					map[curPos.room][HEIGHT-2][j] = UNKNOWN;
				}
				curPos.shiftY(true);
				shiftDoorwayY(true);
				Sound.beep();
				break;
			}
		}
	}
	
	public void setDoorwayExit(byte x, byte y, byte d) {
		doorway_ext_x[curPos.room] = x;
		doorway_ext_y[curPos.room] = y;
		doorway_ext_direc[curPos.room] = d;
	}
	
	public void shiftDoorwayX(boolean whitch) {
		doorway_ent_x[curPos.room] += whitch ? 2 : -2;
		doorway_ext_x[curPos.room] += whitch ? 2 : -2;
	}
	
	public void shiftDoorwayY(boolean whitch) {
		doorway_ent_y[curPos.room] += whitch ? 2 : -2;
		doorway_ext_x[curPos.room] += whitch ? 2 : -2;
	}
	
	/**
	 * 位置情報のリセット(部屋移動時)
	 */
	public void changeNextRoom() {
		setDoorwayExit(curPos.x, curPos.y, curPos.direc);
		curPos.room += 1;
		curPos.x = doorway_ent_x[curPos.room] = curPos.INITIAL_POS_X;
		curPos.y = doorway_ent_y[curPos.room] = curPos.INITIAL_POS_Y;
		curPos.direc = curPos.INITIAL_DIREC;
	}
	
	/**
	 * 位置情報のリセット(部屋帰還時)
	 */
	public void changePrevRoom() {
		curPos.room -= 1;
		curPos.x = doorway_ext_x[curPos.room];
		curPos.y = doorway_ext_y[curPos.room];
		curPos.direc = (byte) ((doorway_ext_direc[curPos.room] + 3) % 4);
	}
	
	/**
	 * 歩数マップのリセット
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
	 * 目的位置までの歩数マップの作成
	 * @param x 目的位置のX座標
	 * @param y　目的位置のY座標
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
		boolean c = false;
		for(byte i = 0; i < HEIGHT; i++) {
			for(byte j = 0; j < WIDTH; j++) {
				if(map[curPos.room][i][j] == FLAG) {
					c = true;
					break;
				}
			}
		}
		if(c == false) {
			makeDistanceMap(doorway_ent_x[curPos.room], doorway_ent_y[curPos.room]);
			return;
		}
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
	    		dataOut.write(doorway_ent_x[i]);
	    		dataOut.write(doorway_ent_y[i]);
	    		dataOut.write(doorway_ext_x[i]);
	    		dataOut.write(doorway_ext_y[i]);
	    		dataOut.write(doorway_ext_direc[i]);
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
	    		dataOut.write(curPos.INITIAL_POS_X);
	    		dataOut.write(curPos.INITIAL_POS_Y);
	    		dataOut.write(curPos.INITIAL_POS_X);
	    		dataOut.write(curPos.INITIAL_POS_Y);
	    		dataOut.write(curPos.INITIAL_DIREC);
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
	    		doorway_ent_x[i] = din.readByte();
	    		doorway_ent_y[i] = din.readByte();
	    		doorway_ext_x[i] = din.readByte();
	    		doorway_ext_y[i] = din.readByte();
	    		doorway_ext_direc[i] = din.readByte();
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
				if(map[curPos.room][i*2+1][j*2] == WALL) {
					g.drawLine(j*10, 63-i*10-1, j*10, 63-i*10-9);
				} 
			}
		}
		/* 横壁の描画 */
		for(byte i = 0; i <= TILE_HEIGHT; i++) {
			for(int j = 0; j < TILE_WIDTH; j++) {
				if(map[curPos.room][i*2][j*2+1] == WALL) {
					g.drawLine(j*10+1, 63-i*10, j*10+9, 63-i*10);
				}
			}
		}
		/* タイル描画 */
		for(byte i = 0; i < TILE_HEIGHT; i++) {
		    for(byte j = 0; j < TILE_WIDTH; j++) {
		    	if(map[curPos.room][i*2+1][j*2+1] == WALL) {
		    		/* 黒タイルの描画 */
		    		for(byte k = 2; k <= 8; k ++) {
		    			g.drawLine(j * 10 + 2, 63-(i * 10 + k), j * 10 + 8, 63-(i * 10 + k));
		    		}
		    	} else if(map[curPos.room][i*2+1][j*2+1] == UNKNOWN && !(j*2+1 == curPos.x && i*2+1 == curPos.y)) {
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