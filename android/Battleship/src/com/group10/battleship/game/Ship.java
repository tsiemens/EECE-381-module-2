package com.group10.battleship.game;

import android.content.Context;
import android.graphics.Color;

import com.group10.battleship.R;
import com.group10.battleship.graphics.TexturedRect;
import com.group10.battleship.model.Board;

public class Ship extends TexturedRect{
	
	public static int SELECTED_COLOR = Color.parseColor("#ff00ffff");
	public static int SHIP_COLOR = Color.parseColor("#ff999999");
	
	private ShipType mType;
	
	private boolean mIsHorizontal;
	// In format of { Col, Row }
	private int[] mGridIndex;
	// In format of { x, y }
	private float[] mBoardLoc;
	private float mGridSize;

	public Ship(Context activityContext, ShipType type) {
		super(activityContext, type.drawableResId());
		mType = type;
		// Place the ship at board 0, 0 to start
		mGridIndex = new int[] {0, 0};
		mBoardLoc = new float[] {0, 0};
		mIsHorizontal = true;
		setSelected(false);
	}

	public void configureBoardConstraints(Board board) {
		float[] bpos = board.getPosition();
		mBoardLoc[0] = bpos[0] + board.getTileOffset();
		mBoardLoc[1] = bpos[1] - board.getTileOffset();
		mGridSize = board.getTileGridSize();
		// Place the ship at board 0, 0 to start
		setPosIndex(mGridIndex[0], mGridIndex[1]);
		setHorizontal(mIsHorizontal);
	}
	
	public void setHorizontal(boolean horiz) {
		mIsHorizontal = horiz;
		if (mIsHorizontal) {
			setSize(mType.size() * mGridSize, mGridSize);
			setTexRotation(0);
		} else {
			setSize(mGridSize, mType.size() * mGridSize);
			setTexRotation(270);
		}
	}
	
	public boolean isHorizontal() { return mIsHorizontal; }
	
	public void setSelected(boolean isSelected) {
		if (isSelected)
			setColor(SELECTED_COLOR);
		else
			setColor(SHIP_COLOR);
	}
	
	public boolean isSelected() {
		if (getColor() == SELECTED_COLOR)
			return true;
		else
			return false;
	}
	
	public boolean isOnGridTile(int x, int y) {
		if (mIsHorizontal && mGridIndex[1] == y) {
			return (x >= mGridIndex[0] && x < (mGridIndex[0] + mType.size()) );
		} else if (!mIsHorizontal && mGridIndex[0] == x) {
			return (y >= mGridIndex[1] && y < (mGridIndex[1] + mType.size()) );
		}
		return false;
	}
	
	public boolean wouldIntersectShipAtPos(int x, int y, Ship ship) {
		for (int i = 0; i < mType.size(); i++) {
			if (ship.isOnGridTile(x, y)) {
				return true;
			}
			
			if (mIsHorizontal) x++;
			else y++;
		}
		return false;
	}
	
	public boolean wouldIntersectShipAfterRotate(Ship ship) {
		int x = mGridIndex[0];
		int y = mGridIndex[1];
		for (int i = 0; i < mType.size(); i++) {
			if (ship.isOnGridTile(x, y)) {
				return true;
			}
			
			if (mIsHorizontal) y++;
			else x++;
		}
		return false;
	}
	
	public boolean wouldBeOnGridAtPos(int x, int y) {
		if (x >= Board.BOARD_SIZE || x < 0 || y >= Board.BOARD_SIZE || y < 0)
			return false;
		if (mIsHorizontal && (x + mType.size()) <= Board.BOARD_SIZE)
			return true;
		else if (!mIsHorizontal && (y + mType.size()) <= Board.BOARD_SIZE)
			return true;
		else
			return false;
	}
	
	public boolean wouldBeOnGridAfterRotate(){
		if (mGridIndex[0] >= Board.BOARD_SIZE || mGridIndex[0] < 0 
			|| mGridIndex[1] >= Board.BOARD_SIZE || mGridIndex[1] < 0)
			return false;
		if (mIsHorizontal && (mGridIndex[1] + mType.size()) <= Board.BOARD_SIZE)
			return true;
		else if (!mIsHorizontal && (mGridIndex[0] + mType.size()) <= Board.BOARD_SIZE)
			return true;
		else
			return false;
	}
	
	public void setPosIndex(int x, int y) {
		mGridIndex[0] = x;
		mGridIndex[1] = y;
		setPosition(mBoardLoc[0] + (x * mGridSize), mBoardLoc[1] - (y * mGridSize));
	}
	
	public int[] getPosIndex() { return mGridIndex.clone(); }
	
	public ShipType getType() { return mType;}
	
	public static enum ShipType {
		CARRIER, BATTLESHIP, DESTROYER,
		SUB, PATROL;
		
		public int size() {
			switch (this) {
			case CARRIER:
				return 5;
			case BATTLESHIP:
				return 4;
			case DESTROYER:
				return 3;
			case SUB:
				return 3;
			case PATROL:
				return 2;
			default:
				return 0;
			}
		}
		
		public int drawableResId() {
			switch (this) {
			case CARRIER:
				return R.drawable.carrier;
			case BATTLESHIP:
				return R.drawable.battleship;
			case DESTROYER:
				return R.drawable.destroyer;
			case SUB:
				return R.drawable.sub;
			case PATROL:
				return R.drawable.patrol;
			default:
				return 0;
			}
		}
	}
}
