package com.group10.battleship.game.ai;

import com.group10.battleship.model.Board;
import com.group10.battleship.model.Board.BoardCoord;
import com.group10.battleship.model.Ship;

public interface BattleshipAI {
	
	/**
	 * Performs a new move for a 10x10 board.
	 * The move can be a repetition of a previously made move
	 * (the method should be called repeatedly until a valid output is given)
	 * @return the coordinate for the move or null if the AI has no more possible moves it knows to make.
	 */
	public BoardCoord getNextMove();
	
	/**
	 * Called after performMove to respond to the move.
	 * Does nothing if called more than once per move, or before first performMove.
	 * @param hit
	 * @param sunk Only given when the ship is sunk. Otherwise, is null.
	 */
	public void respondToLastMove(boolean hit, Ship sunk);
	
	/**
	 * Moves the ships on myBoard into a valid configuraton.
	 * @param myBoard
	 */
	public void arrangeShips(Board myBoard);
	
	/**
	 * Sets the difficult of the ai
	 * @param difficulty 1 for easy, 2 for normal, 3 for hard
	 */
	public void setDifficulty(int difficulty);
}
