package com.group10.battleship.game;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.util.Log;

import com.group10.battleship.GameActivity;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.GL20Renderer;
import com.group10.battleship.graphics.GL20Renderer.RendererListener;
import com.group10.battleship.model.*;

public class Game implements RendererListener {

	private static final String TAG = Game.class.getSimpleName();
	
	private static Game sGameInstance;
	
	private Context mContext;
	
	private List<GL20Drawable> mDrawList;
	
	private Board mPlayerBoard;
	private Board mOpponentBoard;
	
	private GameState mState;
	
	public enum GameState {
		UNITIALIZED, PLACING_SHIPS, WAITING_FOR_OPPONENT, TAKING_TURN,
		GAME_OVER_WIN, GAME_OVER_LOSS
	}
	
	public static Game getInstance() {
		if (sGameInstance == null)
			sGameInstance = new Game();
		
		return sGameInstance;
	}
	
	private Game() {
		mState = GameState.UNITIALIZED;
	}
	
	public void start() {
		mState = GameState.PLACING_SHIPS;
	}
	
	public void configure(Context context, GL20Renderer renderer) {
		mDrawList = new ArrayList<GL20Drawable>();
		renderer.addRendererListener(this);
		mContext = context;
	}
	
	/**
	 * Resets the static game instance to an uninitialized state.
	 * Generally should be used when a game is complete, and is no longer valid.
	 * start() and configureBoard() must be called after this.
	 */
	public void invalidate() {
		mState = GameState.UNITIALIZED;
		mPlayerBoard = null;
		mOpponentBoard = null;
	}
	
	public GameState getState() { return mState; }

	@Override
	public void onSurfaceCreated(GL20Renderer renderer) {
		Log.d(TAG, "GL surface created");		
		renderer.setDrawList(mDrawList);
	}

	@Override
	public void onFrameDrawn(GL20Renderer renderer) {
		
	}

	@Override
	public void onSurfaceChanged(GL20Renderer renderer) {
		Log.d(TAG, "GL surface changed");
		
		float x = renderer.getXLeft();
		float y = renderer.getYTop();
		float width = renderer.getXRight() - x;
		float height = y - renderer.getYBottom();
		float sideLength = (height > width) ? width : height;
		
		if (mPlayerBoard == null) {
			mPlayerBoard = new Board(mContext, sideLength, x, y);
		} else {
			// The screen may have changed, so we need to rebuild the board
			Board b = new Board(mContext, sideLength, x, y);
			b.copyState(mPlayerBoard);
			mDrawList.remove(mPlayerBoard);
			mPlayerBoard = b;
		}
		mDrawList.add(mPlayerBoard);
		
	}
	
	public void onTouchGLSurface(float x, float y) {
		int[] inx = mPlayerBoard.getTileIndexAtLocation(x, y);
		if (inx != null) {
			Log.d(TAG, "Touched tile: "+inx[0]+","+inx[1]);

			// TODO: this should only be permitted during the players turn
			mPlayerBoard.setSelectedTile(inx[0], inx[1]);
				
			
			// TODO do stuff with the touch event, during game
		}
	}
	
}
