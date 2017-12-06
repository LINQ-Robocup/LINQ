package com.linq;

import java.util.Queue;

import lejos.nxt.*;

import javax.microedition.lcdui.Graphics;

public class Map {
  /* 定数宣言 */
	//　部屋の数(坂で接続された部屋の最大数)
	static final byte ROOM 	= 2;
	// 1部屋のサイズ(縦・横)
	static final byte HEIGHT = 5 * 2 + 1;
	static final byte WIDTH  = 9 * 2 + 1;
	// マップ値
	public static final byte WALL 	= 1;
	public static final byte PASS 	= 2;
	public static final byte FLAG 	= 99;
	public static final byte UNKNOWN = 0;
	//方向
	static final byte NORTH = 0;
	static final byte EAST  = 1;
	static final byte SOUTH = 2;
	static final byte WEST  = 3;
	static final byte X_D[] = {0, 1, 0, -1};
	static final byte Y_D[] = {1, 0, -1, 0};
	//位置情報
	final static byte INITIAL_X = 1;
	final static byte INITIAL_Y = 1;
	final static byte INITIAL_DIREC = 0;
  /* 変数宣言 */
	//マップ情報
	private byte[][][] map = new byte[ROOM][HEIGHT][WIDTH];

	// 現在位置(XY座標, 方向, 部屋)
	private byte x, y, direc, room;
	
	private class Coord {
		byte x, y;
		Coord(byte x, byte y) {
			this.x = x;
			this.y = y;
		}
	}
	
	Coord[] ent = new Coord[ROOM];
	Coord[] ext = new Coord[ROOM];
	
	/**
	 * マップ情報の初期化
	 * 迷路生成(デバック用)
	 */
	Map() {
		/*　マップ情報を初期化 */
		for(byte i = 0; i < ROOM; i++) {
			ent[i] = new Coord(INITIAL_X, INITIAL_Y);
			ext[i] = new Coord(INITIAL_X, INITIAL_Y);
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
		x = INITIAL_X;
		y = INITIAL_Y;
		direc = INITIAL_DIREC;
		room = 0;
	}
	
	/**
	 * 方向転換時における方向情報の修正
	 * @param Clockwise 回転方向) true:右回転, false:左回転 
	 */
	public void changeDirec(boolean Clockwise) {
		direc += (Clockwise) ? 1 : 3;
		direc %= 4;
	}
	
	/**
	 * タイル間移動(現在位置の更新)
	 */
	public void moveTile() {
		switch(direc) {
			case NORTH: this.y += 2; break;
			case EAST:	this.x += 2; break;
			case SOUTH: this.y -= 2; break;
			case WEST:	this.x -= 2; break;
			default:
		}
	}
	
	/**
	 * 位置情報を初期化
	 */
	public void resetPosition() {
		this.x = INITIAL_X;
		this.y = INITIAL_Y;
		this.direc = INITIAL_DIREC;
		map[this.room][this.x][this.y] = PASS;
	}
		
	/**
	 * 現在位置の情報を取得
	 * @return 現在座標の値
	 */
	void setCurPosInfo(byte info) {
		map[this.room][this.y][this.x] = info;
	}

	/**
	 * 前方の壁情報の取得
	 * @return　前方座標の値
	 */
	byte getPathFront() {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH: y += 1; break;
			case EAST:	x += 1; break;
			case SOUTH: y -= 1; break;
			case WEST:	x -= 1; break;
			default:
		}
		return map[this.room][y][x];
	}
	
