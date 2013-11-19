package com.group10.battleship.model;

import android.content.Context;
import android.graphics.Color;

import com.group10.battleship.R;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.TexturedRect;
import com.group10.battleship.model.Board.BoardCoord;

public class Ship implements GL20Drawable{
	
	public static int SELECTED_COLOR = Color.parseColor("#ff00ffff");
	public static int SHIP_COLOR = Color.parseColor("#ff999999");
	
	private TexturedRect mTexRect;
	
	private ShipType mType;
	
	private boolean mIsHorizontal;
	// In format of { Col, Row }
	private BoardCoord mGridIndex;
	// In format of { x, y }
	private float[] mBoardLoc;
	private float mGridSize;

	/**
	 * Creates new ship of type, with a graphical component.
	 * @param activityContext
	 * @param type
	 */
	public Ship(Context activityContext, ShipType type) {
		this(type);
		initializeGLDrawable(activityContext);
		setSelected(false);
	}
	
	/**
	 * Creates new ship of type. Has no graphical element
	 * @param type
	 */
	public Ship(ShipType type) {
		mType = type;
		// Place the ship at board 0, 0 to start
		mGridIndex = new BoardCoord(0, 0);
		mBoardLoc = new float[] {0, 0};
		mIsHorizontal = true;
	}

	/**
	 * Configures the ship and drawable (if initialized) so it can appear correctly on the board
	 * @param board
	 */
	public void configureBoardConstraints(Board board) {
		float[] bpos = board.getPosition();
		mBoardLoc[0] = bpos[0] + board.getTileOffset();
		mBoardLoc[1] = bpos[1] - board.getTileOffset();
		mGridSize = board.getTileGridSize();
		// Place the ship at board 0, 0 to start
		setPosIndex(mGridIndex.x, mGridIndex.y);
		setHorizontal(mIsHorizontal);
	}
	
	/**
	 * Initializes the drawable component of the ship.
	 * @param context
	 */
	public void initializeGLDrawable(Context context) {
		mTexRect = new TexturedRect(context, mType.drawableResId());
	}
	
	public void setHorizontal(boolean horiz) {
		mIsHorizontal = horiz;
		if (mTexRect != null) {
			if (mIsHorizontal) {
				mTexRect.setSize(mType.size() * mGridSize, mGridSize);
				mTexRect.setTexRotation(0);
			} else {
				mTexRect.setSize(mGridSize, mType.size() * mGridSize);
				mTexRect.setTexRotation(270);
			}
		}
	}
	
	public boolean isHorizontal() { return mIsHorizontal; }
	
	public void setSelected(boolean isSelected) {
		if (mTexRect != null) {
			if (isSelected)
				mTexRect.setColor(SELECTED_COLOR);
			else
				mTexRect.setColor(SHIP_COLOR);
		}
	}
	
	public boolean isSelected() {
		if (mTexRect == null) return false;
		else {
			if (mTexRect.getColor() == SELECTED_COLOR)
				return true;
			else
				return false; 
		}
	}
	
	public boolean isOnGridTile(int x, int y) {
		if (mIsHorizontal && mGridIndex.y == y) {
			return (x >= mGridIndex.x && x < (mGridIndex.x + mType.size()) );
		} else if (!mIsHorizontal && mGridIndex.x == x) {
			return (y >= mGridIndex.y && y < (mGridIndex.y + mType.size()) );
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
		int x = mGridIndex.x;
		int y = mGridIndex.y;
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
		if (mGridIndex.x >= Board.BOARD_SIZE || mGridIndex.x < 0 
			|| mGridIndex.y >= Board.BOARD_SIZE || mGridIndex.y < 0)
			return false;
		if (mIsHorizontal && (mGridIndex.y + mType.size()) <= Board.BOARD_SIZE)
			return true;
		else if (!mIsHorizontal && (mGridIndex.x + mType.size()) <= Board.BOARD_SIZE)
			return true;
		else
			return false;
	}
	
	public void setPosIndex(int x, int y) {
		mGridIndex.x = x;
		mGridIndex.y = y;
		if (mTexRect != null) {
			mTexRect.setPosition(mBoardLoc[0] + (x * mGridSize), mBoardLoc[1] - (y * mGridSize));
		}
	}
	
	public BoardCoord getPosIndex() { return new BoardCoord(mGridIndex.x, mGridIndex.y); }
	
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

	@Override
	public void draw(float[] mvpMatrix) {
		if (mTexRect != null) {
			mTexRect.draw(mvpMatrix);
		}
	}
}
