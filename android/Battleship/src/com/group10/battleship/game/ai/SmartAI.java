package com.group10.battleship.game.ai;

import java.util.ArrayList;
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
	private List<Integer> mNextX;
	private List<Integer> mNextY;
	private int mDifficulty;
	
	public SmartAI() {
		mBoardStates = new TileState[10][10];
		mBoardHeuristics = new int[10][10];
		mNextX = new ArrayList<Integer>();
		mNextY = new ArrayList<Integer>();
		mRand = new Random();
		mDifficulty = 3;
		
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 10; y++) {
				mBoardStates[x][y] = TileState.EMPTY;
			}
		}
	}
	
	@Override
	public BoardCoord getNextMove() {
		int maxH = calculateHeuristics();
		assignPossibleMoves(maxH);
		
		int randIndex = Math.abs(mRand.nextInt())%mNextX.size();
		
		int x = mNextX.get(randIndex);
		int y = mNextY.get(randIndex);
		mNextY.clear();
		mNextX.clear();
		
		BoardCoord coord = new BoardCoord(x, y);
		mLastX = x;
		mLastY = y;
		return coord;
	}

	@Override
	public void respondToLastMove(boolean hit, Ship sunk) {
		if (sunk != null) {
			if (sunk.isSunk()) {
				BoardCoord[] coords = sunk.getShipCoords();
				for (int i = 0; i < sunk.getShipCoords().length; i++) {
					this.mBoardStates[coords[i].x][coords[i].y] = TileState.SUNK;
				}
			}
		} else if (hit) {
			this.mBoardStates[mLastX][mLastY] = TileState.HIT;
		} else {
			this.mBoardStates[mLastX][mLastY] = TileState.MISS;
		}
	}

	private int calculateHeuristics() {
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
								mBoardHeuristics[x][y] += (25 - i*4);
						} else mBoardHeuristics[x][y] += 3 - i;
						
						if (rightNeighbor < 10) {
							if (mBoardStates[rightNeighbor][y] == TileState.EMPTY)
								mBoardHeuristics[x][y] += 5 - i;
							else if (mBoardStates[rightNeighbor][y] == TileState.HIT)
								mBoardHeuristics[x][y] += (25 - i*4);
						} else mBoardHeuristics[x][y] += 3 - i;
						
						if (topNeighbor >= 0) {
							if (mBoardStates[x][topNeighbor] == TileState.EMPTY)
								mBoardHeuristics[x][y] += 5 - i;
							else if (mBoardStates[x][topNeighbor] == TileState.HIT)
								mBoardHeuristics[x][y] += (25 - i*4);
						} else mBoardHeuristics[x][y] += 3 - i;
						
						if (bottomNeighbor < 10) {
							if (mBoardStates[x][bottomNeighbor] == TileState.EMPTY)
								mBoardHeuristics[x][y] += 5 - i;
							else if (mBoardStates[x][bottomNeighbor] == TileState.HIT)
								mBoardHeuristics[x][y] += (25 - i*4);
						} else mBoardHeuristics[x][y] += 3 - i;
						
						if (mBoardHeuristics[x][y] > maxH) {
							maxH = mBoardHeuristics[x][y];
						}
					}
				}
			}
		}
		return maxH;
	}
	
	private void assignPossibleMoves(int maxH) {
		for(int x = 0; x < 10; x++) {
			for(int y = 0; y < 10; y++) {
				if (mBoardHeuristics[x][y] >= (maxH - mDifficulty)) {
					mNextX.add(x);
					mNextY.add(y);
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
	
	@Override
	public void setDifficulty(int diff) {
		this.mDifficulty = diff;
	}
}
