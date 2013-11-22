package com.group10.battleship.game.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.util.Log;

import com.group10.battleship.model.Board;
import com.group10.battleship.model.Board.BoardCoord;
import com.group10.battleship.model.Ship;

/**
 * Designed to be the easiest AI, which picks completely random moves.
 *
 */
public class RandomAI implements BattleshipAI{

	// Keep track of tiles we still have to chose from
	private ArrayList<BoardCoord> mTiles;
	private Random mRand;
	
	public RandomAI() {
		mTiles = new ArrayList<BoardCoord>(Board.BOARD_SIZE * Board.BOARD_SIZE);
		for (int i = 0; i < Board.BOARD_SIZE; i++) {
			for (int j = 0; j < Board.BOARD_SIZE; j++) {
				mTiles.add(new BoardCoord(i, j));
			}
		}
		mRand = new Random();
		Log.d("test", "creating random AI. has "+mTiles.size()+" tiles");
	}
	
	@Override
	public BoardCoord getNextMove() {
		Log.d("test", "getting next move ("+mTiles.size()+" possible left)");
		if(mTiles.size() == 0) return null;
		
		int randint = mRand.nextInt(mTiles.size());
		BoardCoord bc = mTiles.get(randint);
		mTiles.remove(randint);
		return bc;
	}
	
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
	public void respondToLastMove(boolean hit, Ship sunk) {
		// We don't care about the result here
	}
	
	@Override
	public void setDifficulty(int diff) {
		// We don't care about the result here
	}
}