	/**
	 * 右側の壁情報の取得
	 * @return 右側座標の値
	 */
	byte getPathRight() {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH:	x += 1; break;
			case EAST:	y -= 1; break;
			case SOUTH:	x -= 1; break;
			case WEST:	y += 1; break;
			default:
		}
		return map[this.room][y][x];
	}
	
	/**
	 * 左側の壁情報の取得
	 * @return　左側座標の取得
	 */
	byte getPathLeft() {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH:	x -= 1; break;
			case EAST:	y += 1; break;
			case SOUTH:	x += 1; break;
			case WEST:	y -= 1; break;
			default:
		}
		return map[this.room][y][x];
	}
	
	boolean isRightWall() {
		return (getPathRight() == WALL);
	}

	boolean isFrontWall() {
		return (getPathFront() == WALL);
	}
	
	boolean isLeftWall() {
		return (getPathLeft() == WALL);
	}
	
	/**
	 * 後方の壁情報の取得
	 * @return 後方座標の値
	 */
	byte getPathBack() {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH:	y -= 1; break;
			case EAST:	x -= 1; break;
			case SOUTH:	y += 1; break;
			case WEST:	x += 1; break;
			default:
		}
		return map[this.room][y][x];
	}
	
	byte getTile() {
		return map[room][y][x];
	}
		
	boolean isTilePassed() {
		return (getTile() > 0);
	}
	
	/**
	 * 前方の壁情報の入力
	 * @param info　
	 */
	void setPathFront(byte info) {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH: y += 1; break;
			case EAST:	x += 1; break;
			case SOUTH:	y -= 1; break;
			case WEST:	x -= 1; break;
			default:
		}
		map[room][y][x] = info;
	}
	
	/**
	 * 右側の壁情報の取得
	 * @param info
	 */
	void setPathRight(byte info) {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH:	x += 1; break;
			case EAST:	y -= 1; break;
			case SOUTH:	x -= 1; break;
			case WEST:	y += 1; break;
			default:
		}
		map[room][y][x] = info;
	}
	
	/**
	 * 左側の壁情報の取得
	 * @param info
	 */
	void setPathLeft(byte info) {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH: x -= 1; break;
			case EAST:	y += 1; break;
			case SOUTH:	x += 1; break;
			case WEST:	y -= 1; break;
			default:
		}
		map[room][y][x] = info;
	}
	
	/**
	 * 後方の壁情報の取得
	 * @param info
	 */
	void setPathBack(byte info) {
		byte x = this.x;
		byte y = this.y;
		switch(this.direc) {
			case NORTH:	y -= 1; break;
			case EAST:	x -= 1; break;
			case SOUTH:	y += 1; break;
			case WEST:	x += 1; break;
			default:
		}
		map[room][y][x] = info;
	}
	
	/**
	 * 前方のタイル座標を黒タイルに設定
	 */
	void setFrontBlack() {
		byte x = this.x, y = this.y;
		switch (this.direc) {
			case NORTH:	y += 2; break;
			case EAST:	x += 2; break;
			case SOUTH:	y -= 2; break;
			case WEST:	x -= 2; break;
			default:
		}
		map[room][y][x] = WALL;
		map[room][y+1][x] = WALL;
		map[room][y][x+1] = WALL;
		map[room][y-1][x] = WALL;
		map[room][y][x-1] = WALL;
	}
	
	void setTilePass() {
		map[room][y][x] = PASS;
	}
	
	/**
	 * 自己位置に合わせたマップの整形(配列外参照の防止)
	 */
	public void arrangeMap() {
		/* 横シフト　(X座標の0と末端がUNKOWNの場合) */
		for(byte i = 0; i < HEIGHT; i++) {
			if(this.map[room][i][0] == FLAG) { //右シフト 
				for(byte j = 0; j < HEIGHT; j++) {
					for(byte k = WIDTH-1; k > 1; k--) 
						map[room][j][k] = map[room][j][k-2];
					map[room][j][0] = UNKNOWN;
					map[room][j][1] = UNKNOWN;
				}
				x += 2;
				ent[room].x += 2;
				ext[room].x += 2;
				break;
			}
		}
		/* 縦シフト (Y座標の0と末端がUNKOWNの場合)*/
		for(byte i = 0; i < WIDTH; i++) {
			if(map[room][0][i] == FLAG) {
				//上シフト
				for(byte j = 0; j < WIDTH; j++) {
					for(byte k = HEIGHT-1; k > 1; k--)
						map[room][k][j] = map[room][k-2][j];
					map[room][0][j] = UNKNOWN;
					map[room][1][j] = UNKNOWN;
				}
				y += 2;
				ent[room].y += 2;
				ext[room].y += 2;
				break;
			}
		}
	}
	
	void resetDistanceMap() {
		for(byte i = 0; i < HEIGHT; i++) {
			for(byte j = 0; j < WIDTH; j++) {
				if(map[room][i][j] > PASS && map[room][i][j] < FLAG)
					map[room][i][j] = PASS;
			}
		}
	}
	
	void makeDistanceMap(byte x, byte y) {
		Queue<Byte> que = new Queue<Byte>();
		que.push(x);
		que.push(y);
		map[room][y][x] = FLAG;
		do {
			x = (Byte) que.pop();
			y = (Byte) que.pop();
			for (byte i = 0; i < 4; i++) {
				if(map[room][y+Y_D[i]][x+X_D[i]] == PASS) {
					que.push((byte)(x+X_D[i]));
					que.push((byte)(y+Y_D[i]));
					map[room][y+Y_D[i]][x+X_D[i]] = (byte) (map[room][y][x] - 1);
				}
			}	
		} while (!que.isEmpty());
	}
	
	void searchFlag() {
		Queue<Byte> que = new Queue<Byte>();
		byte x, y;
		boolean flag = false;
		for (byte i = 0; i < HEIGHT; i++) {
			for (byte j = 0; j < WIDTH; j++) {
				if (map[room][i][j] == FLAG) {
					flag = true;
					break;
				}
			}
			if (flag) break;
		}
		resetDistanceMap();
		if (!flag) {
			makeDistanceMap(ent[room].x, ent[room].y);
			return;
		}
		que.push(this.x);
		que.push(this.y);
		while (!que.isEmpty()) {
			x = (Byte) que.pop();
			y = (Byte) que.pop();
			for(byte i = 0; i < 4; i ++) {
				if(map[room][y+Y_D[i]][x+X_D[i]] == PASS) {
					que.push((byte)(x+X_D[i]));
					que.push((byte)(y+Y_D[i]));
				} else if(map[room][y+Y_D[i]][x+X_D[i]] == FLAG) {
					que.clear();
					makeDistanceMap((byte)(x+X_D[i]), (byte)(y+Y_D[i]));
					return;
				}
			}
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
				if(map[room][i*2+1][j*2] == WALL) {
					g.drawLine(j*10, 63-i*10-1, j*10, 63-i*10-9);
				} else if(map[room][i*2+1][j*2] == FLAG) {
					for(int l = 63-i*10-9; l <= 63-i*10-1; l += 2) {
						g.drawLine(j*10, l, j*10, l);
					}
				}
			}
		}
		/* 横壁の描画 */
		for(byte i = 0; i <= TILE_HEIGHT; i++) {
			for(int j = 0; j < TILE_WIDTH; j++) {
				if(map[room][i*2][j*2+1] == WALL) {
					g.drawLine(j*10+1, 63-i*10, j*10+9, 63-i*10);
				} else if(map[room][i*2][j*2+1] == FLAG) {
					for(int l = j*10+1; l <= j*10+9; l += 2) {
						g.drawLine(l, 63-i*10, l, 63-i*10);
					}
				}
			}
		}
		/* タイル描画 */
		for(byte i = 0; i < TILE_HEIGHT; i++) {
		    for(byte j = 0; j < TILE_WIDTH; j++) {
		    	if(map[room][i*2+1][j*2+1] == WALL) {
		    		/* 黒タイルの描画 */
		    		for(byte k = 2; k <= 8; k ++)
		    			g.drawLine(j * 10 + 2, 63-(i * 10 + k), j * 10 + 8, 63-(i * 10 + k));
		    	} else if(map[room][i*2+1][j*2+1] == UNKNOWN && !(j*2+1 == this.x && i*2+1 == this.y)) {
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
		String posInfo = "X:" + this.x + " Y:" + this.y + " D :" + this.direc;
		String refInfo = "F:" + getPathFront() + 
						" B:" + getPathBack() + 
						" R:" + getPathRight() +
						" L:" + getPathLeft();
		LCD.drawString(posInfo, 0, 0);
		LCD.drawString(refInfo, 0, 1);
		final byte x = (byte) (this.x - (this.x / 2) - 1);
		final byte y = (byte) (this.y - (this.y / 2) - 1);
		switch(this.direc) {
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