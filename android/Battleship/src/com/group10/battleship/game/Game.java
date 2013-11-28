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
import com.group10.battleship.R;
import com.group10.battleship.audio.SoundManager;
import com.group10.battleship.game.ai.BattleshipAI;
import com.group10.battleship.game.ai.RandomAI;
import com.group10.battleship.game.ai.SmartAI;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.GL20Renderer;
import com.group10.battleship.graphics.GL20Renderer.RendererListener;
import com.group10.battleship.graphics.TexturedRect;
import com.group10.battleship.model.Board;
import com.group10.battleship.model.Board.BoardCoord;
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
	
	private SoundManager mSoundManager = SoundManager.getInstance();

	private Board mPlayerBoard;
	private Board mOpponentBoard;
	private TexturedRect mBackground;
	
	private boolean isHost;
	private boolean mIsMultiplayer;
	
	private boolean hasReceivedOpponentBoard = false;
	private boolean willYieldTurn = false;
	
	private GameState mState;
	
	// Temporary coords for last move if player 2s
	private BoardCoord mLastMove;

	// Ship dragging state
	private Ship mDraggedShip;
	// How far away the initial touch was from the ship's 'origin'
	private int[] mShipDraggingOffset;
	
	private GameStateChangedListener mStateListener;
	
	private BattleshipAI mSingleplayerAI;
	private int mAIDifficulty = 1;
	
	public enum GameState {
		UNINITIALIZED, PLACING_SHIPS, WAITING_FOR_OPPONENT, TAKING_TURN, GAME_OVER_WIN, GAME_OVER_LOSS
	}

	public static Game getInstance() {
		if (sGameInstance == null)
			sGameInstance = new Game();

		return sGameInstance;
	}
	
	public void setDifficulty(int diff) {
		mAIDifficulty = diff; 
	}

	private Game() {
		setState(GameState.UNINITIALIZED);
		mShipDraggingOffset = new int[] { 0, 0 };
		mLastMove = new BoardCoord(-1, -1);
		NetworkManager.getInstance().setOnAndroidDataReceivedListener(this);
	}

	public void start(boolean isMultiplayer) {
		setState(GameState.PLACING_SHIPS);
		mIsMultiplayer = isMultiplayer;
		willYieldTurn = new Random().nextBoolean();
		if (isMultiplayer) {
			isHost = NetworkManager.getInstance().isHost();
		} else {
			mSingleplayerAI = new SmartAI();
			mSingleplayerAI.setDifficulty(mAIDifficulty);
			isHost = true;
		}
	}

	/**
	 * Reconfigures the game to use the renderer. Does not affect any internal
	 * game state.
	 * 
	 * @param context
	 * @param renderer
	 */
	public void configure(Context context, GL20Renderer renderer) {
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
	
	public void forfeit() {
		if (isMultiplayer()) {
			try {
				NetworkManager.getInstance().send(ModelParser.getJsonForGameOver(true), true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (isHost) {
			NIOS2NetworkManager.sendGameOver(false, true);
		}
		
		invalidate();
	}
	
	public void win(boolean youWon) {
		if (isMultiplayer() && isHost) {
			try {
				NetworkManager.getInstance().send(ModelParser.getJsonForGameOver(!youWon), true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		
		if (isHost) {
			NIOS2NetworkManager.sendGameOver(false, true);
		}
		
		if (youWon) {
			setState(GameState.GAME_OVER_WIN);
		} else {
			setState(GameState.GAME_OVER_LOSS);
			mOpponentBoard.revealShips();
		}
	}
	
	public boolean isMultiplayer() {
		return mIsMultiplayer;
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
		mDrawList = new ArrayList<GL20Drawable>();
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
		
		if (mBackground != null) {
			mDrawList.remove(mBackground);
		}
		mBackground = new TexturedRect(mContext, R.drawable.gamebackground);
		mBackground.setPosition(-1f, 3f);
		mBackground.setSize(2f, 4f);
		
		mDrawList.add(mBackground);
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
		// deselect the highlighted ship
		if(mPlayerBoard.getSelectedShip() != null) mPlayerBoard.getSelectedShip().setSelected(false);
		try {
			if (!isMultiplayer()) {
				mSingleplayerAI.arrangeShips(mOpponentBoard);
				if(willYieldTurn) {
					setState(GameState.WAITING_FOR_OPPONENT);
				} else {
					setState(GameState.TAKING_TURN);
				}
			} else if(!NetworkManager.getInstance().isHost()) {
				// Is player 2
				Log.d(TAG, "Sending board");
				NetworkManager.getInstance().send(ModelParser.getJsonForBoard(mPlayerBoard.getShips()), true);
				setState(GameState.WAITING_FOR_OPPONENT);
			} else if(hasReceivedOpponentBoard) {
				// if host already received client board & is pressing to confirm own
				if(willYieldTurn) {
					setState(GameState.WAITING_FOR_OPPONENT);
					NetworkManager.getInstance().send(ModelParser.getJsonForYield(), true);
				} else {
					setState(GameState.TAKING_TURN);
				}
			} else {
				// Host is confirming board, but still waiting for other player to confirm.
				setState(GameState.WAITING_FOR_OPPONENT);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void onFireButtonPressed() {
		BoardCoord pos = mOpponentBoard.getSelectedTileIndex();
		// Don't do anything if tile has already been acted on.
		if (mOpponentBoard.getTileColour(pos.x, pos.y) == Board.TILE_COLOR_NORMAL) {
			if (isHost || !isMultiplayer()) { 
				boolean hit = processMoveOnBoard(pos.x, pos.y, true);
				if (isMultiplayer()){
					try {
						Ship oppShip = mOpponentBoard.getShipAtIndex(pos.x, pos.y);
						boolean sunk = false;
						if (oppShip != null)
							sunk = oppShip.isSunk();
						String msg = ModelParser.getJsonForMove(pos.x, pos.y, ModelParser.getJsonForMoveResponse(hit, sunk));
						NetworkManager.getInstance().send(msg, true);
					} catch (JSONException e) {
						Log.e(TAG, "THIS SHOULD NEVER HAPPEN");
						e.printStackTrace();
					}
				}
			} else {
				try {
					mLastMove.x = pos.x;
					mLastMove.y = pos.y;
					String msg = ModelParser.getJsonForMove(pos.x, pos.y, null);
					NetworkManager.getInstance().send(msg, true);
				} catch (JSONException e) {
					Log.e(TAG, "THIS SHOULD NEVER HAPPEN");
					e.printStackTrace();
				}
			}
			setState(GameState.WAITING_FOR_OPPONENT);
		}
		
		if (mOpponentBoard.isAllSunk())
			win(true);
	}

	public void onTouchGLSurface(MotionEvent me, float x, float y) {
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			// Check for selection of enemy tile
			BoardCoord inx = mOpponentBoard.getTileIndexAtLocation(x, y);
			if (inx != null && mState == GameState.TAKING_TURN) {
                Log.d(TAG, "Touched down enemy tile: " + inx.x + "," + inx.y);
                mOpponentBoard.setSelectedTile(inx);
        }

			// Check for selecting ship
			inx = mPlayerBoard.getTileIndexAtLocation(x, y);
			if (inx != null) {
				Log.d(TAG, "Touched down player tile: " + inx.x + "," + inx.y);

				Ship selectShip = mPlayerBoard.getShipAtIndex(inx.x, inx.y);
				if (selectShip != null && mState == GameState.PLACING_SHIPS) {
					mPlayerBoard.selectShip(selectShip);
					mDraggedShip = selectShip;

					mShipDraggingOffset[0] = inx.x
							- selectShip.getPosIndex().x;
					mShipDraggingOffset[1] = inx.y
							- selectShip.getPosIndex().y;
				}
			}
		} else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			if (mDraggedShip != null) {
				BoardCoord inx = mPlayerBoard.getTileIndexAtLocation(x, y);
				if (inx != null) {
					/*
					 * To make moving around easier, find how much the user has
					 * moved the ship, from the point where they selected it.
					 * Find the potential index to move the ship to here.
					 */
					inx.x = inx.x - mShipDraggingOffset[0];
					inx.y = inx.y - mShipDraggingOffset[1];

					BoardCoord dragShipPos = mDraggedShip.getPosIndex();
					// Only bother moving the ship, if it has moved
					if ((dragShipPos.x != inx.x || dragShipPos.y != inx.y)
							&& mPlayerBoard.verifyNewShipPos(inx.x, inx.y,
									mDraggedShip)) {
						mDraggedShip.setPosIndex(inx.x, inx.y);
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
				if(obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.MOVE_TYPE_VAL)) {
					// Process Move data
					if(!isHost) {
						// Guest is given host's move and if it hit or missed
						JSONObject responseObj = (JSONObject) new JSONTokener(obj.getString(ModelParser.MOVE_RESPONSE_KEY)).nextValue();
						boolean wasHit = responseObj.getBoolean(ModelParser.MOVE_RESPONSE_HIT_KEY);
						boolean wasSunk = responseObj.getBoolean(ModelParser.MOVE_RESPONSE_SUNK_KEY);
						
						if (wasSunk) {
							mPlayerBoard.sinkShipAt(obj.getInt(ModelParser.MOVE_XPOS_KEY), obj.getInt(ModelParser.MOVE_YPOS_KEY));
						} else {
							mPlayerBoard.setTileColour(wasHit?Board.TILE_COLOR_HIT:Board.TILE_COLOR_MISS, 
									obj.getInt(ModelParser.MOVE_XPOS_KEY), obj.getInt(ModelParser.MOVE_YPOS_KEY));
							if(wasHit)
								mPlayerBoard.setHitTile(obj.getInt(ModelParser.MOVE_XPOS_KEY), obj.getInt(ModelParser.MOVE_YPOS_KEY));
						}
					} else {
						// Host must process the move, and return if it hit/missed
						boolean wasHit = processMoveOnBoard(obj.getInt(ModelParser.MOVE_XPOS_KEY), 
								obj.getInt(ModelParser.MOVE_YPOS_KEY), false);
						
						Ship playerShip = mPlayerBoard.getShipAtIndex(obj.getInt(ModelParser.MOVE_XPOS_KEY),
								obj.getInt(ModelParser.MOVE_YPOS_KEY));
						boolean wasSunk = false;
						if (playerShip != null)
							wasSunk = playerShip.isSunk();
						
						NetworkManager.getInstance().send(ModelParser.getJsonForMoveResponse(wasHit, wasSunk), true);
						
						if(mPlayerBoard.isAllSunk())
							win(false);
					}
					Toast.makeText(mContext, "Move received: " + obj.getInt(ModelParser.MOVE_XPOS_KEY) + ", " + obj.getInt(ModelParser.MOVE_YPOS_KEY), Toast.LENGTH_SHORT).show();
					setState(GameState.TAKING_TURN);
					
				} else if (obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.MOVE_RESPONSE_TYPE_VAL)) {
					// Guest is receiving response to move
					boolean wasHit = obj.getBoolean(ModelParser.MOVE_RESPONSE_HIT_KEY);
					boolean wasSunk = obj.getBoolean(ModelParser.MOVE_RESPONSE_SUNK_KEY);
					
					if (wasSunk) {
						mOpponentBoard.sinkShipAt(mLastMove.x, mLastMove.y);
					} else {
						mOpponentBoard.setTileColour(wasHit ? Board.TILE_COLOR_HIT : Board.TILE_COLOR_MISS, 
								mLastMove.x, mLastMove.y);
						if(wasHit)
							mOpponentBoard.setHitTile(mLastMove.x, mLastMove.y);
					}
				} else if(obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.BOARD_TYPE_VAL)) {
					// Host is receiving the guest's board data
					JSONArray shipArr = obj.getJSONArray(ModelParser.BOARD_TYPE_SHIPS_KEY);
					Toast.makeText(mContext, "Host received game board", Toast.LENGTH_SHORT).show();
					hasReceivedOpponentBoard = true;
					
					for(int i=0; i < shipArr.length(); i++) {
						JSONObject ship = (JSONObject)shipArr.get(i);
						mOpponentBoard.setShip(ship.getInt(ModelParser.SHIP_XPOS_KEY), 
								ship.getInt(ModelParser.SHIP_YPOS_KEY), 
								ship.getBoolean(ModelParser.SHIP_HORIZ_KEY), 
								ModelParser.getShipTypeFromString(ship.getString(ModelParser.SHIP_TYPE_TYPE_KEY)));
					}
					
					if(mState == GameState.WAITING_FOR_OPPONENT) {
						// Host had already submitted board
						if(willYieldTurn) {
							// Guest gets the first move
							NetworkManager.getInstance().send(ModelParser.getJsonForYield(), true);
						} else { 
							setState(GameState.TAKING_TURN);
						}
					}
				} else if(obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.YIELD_TURN_TYPE_VAL)) {
					// The guest has been told by host that it gets to move first.
					setState(GameState.TAKING_TURN);
				} else if(obj.getString(ModelParser.TYPE_KEY).equals(ModelParser.GAME_OVER_TYPE_VAL)) {
					boolean youWin = obj.getBoolean(ModelParser.GAME_OVER_WIN_KEY);
					win(youWin);
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error getting json object from json string");
			}
		}
	}
	
	/**
	 * Processes a move attempt taken on the board
	 * Sends message to nios appropriately
	 * @param x
	 * @param y
	 * @return true if the move hit, false if missed
	 */
	private boolean processMoveOnBoard(int x, int y, boolean isHostsMove) {
		Board board;
		if (isHostsMove) board = mOpponentBoard;
		else board = mPlayerBoard;
		
		boolean wasHit = board.playerShotAttempt(x, y);
		if(wasHit) {
			mSoundManager.playSFX(R.raw.hit);
			NIOS2NetworkManager.sendHit(isHostsMove, x, y);
		} else if (wasHit) {
			// TODO: add sinking ship
			mSoundManager.playSFX(R.raw.ship_explode);
		} else {
			mSoundManager.playSFX(R.raw.miss);
			NIOS2NetworkManager.sendMiss(isHostsMove, x, y);
		}
		return wasHit;
	}
	
	public static interface GameStateChangedListener {
		public void onGameStateChanged();
	}
	
	private void setState(GameState state)
	{
		mState = state;
		if(mStateListener != null)
			mStateListener.onGameStateChanged();
		
		if (!isMultiplayer() && state == GameState.WAITING_FOR_OPPONENT) {
			performAIMove();
			setState(GameState.TAKING_TURN);
		}
	}
	
	private void performAIMove() {
		BoardCoord pos;
		// Ask AI for move until it responds with a valid one.
		do {
			pos = mSingleplayerAI.getNextMove();
			if (pos == null) {
				Log.e(TAG, "move requested of AI after all spaces taken.");
				break;
			}
		} while(pos != null && mPlayerBoard.getTileColour(pos.x, pos.y) != Board.TILE_COLOR_NORMAL);
		
		if (pos != null) {
			Ship target = mPlayerBoard.getShipAtIndex(pos.x, pos.y);
			boolean hit = processMoveOnBoard(pos.x, pos.y, false);
			boolean sunk = false;
			
			//Give AI the ship if the ship is sunk, 
			//The coordinates of the sunken ship is public information
			 
			if (target != null)
				sunk = target.isSunk();
			
			if (sunk)
				mSingleplayerAI.respondToLastMove(hit, target);
			else
				mSingleplayerAI.respondToLastMove(hit, null);
			
		} else {
			Log.e(TAG, "move requested of AI after all spaces taken.");
		}
		
		if (mPlayerBoard.isAllSunk())
			win(false);
	}
}