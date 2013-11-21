package com.group10.battleship.game.ai;

import java.util.List;
import java.util.Random;

import com.group10.battleship.model.Board;
import com.group10.battleship.model.Ship;
import com.group10.battleship.model.Board.BoardCoord;

public class SmartAI implements BattleshipAI {
	private enum TileState {EMPTY, HIT, MISS, SUNK};
	private TileState[][] mBoardStates;
	private int[][] mBoardHeuristics;
	private Random mRand;
	private int mLastX;
	private int mLastY;
	private int mNextX;
	private int mNextY;
	
	public SmartAI() {
		mBoardStates = new TileState[10][10];
		mBoardHeuristics = new int[10][10];
		mRand = new Random();
		
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 10; y++) {
				mBoardStates[x][y] = TileState.EMPTY;
			}
		}
	}
	
	@Override
	public BoardCoord getNextMove() {
		calculateHeuristics();
		BoardCoord coord = new BoardCoord(mNextX, mNextY);
		mLastX = mNextX;
		mLastY = mNextY;
		return coord;
	}

	@Override
	public void respondToLastMove(boolean hit, Ship sunk) {
		if (sunk != null && sunk.isSunk()) {
			BoardCoord[] coords = sunk.getShipCoords();
			for (int i = 0; i < sunk.getShipCoords().length; i++) {
				this.mBoardStates[coords[i].x][coords[i].y] = TileState.SUNK;
			}
		} else if (hit) {
			this.mBoardStates[mLastX][mLastY] = TileState.HIT;
		} else {
			this.mBoardStates[mLastX][mLastY] = TileState.MISS;
		}
	}

	private void calculateHeuristics() {
		int maxH = 0;
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 10; y++) {
				mBoardHeuristics[x][y] = 0;
				
				if (mBoardStates[x][y] == TileState.EMPTY) { 
					for (int i = 1; i <= 4; i++) {
						int leftNeighbor = x - i;
						int rightNeighbor = x + i;
						int topNeighbor = y - i;
						int bottomNeighbor = y + i;
						
						if (leftNeighbor >= 0) {
							if (mBoardStates[leftNeighbor][y] == TileState.EMPTY)
								mBoardHeuristics[x][y] += 5 - i;
							else if (mBoardStates[leftNeighbor][y] == TileState.HIT)
								mBoardHeuristics[x][y] += (20 - i*4);
						} else mBoardHeuristics[x][y] += 5 - i;
						
						if (rightNeighbor < 10) {
							if (mBoardStates[rightNeighbor][y] == TileState.EMPTY)
								mBoardHeuristics[x][y] += 5 - i;
							else if (mBoardStates[rightNeighbor][y] == TileState.HIT)
								mBoardHeuristics[x][y] += (20 - i*4);
						} else mBoardHeuristics[x][y] += 5 - i;
						
						if (topNeighbor >= 0) {
							if (mBoardStates[x][topNeighbor] == TileState.EMPTY)
								mBoardHeuristics[x][y] += 5 - i;
							else if (mBoardStates[x][topNeighbor] == TileState.HIT)
								mBoardHeuristics[x][y] += (20 - i*4);
						} else mBoardHeuristics[x][y] += 5 - i;
						
						if (bottomNeighbor < 10) {
							if (mBoardStates[x][bottomNeighbor] == TileState.EMPTY)
								mBoardHeuristics[x][y] += 5 - i;
							else if (mBoardStates[x][bottomNeighbor] == TileState.HIT)
								mBoardHeuristics[x][y] += (20 - i*4);
						} else mBoardHeuristics[x][y] += 5 - i;
						
						if (mBoardHeuristics[x][y] > maxH) {
							maxH = mBoardHeuristics[x][y];
							mNextX = x;
							mNextY = y;
						}
					}
				}
			}
		}
	}
	
	/**
	 * Taken from Trevor's RandomAI
	 */
	@Override
	public void arrangeShips(Board myBoard) {
		List<Ship> ships = myBoard.getShips();
		// Get the ships off the board
		for (Ship ship : ships) {
			ship.setHorizontal(true);
			ship.setPosIndex(-5, -5);
		}
		
		int newX;
		int newY;
		// Randomly place each ship
		for (Ship ship : ships) {
			do {
				ship.setHorizontal(mRand.nextBoolean());
				newX = mRand.nextInt(Board.BOARD_SIZE);
				newY = mRand.nextInt(Board.BOARD_SIZE);
			} while (!myBoard.verifyNewShipPos(newX, newY, ship));
			ship.setPosIndex(newX, newY);
		}
	}
}
