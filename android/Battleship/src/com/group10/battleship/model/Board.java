package com.group10.battleship.model;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.group10.battleship.R;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.TexturedRect;
import com.group10.battleship.model.Ship.ShipType;

public class Board implements GL20Drawable {

	public static final int TILE_COLOR_NORMAL = Color.parseColor("#aa4285f4");
	public static final int TILE_COLOR_MISS = Color.parseColor("#aaeeeeee");
	public static final int TILE_COLOR_HIT = Color.parseColor("#aadb4437");
	public static final int TILE_COLOR_REVEAL = Color.parseColor("#aaffd700");
	public static final int TILE_COLOR_SUNK = Color.parseColor("#aa545454");
	public static final int TILE_COLOR_SELECTION = Color.parseColor("#ffff7800");

	public static final int BORDER_COLOR_PLAYER = Color.parseColor("#ff6CB34B");
	public static final int BORDER_COLOR_OPPONENT = Color.parseColor("#FFA60000");
	public static final int BOARD_SIZE = 10;

	private Context mContext;
	private TexturedRect mNumberRow;
	private TexturedRect mAlphaCol;

	private ArrayList<ArrayList<TexturedRect>> mTileRows;
	private BoardCoord mSelectedTileIndex;
	private TexturedRect mSelectionTile;

	private boolean mIsPlayerBoard = false;

	private float mTopLeftX;
	private float mTopLeftY;
	private float mSideLength;

	private List<Ship> mShips;
	private List<TexturedRect> mHitTiles;
	private int numHitTiles = 0;

