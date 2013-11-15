package com.group10.battleship.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.group10.battleship.PrefsManager;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.GL20Renderer;
import com.group10.battleship.graphics.GL20Renderer.RendererListener;
import com.group10.battleship.model.Board;
import com.group10.battleship.model.ModelParser;
import com.group10.battleship.model.Ship;
import com.group10.battleship.network.NIOS2NetworkManager;
import com.group10.battleship.network.NetworkManager;
import com.group10.battleship.network.NetworkManager.OnAndroidDataReceivedListener;

public class Game implements RendererListener, OnAndroidDataReceivedListener {

	private static final String TAG = Game.class.getSimpleName();

	private static Game sGameInstance;

	private Context mContext;

	private List<GL20Drawable> mDrawList;

	private Board mPlayerBoard;
	private Board mOpponentBoard;

	private boolean isHost;
	private boolean hasReceivedOpponentBoard = false;
	private boolean willYieldTurn = false;
	
	private GameState mState;
	
	// Temporary coords for last move if player 2s
	private int[] mLastMove;

	// Ship dragging state
	private Ship mDraggedShip;
	// How far away the initial touch was from the ship's 'origin'
	private int[] mShipDraggingOffset;
	
	private GameStateChangedListener mStateListener;

	public enum GameState {
		UNINITIALIZED, PLACING_SHIPS, WAITING_FOR_OPPONENT, TAKING_TURN, GAME_OVER_WIN, GAME_OVER_LOSS
	}

	public static Game getInstance() {
		if (sGameInstance == null)
			sGameInstance = new Game();

		return sGameInstance;
	}

	private Game() {
		setState(GameState.UNINITIALIZED);
		mShipDraggingOffset = new int[] { 0, 0 };
		mLastMove = new int[] { -1, -1 };
		if (!PrefsManager.getInstance().getBoolean(
				PrefsManager.PREF_KEY_LOCAL_DEBUG, false))
			NetworkManager.getInstance().setOnAndroidDataReceivedListener(this);
	}

	public void start() {
		setState(GameState.PLACING_SHIPS);
		willYieldTurn = new Random().nextBoolean();
		isHost = NetworkManager.getInstance().isHost;
	}

	/**
	 * Reconfigures the game to use the renderer. Does not affect any internal
	 * game state.
	 * 
	 * @param context
	 * @param renderer
	 */
	public void configure(Context context, GL20Renderer renderer) {
		mDrawList = new ArrayList<GL20Drawable>();
		renderer.addRendererListener(this);
		mContext = context;
	}

	/**
	 * Resets the static game instance to an uninitialized state. Generally
	 * should be used when a game is complete, and is no longer valid. start()
	 * and configureBoard() must be called after this.
	 */
	public void invalidate() {
		setState(GameState.UNINITIALIZED);
		mPlayerBoard = null;
		mOpponentBoard = null;
	}

	public GameState getState() {
		return mState;
	}
	
	public void setGameStateListener(GameStateChangedListener listener){
		mStateListener = listener;
	}

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

		float x = renderer.getDefaultLeft();
		float y = renderer.getDefaultTop();
		float width = renderer.getDefaultRight() - x;
		float height = y - renderer.getDefaultBottom();
		float sideLength = (height > width) ? width : height;

		if (mPlayerBoard == null) {
			mPlayerBoard = new Board(mContext, sideLength, x, y, true);
		} else {
			// The screen may have changed, so we need to rebuild the board
			Board b = new Board(mContext, sideLength, x, y, true);
			b.copyState(mPlayerBoard);
			mDrawList.remove(mPlayerBoard);
			mPlayerBoard = b;
		}

		if (mOpponentBoard == null) {
			mOpponentBoard = new Board(mContext, sideLength, x, y + height,
					false);
		} else {
			// The screen may have changed, so we need to rebuild the board
			Board b = new Board(mContext, sideLength, x, y + height, false);
			b.copyState(mOpponentBoard);
			mDrawList.remove(mOpponentBoard);
			mOpponentBoard = b;
		}

