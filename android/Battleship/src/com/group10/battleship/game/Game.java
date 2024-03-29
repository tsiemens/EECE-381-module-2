package com.group10.battleship.game;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Toast;

import com.group10.battleship.GameActivity;
import com.group10.battleship.PrefsManager;
import com.group10.battleship.R;
import com.group10.battleship.audio.SoundManager;
import com.group10.battleship.database.ConnectionHistoryRepository;
import com.group10.battleship.game.ai.BattleshipAI;
import com.group10.battleship.game.ai.SmartAI;
import com.group10.battleship.graphics.BitmapUtils;
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

	private float mFireTileX;
	private float mFireTileY;
	private float mTileLength;

	private String mOpponentProfileName;
	private String mOpponentProfileTaunt;
	private Bitmap mOpponentProfileImage;
	private ProfileDataReceivedListener mProfileDataListener;

	private boolean isHost;
	private boolean mIsMultiplayer;
	private boolean hasReceivedOpponentBoard = false;

	private boolean willYieldTurn = false;
	private boolean mGameStarted = false;

	private GameState mState;

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

	/**
	 * Sets the Ai's difficulty
	 * 
	 * @param diff
	 *            1 for hard, 2 for medium, 3 for easy
	 */
	public void setDifficulty(int diff) {
		mAIDifficulty = diff;
	}

	private Game() {
		setState(GameState.UNINITIALIZED);
		mShipDraggingOffset = new int[] { 0, 0 };
		NetworkManager.getInstance().setOnAndroidDataReceivedListener(this);
	}

	public void start(boolean isMultiplayer) {
		PrefsManager pm = PrefsManager.getInstance();
		setState(GameState.PLACING_SHIPS);
		mIsMultiplayer = isMultiplayer;
		willYieldTurn = new Random().nextBoolean();

		if (isMultiplayer) {
			isHost = NetworkManager.getInstance().isHost();

			if (!isHost) {
				String ip = NetworkManager.getInstance().getAndroidHostIP();
				if (ConnectionHistoryRepository.updateLastPlayed(ip) <= 0) {
					// The item was not already in history
					ConnectionHistoryRepository
							.addHistoryItem(new ConnectionHistoryRepository.HistoryItem(
									mOpponentProfileName, ip));
				} else {
					ConnectionHistoryRepository.updateNameforItem(ip,
							mOpponentProfileName);
				}
			}

			// Send profile data
			String imageUriStr = pm.getString(
					PrefsManager.KEY_PROFILE_IMAGE_URI, null);
			try {
				NetworkManager.getInstance().send(
						ModelParser.getJsonForProfile(pm.getString(
								PrefsManager.KEY_PROFILE_NAME, null),
								imageUriStr != null ? Uri.parse(imageUriStr)
										: null, pm.getString(
										PrefsManager.KEY_PROFILE_TAUNT, null)),
						true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		} else {
			mSingleplayerAI = new SmartAI();
			mSingleplayerAI.setDifficulty(mAIDifficulty);
			isHost = true;
		}
		
		if (isHost)
			willYieldTurn = new Random().nextBoolean();
		else
			willYieldTurn = true;
	}

	public void onNiosGameStarted() {
		// Send names to nios
		if (isHost) {
			NIOS2NetworkManager.sendProfileName(true, PrefsManager
					.getInstance()
					.getString(PrefsManager.KEY_PROFILE_NAME, "-"));
			if (mOpponentProfileName != null) {
				NIOS2NetworkManager
						.sendProfileName(false, mOpponentProfileName);
			}
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
		Log.d(TAG, "Inavlidating Game");
		setState(GameState.UNINITIALIZED);
		mPlayerBoard = null;
		mOpponentBoard = null;

		mOpponentProfileImage = null;
		mOpponentProfileName = null;
		mOpponentProfileTaunt = null;
	}

	public void forfeit() {
		if (mState != GameState.GAME_OVER_LOSS
				&& mState != GameState.GAME_OVER_WIN) {
			if (isMultiplayer()) {
				try {
					NetworkManager.getInstance().send(
							ModelParser.getJsonForGameOver(true), true);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			if (isHost) {
				NIOS2NetworkManager.sendGameOver(false);
			}
		}

		invalidate();
	}

	public void win(boolean youWon) {
		if (isMultiplayer() && isHost) {
			try {
				NetworkManager.getInstance().send(
						ModelParser.getJsonForGameOver(!youWon), true);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		if (isHost) {
			NIOS2NetworkManager.sendGameOver(youWon);
		}

		if (youWon) {
			setState(GameState.GAME_OVER_WIN);
		} else {
			setState(GameState.GAME_OVER_LOSS);
			if (mOpponentBoard != null)
				mOpponentBoard.revealShips();
		}
	}

	public boolean isMultiplayer() {
		return mIsMultiplayer;
	}

	public GameState getState() {
		return mState;
	}

	public void setGameStateListener(GameStateChangedListener listener) {
		mStateListener = listener;
	}

	public float[] getFirePosition() {
		float[] pos = { mFireTileX, mFireTileY };
		return pos;
	}

	public float getTileLength() {
		return mTileLength;
	}
	
	public boolean isGameStarted() {
		return mGameStarted;
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

		mTileLength = sideLength;

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

	public void onConfirmBoardPressed() {
		
		mGameStarted = false;
		
		// deselect the highlighted ship
		if (mPlayerBoard.getSelectedShip() != null)
			mPlayerBoard.getSelectedShip().setSelected(false);
		try {
			if (!isMultiplayer()) {
				mSingleplayerAI.arrangeShips(mOpponentBoard);
				hasReceivedOpponentBoard = true;
			} else {
				// Player is confirming board
				NetworkManager.getInstance().send(
						ModelParser.getJsonForBoard(mPlayerBoard.getShips()),
						true);
			}

			if (willYieldTurn) {
				setState(GameState.WAITING_FOR_OPPONENT);
				if (isMultiplayer() && isHost)
					NetworkManager.getInstance().send(
							ModelParser.getJsonForYield(), true);
			} else if (hasReceivedOpponentBoard == false) {
				setState(GameState.WAITING_FOR_OPPONENT);
			} else {
				setState(GameState.TAKING_TURN);
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public void onFireButtonPressed() {
		BoardCoord pos = mOpponentBoard.getSelectedTileIndex();
		if (pos == null) {
			// if user didn't select a text, don't let them send a fire
			Toast.makeText(mContext, "Please select a tile first",
					Toast.LENGTH_SHORT).show();
			return;
		}
		// Don't do anything if tile has already been acted on.
		if (mOpponentBoard.getTileColour(pos.x, pos.y) == Board.TILE_COLOR_NORMAL) {
			processMoveOnBoard(pos.x, pos.y, true);
			if (isMultiplayer()) {
				try {
					Ship oppShip = mOpponentBoard.getShipAtIndex(pos.x, pos.y);
					if (oppShip != null)
						oppShip.isSunk();
					String msg = ModelParser.getJsonForMove(pos.x, pos.y);
					NetworkManager.getInstance().send(msg, true);
				} catch (JSONException e) {
					Log.e(TAG, "THIS SHOULD NEVER HAPPEN");
					e.printStackTrace();
				}
			}

			mFireTileX = mOpponentBoard.getTileLocationAtIndex(pos.x, pos.y)[0];
			mFireTileY = mOpponentBoard.getTileLocationAtIndex(pos.x, pos.y)[1];

			if (mOpponentBoard.isAllSunk()) {
				win(true);
			} else {
				setState(GameState.WAITING_FOR_OPPONENT);
			}
		}
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

					mShipDraggingOffset[0] = inx.x - selectShip.getPosIndex().x;
					mShipDraggingOffset[1] = inx.y - selectShip.getPosIndex().y;
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
		if (!(message.charAt(0) == '{')) {
			return;
		} // catch non-JSON messages
		else {
			try {
				JSONObject obj = (JSONObject) new JSONTokener(message)
						.nextValue();
				if (obj.getString(ModelParser.TYPE_KEY).equals(
						ModelParser.MOVE_TYPE_VAL)) {
					// Process Move data
					processMoveOnBoard(
							obj.getInt(ModelParser.MOVE_XPOS_KEY),
							obj.getInt(ModelParser.MOVE_YPOS_KEY), false);

					Ship playerShip = mPlayerBoard.getShipAtIndex(
							obj.getInt(ModelParser.MOVE_XPOS_KEY),
							obj.getInt(ModelParser.MOVE_YPOS_KEY));
					if (playerShip != null)
						playerShip.isSunk();

					if (mPlayerBoard.isAllSunk())
						win(false);
					Toast.makeText(
							mContext,
							"Move received: "
									+ obj.getInt(ModelParser.MOVE_XPOS_KEY)
									+ ", "
									+ obj.getInt(ModelParser.MOVE_YPOS_KEY),
							Toast.LENGTH_SHORT).show();
					
					if (mState != GameState.GAME_OVER_LOSS
							&& mState != GameState.GAME_OVER_WIN)
						setState(GameState.TAKING_TURN);

				} else if (obj.getString(ModelParser.TYPE_KEY).equals(
						ModelParser.BOARD_TYPE_VAL)) {
					// Receiving Opponent's board data
					JSONArray shipArr = obj
							.getJSONArray(ModelParser.BOARD_TYPE_SHIPS_KEY);
					Toast.makeText(mContext, "Received game board",
							Toast.LENGTH_SHORT).show();
					hasReceivedOpponentBoard = true;

					for (int i = 0; i < shipArr.length(); i++) {
						JSONObject ship = (JSONObject) shipArr.get(i);
						mOpponentBoard
								.setShip(
										ship.getInt(ModelParser.SHIP_XPOS_KEY),
										ship.getInt(ModelParser.SHIP_YPOS_KEY),
										ship.getBoolean(ModelParser.SHIP_HORIZ_KEY),
										ModelParser.getShipTypeFromString(ship
												.getString(ModelParser.SHIP_TYPE_TYPE_KEY)));
					}
					
					if (mState == GameState.WAITING_FOR_OPPONENT && willYieldTurn == false) {
						setState(GameState.TAKING_TURN);
					}
				} else if (obj.getString(ModelParser.TYPE_KEY).equals(
						ModelParser.YIELD_TURN_TYPE_VAL)) {
					// The guest has been told by host that it gets to move
					// first.
					if (mState == GameState.PLACING_SHIPS) {
						willYieldTurn = false;
					} else {
						setState(GameState.TAKING_TURN);
					}
				} else if (obj.getString(ModelParser.TYPE_KEY).equals(
						ModelParser.GAME_OVER_TYPE_VAL)) {
					boolean youWin = obj
							.getBoolean(ModelParser.GAME_OVER_WIN_KEY);
					win(youWin);
				} else if (obj.getString(ModelParser.TYPE_KEY).equals(
						ModelParser.PROFILE_TYPE_VAL)) {
					// The opponent has sent its profile data
					mOpponentProfileName = obj
							.getString(ModelParser.PROFILE_NAME_KEY);

					// Update history
					if (!isHost) {
						String ip = NetworkManager.getInstance()
								.getAndroidHostIP();
						ConnectionHistoryRepository.updateNameforItem(ip,
								mOpponentProfileName);
					} else {
						NIOS2NetworkManager.sendProfileName(false,
								mOpponentProfileName);
					}

					mOpponentProfileTaunt = obj
							.getString(ModelParser.PROFILE_TAUNT_KEY);
					String imgString = obj
							.getString(ModelParser.PROFILE_IMAGE_KEY);
					mOpponentProfileImage = (imgString != null) ? BitmapUtils
							.decodeBase64(imgString) : null;
					if (mProfileDataListener != null) {
						mProfileDataListener.onProfileDataReceived(
								mOpponentProfileName, mOpponentProfileTaunt,
								mOpponentProfileImage);
					}
				}
			} catch (JSONException e) {
				Log.e(TAG, "Error getting json object from json string");
			}
		}
	}

	/**
	 * Processes a move attempt taken on the board Sends message to nios
	 * appropriately
	 * 
	 * @param x
	 * @param y
	 * @return true if the move hit, false if missed
	 */
	private boolean processMoveOnBoard(int x, int y, boolean isPlayerMove) {
		Board board;
		if (isPlayerMove)
			board = mOpponentBoard;
		else
			board = mPlayerBoard;

		mGameStarted = true;
		
		boolean wasHit = board.playerShotAttempt(x, y);

		Ship target = board.getShipAtIndex(x, y);
		boolean sunk = false;

		if (target != null)
			sunk = target.isSunk();

		if (sunk) {
			mSoundManager.playSFX(R.raw.ship_explode);
			if(isHost)
				NIOS2NetworkManager.sendHit(isPlayerMove, x, y);
		} else if (wasHit) {
			mSoundManager.playSFX(R.raw.hit);
			if(isHost)
				NIOS2NetworkManager.sendHit(isPlayerMove, x, y);
		} else {
			mSoundManager.playSFX(R.raw.miss);
			if(isHost)
				NIOS2NetworkManager.sendMiss(isPlayerMove, x, y);
		}

		mFireTileX = board.getTileLocationAtIndex(x, y)[0];
		mFireTileY = board.getTileLocationAtIndex(x, y)[1];

		return wasHit;
	}

	public static interface GameStateChangedListener {
		public void onGameStateChanged();
	}

	private void setState(GameState state) {
		mState = state;
		if (mStateListener != null)
			mStateListener.onGameStateChanged();

		if (!isMultiplayer() && state == GameState.WAITING_FOR_OPPONENT) {
			// The AI needs to go after the board has shifted
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {

				@Override
				public void run() {
					performAIMove();
					if (mState != GameState.GAME_OVER_LOSS
							&& mState != GameState.GAME_OVER_WIN)
						setState(GameState.TAKING_TURN);
				}
			}, GameActivity.BOARD_TRANS_ANIM_DURATION
					+ GameActivity.SMOKE_ANIM_DURATION + 100);
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
		} while (pos != null
				&& mPlayerBoard.getTileColour(pos.x, pos.y) != Board.TILE_COLOR_NORMAL);

		if (pos != null) {
			Ship target = mPlayerBoard.getShipAtIndex(pos.x, pos.y);
			boolean hit = processMoveOnBoard(pos.x, pos.y, false);
			boolean sunk = false;

			// Give AI the ship if the ship is sunk,
			// The coordinates of the sunken ship is public information
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

	public static interface ProfileDataReceivedListener {
		public void onProfileDataReceived(String name, String taunt,
				Bitmap image);
	}

	public void setProfileDataReveivedListener(
			ProfileDataReceivedListener listener) {
		mProfileDataListener = listener;
		if (mProfileDataListener != null
				&& (mOpponentProfileName != null
						|| mOpponentProfileTaunt != null || mOpponentProfileImage != null)) {
			mProfileDataListener.onProfileDataReceived(mOpponentProfileName,
					mOpponentProfileTaunt, mOpponentProfileImage);
		}
	}

	public Bitmap getOpponentImage() {
		return mOpponentProfileImage;
	}

	public SoundManager getSoundManager() {
		return mSoundManager;
	}
}