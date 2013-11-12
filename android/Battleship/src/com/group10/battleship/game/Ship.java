package com.group10.battleship.game;

import android.content.Context;

import com.group10.battleship.R;
import com.group10.battleship.graphics.TexturedRect;
import com.group10.battleship.model.Board;

public class Ship extends TexturedRect{
	
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
