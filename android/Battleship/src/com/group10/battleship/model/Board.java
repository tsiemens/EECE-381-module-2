package com.group10.battleship.model;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;

import com.group10.battleship.R;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.TexturedRect;

public class Board implements GL20Drawable{

	public static final int TILE_COLOR_NORMAL = Color.parseColor("#aa4285f4");
	public static final int TILE_COLOR_MISS = Color.parseColor("#aaa0c3ff");
	public static final int TILE_COLOR_HIT = Color.parseColor("#aadb4437");
	public static final int BOARD_SIZE = 10;
	
	private Context mContext;
	private TexturedRect mNumberRow;
	private TexturedRect mAlphaCol;
	
	private ArrayList<ArrayList<TexturedRect>> mTileRows;
	
	private float mTopLeftX;
	private float mTopLeftY;
	private float mSideLength;
	
	public Board(Context context, float sideLength, float x, float y) {
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
			
			for (int col = 0; col < BOARD_SIZE ; col++) {
				tempTile = new TexturedRect(mContext, R.drawable.white_pix);
				tempTile.setColor(TILE_COLOR_NORMAL);
				tempTile.setPosition( mTopLeftX + ((col + 1)*tileGridSize)+tilePadding, 
						mTopLeftY - (((row + 1)*tileGridSize)+tilePadding) );
				tempTile.setSize(tileSize, tileSize);
				tempArray.add(tempTile);
			}
		}
	}
	
	@Override
	public void draw(float[] mvpMatrix) {
		mNumberRow.draw(mvpMatrix);
		mAlphaCol.draw(mvpMatrix);
		
		for (ArrayList<TexturedRect> al : mTileRows) {
			for (TexturedRect t : al) {
				t.draw(mvpMatrix);
			}
		}
	}
	
	/**
	 * Sets a tile to the specified colour
	 * Usually, should be TILE_COLOR_NORMAL, TILE_COLOR_MISS, TILE_COLOR_HIT
	 * @param color
	 */
	public void setTileColour(int colour, int col, int row) {
		mTileRows.get(row).get(col).setColor(colour);
	}
	
	/**
	 * Gets a tile to the specified colour
	 * Usually, will be TILE_COLOR_NORMAL, TILE_COLOR_MISS, TILE_COLOR_HIT
	 * @param color
	 */
	public int getTileColour(int col, int row) {
		return mTileRows.get(row).get(col).getColor();
	}
	
	/**
	 * Returns the indexes of the tile at the given location
	 * @param x
	 * @param y
	 * @return { col, row } or null if outside the board
	 */
	public int[] getTileIndexAtLocation(float x, float y) {
		double col = ((((x - mTopLeftX) - getTileOffset())/(mSideLength-getTileGridSize())) * BOARD_SIZE);
		double row = ((((mTopLeftY - y) - getTileOffset())/(mSideLength-getTileGridSize())) * BOARD_SIZE);
		
		if (col >= BOARD_SIZE || col < 0 || row >= BOARD_SIZE || row < 0) {
			return null;
		}
		
		return new int [] { (int)col, (int)row };
	}
	
	private float getTileOffset() {
		// 11 is the drawable ratio
		return (float) (mSideLength / 11.0);
	}
	
	private float getTileGridSize() {
		return (float) (mSideLength / 11.0);
	}
	
	private float getTilePadding() {
		return (float) (getTileGridSize() * 0.05);
	}
}