		mDrawList.add(mOpponentBoard);
		mDrawList.add(mPlayerBoard);

	}

	public void onRotateButtonPressed() {
		Ship ship = mPlayerBoard.getSelectedShip();
		if (ship != null) {
			if (mPlayerBoard.verifyShipRotation(ship)) {
				ship.setHorizontal(!ship.isHorizontal());
			}
		}
	}
	
	public void onConfirmBoardPressed()
	{
			try {
				if(!PrefsManager.getInstance().getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false)) 
				{
					if(!NetworkManager.getInstance().getIsHost()){
						Log.d(TAG, "Sending board");
						NetworkManager.getInstance().send(ModelParser.getJsonForBoard(mPlayerBoard.getShips()), true);
						setState(GameState.WAITING_FOR_OPPONENT);
					}
					// if host already received client board & is pressing to confirm own
					else if(hasReceivedOpponentBoard)
					{
						if(willYieldTurn)
						{
							setState(GameState.WAITING_FOR_OPPONENT);
							NetworkManager.getInstance().send(ModelParser.getJsonForYield(), true);
						}
						else
							setState(GameState.TAKING_TURN);
					}
					else 
					{
						setState(GameState.WAITING_FOR_OPPONENT);
					}
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
	}
	
	public void onFireButtonPressed() {
		int[] pos = mOpponentBoard.getSelectedTileIndex();
		if (mOpponentBoard.getTileColour(pos[0], pos[1]) == Board.TILE_COLOR_NORMAL) {
			if (isHost){ 
				boolean hit = mOpponentBoard.playerShotAttempt(pos[0], pos[1]);
				if (hit) {
					if (PrefsManager.getInstance().getBoolean(
							PrefsManager.PREF_KEY_USE_NIOS, true)) {
						NIOS2NetworkManager.sendHit(true, pos[0], pos[1]);
					}
				} else {
					if (PrefsManager.getInstance().getBoolean(
							PrefsManager.PREF_KEY_USE_NIOS, true)) {
						NIOS2NetworkManager.sendMiss(true, pos[0], pos[1]);
					}
				}
				try {
					String msg = ModelParser.getJsonForMove(pos[0], pos[1], ModelParser.getJsonForMoveResponse(hit));
					NetworkManager.getInstance().send(msg, true);
				} catch (JSONException e) {
					Log.e(TAG, "THIS SHOULD NEVER HAPPEN");
					e.printStackTrace();
				}
			} else {
				try {
					mLastMove[0] = pos[0];
					mLastMove[1] = pos[1];
					String msg = ModelParser.getJsonForMove(pos[0], pos[1], null);
					NetworkManager.getInstance().send(msg, true);
				} catch (JSONException e) {
					Log.e(TAG, "THIS SHOULD NEVER HAPPEN");
					e.printStackTrace();
				}
			}
			setState(GameState.WAITING_FOR_OPPONENT);
		}
	}

	public void onTouchGLSurface(MotionEvent me, float x, float y) {
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			// Check for selection of enemy tile
			int[] inx = mOpponentBoard.getTileIndexAtLocation(x, y);
			if (inx != null && mState == GameState.TAKING_TURN) {
                Log.d(TAG, "Touched down enemy tile: " + inx[0] + "," + inx[1]);
                mOpponentBoard.setSelectedTile(inx[0], inx[1]);
        }

			// Check for selecting ship
			inx = mPlayerBoard.getTileIndexAtLocation(x, y);
			if (inx != null) {
				Log.d(TAG, "Touched down player tile: " + inx[0] + "," + inx[1]);

				Ship selectShip = mPlayerBoard.getShipAtIndex(inx[0], inx[1]);
				if (selectShip != null && mState == GameState.PLACING_SHIPS) {
					mPlayerBoard.selectShip(selectShip);
					mDraggedShip = selectShip;

					mShipDraggingOffset[0] = inx[0]
							- selectShip.getPosIndex()[0];
					mShipDraggingOffset[1] = inx[1]
							- selectShip.getPosIndex()[1];
				}
			}
		} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			if (mDraggedShip != null) {
				int[] inx = mPlayerBoard.getTileIndexAtLocation(x, y);
				if (inx != null) {
					/*
					 * To make moving around easier, find how much the user has
					 * moved the ship, from the point where they selected it.
					 * Find the potential index to move the ship to here.
					 */
					inx[0] = inx[0] - mShipDraggingOffset[0];
					inx[1] = inx[1] - mShipDraggingOffset[1];

					int[] dragShipPos = mDraggedShip.getPosIndex();
					// Only bother moving the ship, if it has moved
					if ((dragShipPos[0] != inx[0] || dragShipPos[1] != inx[1])
							&& mPlayerBoard.verifyNewShipPos(inx[0], inx[1],
									mDraggedShip)) {
						mDraggedShip.setPosIndex(inx[0], inx[1]);
					}
				}
			}
		} else if (me.getAction() == MotionEvent.ACTION_UP) {
			Log.d(TAG, "Action up");
			mDraggedShip = null;
		}
	}

	public void ReceivedAndroidData(String message) {
		if(!(message.charAt(0) == '{')) { return; } // catch non-JSON messages
		else { 
			try {
				JSONObject obj = (JSONObject) new JSONTokener(message).nextValue();
				if(obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.MOVE_TYPE_VAL))
				{
					// TODO: determine hit/miss, update view with player move, send to NIOS & other player (if hit/miss)
					setState(GameState.TAKING_TURN);
					if(!isHost)
					{
						JSONObject responseObj = (JSONObject) new JSONTokener(obj.getString(ModelParser.MOVE_RESPONSE_KEY)).nextValue();
						boolean wasHit = responseObj.getBoolean(ModelParser.MOVE_RESPONSE_HIT_KEY);
						mPlayerBoard.setTileColour(wasHit?Board.TILE_COLOR_HIT:Board.TILE_COLOR_MISS, obj.getInt(ModelParser.MOVE_XPOS_KEY), obj.getInt(ModelParser.MOVE_YPOS_KEY));
					}
					else
					{
						boolean wasHit = mPlayerBoard.playerShotAttempt(obj.getInt(ModelParser.MOVE_XPOS_KEY), obj.getInt(ModelParser.MOVE_YPOS_KEY));
						NetworkManager.getInstance().send(ModelParser.getJsonForMoveResponse(wasHit), true);
					}
					Toast.makeText(mContext, "Move received: " + obj.getInt(ModelParser.MOVE_XPOS_KEY) + ", " + obj.getInt(ModelParser.MOVE_YPOS_KEY), Toast.LENGTH_SHORT).show();
				}
				else if (obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.MOVE_RESPONSE_TYPE_VAL))
				{
					setState(GameState.TAKING_TURN);
					boolean wasHit = obj.getBoolean(ModelParser.MOVE_RESPONSE_HIT_KEY);
					mOpponentBoard.setTileColour(wasHit?Board.TILE_COLOR_HIT:Board.TILE_COLOR_MISS, mLastMove[0], mLastMove[1]);
				}
				else if(obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.BOARD_TYPE_VAL))
				{
					// TODO: do things with it 
					JSONArray shipArr = obj.getJSONArray(ModelParser.BOARD_TYPE_SHIPS_KEY);
					Toast.makeText(mContext, "Host received game board", Toast.LENGTH_SHORT).show();
					hasReceivedOpponentBoard = true;
					for(int i=0; i < shipArr.length(); i++)
							{
								JSONObject ship = (JSONObject)shipArr.get(i);
								mOpponentBoard.setShip(ship.getInt(ModelParser.SHIP_XPOS_KEY), 
										ship.getInt(ModelParser.SHIP_YPOS_KEY), 
										ship.getBoolean(ModelParser.SHIP_HORIZ_KEY), 
										ModelParser.getShipTypeFromString(ship.getString(ModelParser.SHIP_TYPE_TYPE_KEY)));
							}	
					if(mState == GameState.WAITING_FOR_OPPONENT){
						if(!willYieldTurn)
							setState(GameState.TAKING_TURN);
						else 
							NetworkManager.getInstance().send(ModelParser.getJsonForYield(), true);
					}
				}
				else if(obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.YIELD_TURN_TYPE_VAL))
				{
					setState(GameState.TAKING_TURN);
				}
			} catch (JSONException e) {
				Log.d(TAG, "Error getting json object from json string");
				e.printStackTrace();		
		}
		}

	}
	
	public static interface GameStateChangedListener {
		public void onGameStateChanged();
	}
	
	private void setState(GameState state)
	{
		mState = state; 
		if(mStateListener != null)
			mStateListener.onGameStateChanged();

	}
}
