package com.linq;

import lejos.nxt.Sound;


public class Main {
	enum Direc{RIGHT, LEFT, FRONT, BACK;}
	public static void main(String[] args) {
		/* インスタンス生成・初期化 */
		MotionToMap map = new MotionToMap();
		map.dispMapInfo();
		map.waitForButtonPress(0);
		
		/* 迷路探索 */
		map.setPathBack(Map.WALL);
		while(true) {
			if (!map.isTilePassed()) {
				// 壁情報の取得(新規)
				map.updateRealWallInfo();
				if (map.getPathRight() == Map.UNKNOWN)
					map.setPathRight(map.real.isWallRight() ? Map.WALL : Map.FLAG);
				if (map.getPathFront() == Map.UNKNOWN)
					map.setPathFront(map.real.isWallFront() ? Map.WALL : Map.FLAG);
				if (map.getPathLeft() == Map.UNKNOWN) 
					map.setPathLeft(map.real.isWallLeft() ? Map.WALL : Map.FLAG);
				// マップの整形
				map.arrangeMap();
				map.dispMapInfo();
			}
			
			// 進行方向の決定
			Direc direc;
			while (true) {
				direc = Direc.BACK;
				byte path = map.getPathBack();
				if(map.getPathLeft() >= path) {
					path = map.getPathLeft();
					direc = Direc.LEFT;
				}
				if(map.getPathFront() >= path) {
					path = map.getPathFront();
					direc = Direc.FRONT;
				}
				if(map.getPathRight() >= path) {
					path = map.getPathRight();
					direc = Direc.RIGHT;
				}
				// 全て通過済みの場合(最短のFLAGまでの距離を配列に代入)
				if (path <= Map.PASS) {
					Sound.buzz();
					map.searchFlag();
					map.dispMapInfo();
				} else {
					if (path == Map.FLAG)
						map.resetDistanceMap();
					break;
				}
			}
			
			//方向転換
			switch (direc) {
				case RIGHT: map.turnRight(); break;
				case LEFT : map.turnLeft(); break;
				case BACK : map.turn(); break;
				default: break;
			}

			//タイル移動
			if (!map.isFrontWall()) {
				map.setTilePass();
				map.move();
				if (map.getTile() == Map.FLAG)
					break;
				map.setPathBack(Map.PASS);
				map.dispMapInfo();
				map.waitForButtonPress(3);
			}
		}
	}
}