	public Board(Context context, float sideLength, float x, float y,
			boolean isPlayerBoard) {
		mContext = context;
		mTopLeftX = x;
		mTopLeftY = y;
		mSideLength = sideLength;

		mNumberRow = new TexturedRect(mContext, R.drawable.boardtop);
		mNumberRow.setPosition(mTopLeftX, mTopLeftY);
		mNumberRow.setSize(mSideLength, getTileOffset());

		mAlphaCol = new TexturedRect(mContext, R.drawable.boardside);
		mAlphaCol.setPosition(mTopLeftX, mTopLeftY);
		mAlphaCol.setSize(getTileOffset(), mSideLength);

		setIsPlayerBoard(isPlayerBoard);

		mTileRows = new ArrayList<ArrayList<TexturedRect>>(BOARD_SIZE);
		TexturedRect tempTile;
		ArrayList<TexturedRect> tempArray;
		float tileGridSize = getTileGridSize();
		float tilePadding = getTilePadding();
		float tileSize = tileGridSize - (2 * tilePadding);

		// Add each tile
		for (int row = 0; row < BOARD_SIZE; row++) {
			tempArray = new ArrayList<TexturedRect>(BOARD_SIZE);
			mTileRows.add(tempArray);

			for (int col = 0; col < BOARD_SIZE; col++) {
				tempTile = new TexturedRect(mContext, R.drawable.white_pix);
				tempTile.setColor(TILE_COLOR_NORMAL);
				tempTile.setPosition(mTopLeftX + ((col + 1) * tileGridSize)
						+ tilePadding, mTopLeftY
						- (((row + 1) * tileGridSize) + tilePadding));
				tempTile.setSize(tileSize, tileSize);
				tempArray.add(tempTile);
			}
		}

		mSelectedTileIndex = new BoardCoord(-1, -1);
		mSelectionTile = new TexturedRect(mContext, R.drawable.white_pix);
		mSelectionTile.setColor(TILE_COLOR_SELECTION);
		mSelectionTile.setSize(tileGridSize, tileGridSize);

		if (mIsPlayerBoard) {
			// Initialize the player's ships
			mShips = new ArrayList<Ship>(5);
			Ship shipTemp = new Ship(mContext, ShipType.CARRIER);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.BATTLESHIP);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.DESTROYER);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.SUB);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.PATROL);
			mShips.add(shipTemp);

			setShips(mShips);
		} else {
			mShips = new ArrayList<Ship>(5);
			Ship shipTemp = new Ship(mContext, ShipType.CARRIER);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.BATTLESHIP);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.DESTROYER);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.SUB);
			mShips.add(shipTemp);
			shipTemp = new Ship(mContext, ShipType.PATROL);
			mShips.add(shipTemp);

		}

		mHitTiles = makeHitTiles();
	}

	public List<TexturedRect> makeHitTiles() {
		float tileSize = getTileGridSize() - (2 * getTilePadding());
		List<TexturedRect> temp = new ArrayList<TexturedRect>(17);
		for (int i = 0; i < 17; i++) {
			TexturedRect hitTile = new TexturedRect(mContext, R.drawable.hit);
			hitTile.setSize(tileSize, tileSize);
			temp.add(hitTile);
		}
		return temp;
	}

	public void setHitTileList(List<TexturedRect> list) {
		mHitTiles = list;
	}

	public List<TexturedRect> getHitTileList() {
		return mHitTiles;
	}

	public void setShip(int xPos, int yPos, boolean horiz, ShipType type) {

		for (int i = 0; i < mShips.size(); i++) {
			if (mShips.get(i).getType() == type) {
				mShips.get(i).setPosIndex(xPos, yPos);
				mShips.get(i).setHorizontal(horiz);
			}
		}
	}

	/**
	 * Copies the tile state of board to this. Does not affect the rendering
	 * parameters, such as width, height, position, etc.
	 * 
	 * @param board
	 */
	public void copyState(Board board) {
		for (int row = 0; row < Board.BOARD_SIZE; row++) {
			for (int col = 0; col < Board.BOARD_SIZE; col++) {
				this.setTileColour(board.getTileColour(col, row), col, row);
			}
		}
		this.setIsPlayerBoard(board.isPlayerBoard());

		BoardCoord sel = board.getSelectedTileIndex();
		if (sel != null) {
			setSelectedTile(sel);
		}

		List<Ship> ships = board.getShips();
		if (ships != null && mShips != null) {
			for (Ship s : ships) {
				for (Ship myShip : mShips) {
					if (myShip.getType().equals(s.getType())) {
						myShip.setHorizontal(s.isHorizontal());
						BoardCoord pos = s.getPosIndex();
						myShip.setPosIndex(pos.x, pos.y);
						myShip.setSelected(s.isSelected());
						break;
					}
				}
			}
		}

		List<TexturedRect> hitTiles = board.getHitTiles();
		if (hitTiles != null && mHitTiles != null) {
			hitTiles = makeHitTiles();
			numHitTiles = board.numHitTiles;
			for (int i = 0; i < numHitTiles; i++) {
				hitTiles.get(i).setPosition(
						board.getHitTileList().get(i).getXPos(),
						board.getHitTileList().get(i).getYPos());
			}

			mHitTiles = hitTiles;
		}
	}

	@Override
	public void draw(float[] mvpMatrix) {
		mNumberRow.draw(mvpMatrix);
		mAlphaCol.draw(mvpMatrix);

		if (isTileIndexValid(mSelectedTileIndex.x, mSelectedTileIndex.y)) {
			mSelectionTile.draw(mvpMatrix);
		}

		for (ArrayList<TexturedRect> al : mTileRows) {
			for (TexturedRect t : al) {
				t.draw(mvpMatrix);
			}
		}
		if (isPlayerBoard() && mShips != null) {
			for (Ship s : mShips) {
				s.draw(mvpMatrix);
			}
		}
		if (mHitTiles != null) {
			for (int i = 0; i < numHitTiles; i++) {
				mHitTiles.get(i).draw(mvpMatrix);
			}
		}
	}

	private void setIsPlayerBoard(boolean isPlayerBoard) {
		mIsPlayerBoard = isPlayerBoard;
		if (mIsPlayerBoard) {
			mNumberRow.setColor(BORDER_COLOR_PLAYER);
			mAlphaCol.setColor(BORDER_COLOR_PLAYER);
		} else {
			mNumberRow.setColor(BORDER_COLOR_OPPONENT);
			mAlphaCol.setColor(BORDER_COLOR_OPPONENT);
		}
	}

	/**
	 * Determines if there is a ship at the tile location & sets the tile to
	 * show either hit/miss
	 * 
	 * @param col
	 *            , row
	 */
	public boolean playerShotAttempt(int x, int y) {
		// notify player if already fired

		Ship ship = getShipAtIndex(x, y);

		if (ship != null) {
			if (ship.addHit()) {
				sinkShipAt(x, y);
			} else {
				setTileColour(TILE_COLOR_HIT, x, y);
				setHitTile(x, y);
			}
			return true;
		} else {
			setTileColour(TILE_COLOR_MISS, x, y);
			return false;
		}
	}

	/**
	 * Used to sink ship at a given location by changing the tile colors.
	 * 
	 * @param x
	 *            coordinate of shot
	 * @param y
	 *            coordinate of shot
	 * @return false if there is no ship at the coordinate given.
	 */
	public boolean sinkShipAt(int x, int y) {
		setHitTile(x, y);
		Ship ship = getShipAtIndex(x, y);

		if (ship != null) {
			BoardCoord[] coords = ship.getShipCoords();

			for (int i = 0; i < coords.length; i++) {
				setTileColour(TILE_COLOR_SUNK, coords[i].x, coords[i].y);
			}

			return true;
		} else {
			return false;
		}
	}
	
	public void revealShips() {
		if (mShips != null && mShips.size() > 0) {
			for (Ship ship : mShips) {
			
				BoardCoord[] coords = ship.getShipCoords();
			
				for( int i = 0; i < coords.length; i++) {
					if (mTileRows.get(coords[i].y).get(coords[i].x).getColor() == TILE_COLOR_NORMAL)
						setTileColour(TILE_COLOR_REVEAL, coords[i].x, coords[i].y);
				} 
			}
		}
	}
	
	public boolean isPlayerBoard(){
		return mIsPlayerBoard;
	}

	public boolean isAllSunk() {
		for (int i = 0; i < mShips.size(); i++) {
			if (mShips.get(i).isSunk() == false)
				return false;
		}

		return true;
	}

	public List<Ship> getShips() {
		return mShips;
	}

	public List<TexturedRect> getHitTiles() {
		return mHitTiles;
	}

	public void setShips(List<Ship> ss) {
		mShips = ss;
		for (int i = 0; i < mShips.size(); i++) {
			Ship s = mShips.get(i);
			s.configureBoardConstraints(this);
			s.setPosIndex(0, i);
		}
	}

	/**
	 * Returns the first ship which lies on the coordinate {x, y}
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public Ship getShipAtIndex(int x, int y) {
		if (mShips != null) {
			for (Ship s : mShips) {
				if (s.isOnGridTile(x, y)) {
					return s;
				}
			}
		}
		return null;
	}

	/**
	 * Marks the ship at the index (found with getShipAtIndex) as selected.
	 * Deselects other ships
	 * 
	 * @param x
	 * @param y
	 */
	public void selectShipAtIndex(int x, int y) {
		if (mShips != null) {
			Ship s = getShipAtIndex(x, y);
			if (s != null) {
				// Deselect all other ships
				for (Ship othership : mShips) {
					othership.setSelected(false);
				}
				s.setSelected(true);
			}
		}
	}

	/**
	 * Marks the ship as selected, and deselects other ships Does nothing if
	 * ship is not contained by the board
	 * 
	 * @param ship
	 */
	public void selectShip(Ship ship) {
		if (mShips != null && ship != null && mShips.contains(ship)) {
			// Deselect all other ships
			for (Ship othership : mShips) {
				othership.setSelected(false);
			}
			ship.setSelected(true);
		}
	}

	public Ship getSelectedShip() {
		for (Ship ship : mShips) {
			if (ship.isSelected()) {
				return ship;
			}
		}
		return null;
	}

	/**
	 * Verifies that ship won't overlap other ships, or be out of bounds at the
	 * new position.
	 * 
	 * @param x
	 * @param y
	 * @param ship
	 * @return true if the new position is ok, false otherwise or if ship is not
	 *         on this board
	 */
	public boolean verifyNewShipPos(int x, int y, Ship ship) {
		if (mShips.contains(ship) && ship.wouldBeOnGridAtPos(x, y)) {
			for (Ship othership : mShips) {
				if (othership != ship
						&& ship.wouldIntersectShipAtPos(x, y, othership)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks if the ship can be rotated on the board in its current position
	 * 
	 * @param ship
	 * @return true if the new position is ok, false otherwise or if ship is not
	 *         on this board
	 */
	public boolean verifyShipRotation(Ship ship) {
		if (mShips.contains(ship) && ship.wouldBeOnGridAfterRotate()) {
			for (Ship othership : mShips) {
				if (othership != ship
						&& ship.wouldIntersectShipAfterRotate(othership)) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	/**
	 * Sets a tile to the specified colour Usually, should be TILE_COLOR_NORMAL,
	 * TILE_COLOR_MISS, TILE_COLOR_HIT
	 * 
	 * @param color
	 */
	public void setTileColour(int colour, int col, int row) {
		mTileRows.get(row).get(col).setColor(colour);
	}

	/**
	 * Adds a hit tile to the array of hit tiles
	 * 
	 * @param col
	 *            , row
	 */
	public void setHitTile(int col, int row) {
		if (numHitTiles == 17 || mHitTiles == null)
			return; // return if no hit tiles or if uninitialized (error!!)
		float tileGridSize = getTileGridSize();
		mHitTiles.get(numHitTiles).setPosition(
				mTopLeftX + ((col + 1) * tileGridSize),
				mTopLeftY - ((row + 1) * tileGridSize));
		numHitTiles++;
	}

	/**
	 * Selects the currently selected tile. If the index is invalid (< 0 or >=
	 * BOARD_SIZE), selects no tile.
	 * 
	 * @param col
	 * @param row
	 */
	public void setSelectedTile(int col, int row) {
		if (!isTileIndexValid(col, row)) {
			mSelectedTileIndex.x = -1;
		} else {
			mSelectedTileIndex.x = col;
			mSelectedTileIndex.y = row;

			float tileGridSize = getTileGridSize();
			mSelectionTile.setPosition(mTopLeftX + ((col + 1) * tileGridSize),
					mTopLeftY - ((row + 1) * tileGridSize));
		}
	}

	public void setSelectedTile(BoardCoord bc) {
		setSelectedTile(bc.x, bc.y);
	}

	public BoardCoord getSelectedTileIndex() {
		BoardCoord coord = null;
		if (isTileIndexValid(mSelectedTileIndex.x, mSelectedTileIndex.y)) {
			coord = new BoardCoord(mSelectedTileIndex);
		}
		return coord;
	}

	/**
	 * Gets a tile to the specified colour Usually, will be TILE_COLOR_NORMAL,
	 * TILE_COLOR_MISS, TILE_COLOR_HIT
	 */
	public int getTileColour(int col, int row) {
		return mTileRows.get(row).get(col).getColor();
	}

	/**
	 * Returns the indexes of the tile at the given location
	 * 
	 * @param x
	 * @param y
	 * @return { col, row } or null if outside the board
	 */
	public BoardCoord getTileIndexAtLocation(float x, float y) {
		double col = ((((x - mTopLeftX) - getTileOffset()) / (mSideLength - getTileGridSize())) * BOARD_SIZE);
		double row = ((((mTopLeftY - y) - getTileOffset()) / (mSideLength - getTileGridSize())) * BOARD_SIZE);

		if (!isTileIndexValid((int) col, (int) row)) {
			return null;
		}

		return new BoardCoord((int) col, (int) row);
	}

	/**
	 * Returns the bottom right position of the tile at the given index
	 * 
	 * @param x
	 * @param y
	 * @return { col, row }
	 */
	public float[] getTileLocationAtIndex(int x, int y) {
		float[] pos = { 0, 0 };
		pos[0] = mTopLeftX + ((x + 2) * getTileGridSize());
		pos[1] = mTopLeftY - ((y + 2) * getTileGridSize());
		return pos;
	}

	public float getTileOffset() {
		// 11 is the drawable ratio
		return (float) (mSideLength / 11.0);
	}

	public float getTileGridSize() {
		return (float) (mSideLength / 11.0);
	}

	public float getTilePadding() {
		return (float) (getTileGridSize() * 0.05);
	}

	public float[] getPosition() {
		return new float[] { mTopLeftX, mTopLeftY };
	}

	public static boolean isTileIndexValid(int col, int row) {
		return !(col >= BOARD_SIZE || col < 0 || row >= BOARD_SIZE || row < 0);
	}

	public static boolean isTileIndexValid(BoardCoord bc) {
		return isTileIndexValid(bc.x, bc.y);
	}

	/**
	 * A holder class for coordinates.
	 */
	public static class BoardCoord {
		public int x;
		public int y;

		public BoardCoord(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public BoardCoord(BoardCoord bc) {
			this(bc.x, bc.y);
		}
	}
}
