package com.group10.battleship;

import java.io.IOException;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.group10.battleship.audio.MusicManager;
import com.group10.battleship.audio.MusicManager.Music;
import com.group10.battleship.game.Game;
import com.group10.battleship.game.Game.GameState;
import com.group10.battleship.game.Game.GameStateChangedListener;
import com.group10.battleship.game.Game.ProfileDataReceivedListener;
import com.group10.battleship.graphics.BitmapUtils;
import com.group10.battleship.graphics.GL20Renderer;

/**
 * http://www.learnopengles.com/android-lesson-one-getting-started/
 * 
 */
public class GameActivity extends SherlockActivity implements OnTouchListener,
		AnimationListener, GameStateChangedListener, ProfileDataReceivedListener {

	private static final String TAG = GameActivity.class.getSimpleName();
	
	public static final int BOARD_TRANS_ANIM_DURATION = 500;

	private GLSurfaceView mGLSurfaceView;
	private GL20Renderer mGLRenderer;

	private Handler mHustleHandler = new Handler();
	private Runnable mHustleRunnable = new hustleRunnable();
	
	private RelativeLayout mBannerAd;
	
	private ImageView mOpponentImage;
	private TextView mOpponentName;
	private TextView mOpponentTaunt;
	
	private ImageView mCurrentTurnImage;
	private Bitmap mPlayerProfileBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		mOpponentName = (TextView) findViewById(R.id.opponent_vs_name);
		mOpponentTaunt = (TextView) findViewById(R.id.opponent_vs_taunt_text);
		mOpponentImage = (ImageView) findViewById(R.id.opponent_profile_image);
		mCurrentTurnImage = (ImageView) findViewById(R.id.player_turn_img);
		
		Log.d(TAG, "onCreate");
		mGLSurfaceView = (GLSurfaceView) findViewById(R.id.glsv_game_view);

		// Check if the system supports OpenGL ES 2.0.
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		ConfigurationInfo configurationInfo = activityManager
				.getDeviceConfigurationInfo();
		boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (!supportsEs2) {
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			throw new RuntimeException(
					"Device not compatible with Open GL ES 2.0");
		}

		// Request an OpenGL ES 2.0 compatible context.
		mGLSurfaceView.setEGLContextClientVersion(2);

		// Set the renderer to our demo renderer, defined below.
		mGLRenderer = new GL20Renderer();
		mGLSurfaceView.setRenderer(mGLRenderer);
		mGLSurfaceView.setOnTouchListener(this);
		mGLRenderer.setAnimationListener(this);

		Game game = Game.getInstance();
		if (game.getState() == GameState.UNINITIALIZED) {
			// This should not happen in theory, since the game should be
			// started by the MainActivity
			finish();
		}

		game.configure(this, mGLRenderer);
		game.setGameStateListener(this);
		game.setProfileDataReveivedListener(this);

		supportInvalidateOptionsMenu();
		MusicManager.getInstance().stop(Music.MENU);
		MusicManager.getInstance().play(Music.GAME);
		
		// Set the user's profile image
		String profileImgUriStr = PrefsManager.getInstance().getString(PrefsManager.KEY_PROFILE_IMAGE_URI, null);
		if (profileImgUriStr != null) {
			try {
				mPlayerProfileBitmap = BitmapUtils.decodeSampledBitmapFromUri(Uri.parse(profileImgUriStr), 100, 100);
			} catch (IOException e) {
				e.printStackTrace();
				mPlayerProfileBitmap = null;
			}
		} else {
			mPlayerProfileBitmap = null;
		}
		
		Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
		final Animation slideDown = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down);
		mBannerAd = (RelativeLayout) findViewById(R.id.banner_ad_layout);
		ImageButton imgButton = (ImageButton) findViewById(R.id.close_ad_button);
		mBannerAd.startAnimation(slideUp);
		imgButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBannerAd.startAnimation(slideDown);
				mBannerAd.setVisibility(View.INVISIBLE);
			}
		});
	}

	@Override
	public void onPause() {
		Log.d(TAG, "onPause");
		stopHustling();
		MusicManager.getInstance().pause();
		mGLSurfaceView.onPause();
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		refreshOptionsMenu();
		mGLSurfaceView.onResume();
		MusicManager.getInstance().resume();

		if (Game.getInstance().getState() == GameState.TAKING_TURN)
			initiateHustling();

		if(mBannerAd.getVisibility() == View.INVISIBLE)
		{
			Animation slideUp = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up);
			mBannerAd.setVisibility(View.VISIBLE);
			mBannerAd.startAnimation(slideUp);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getSupportMenuInflater().inflate(R.menu.game, menu);
		GameState state = Game.getInstance().getState();

		MenuItem mi;
		for (int i = 0; i < menu.size(); i++) {
			mi = menu.getItem(i);
			if (mi.getItemId() == R.id.switch_boards_item
					&& mGLRenderer != null) {
				if (mGLRenderer.getCamPosY() > 1.0f) {
					mi.setIcon(R.drawable.ic_find_next_holo_light);
					mi.setTitle(R.string.menu_item_goto_pboard);
				} else {
					mi.setIcon(R.drawable.ic_find_previous_holo_light);
					mi.setTitle(R.string.menu_item_goto_oboard);
				}
			} else if (mi.getItemId() == R.id.confirm_item) {
				if (state == GameState.PLACING_SHIPS) {
					mi.setVisible(true);
				} else {
					mi.setVisible(false);
				}
			} else if (mi.getItemId() == R.id.fire_item) {
				if (state == GameState.TAKING_TURN) {
					mi.setVisible(true);
				} else {
					mi.setVisible(false);
				}
			} else if (mi.getItemId() == R.id.rotate_item) {
				if (state == GameState.PLACING_SHIPS) {
					mi.setVisible(true);
				} else {
					mi.setVisible(false);
				}
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.switch_boards_item) {
			if (mGLRenderer.getCamPosY() > 1.0f) {
				mGLRenderer.translateCamWithAnimation(0f, 0f, BOARD_TRANS_ANIM_DURATION);
			} else {
				mGLRenderer.translateCamWithAnimation(0f, 2.0f, BOARD_TRANS_ANIM_DURATION);
			}
		} else if (item.getItemId() == R.id.rotate_item) {
			Game.getInstance().onRotateButtonPressed();
		} else if (item.getItemId() == R.id.fire_item) {
			Game.getInstance().onFireButtonPressed();
		} else if (item.getItemId() == R.id.confirm_item) {
			Game.getInstance().onConfirmBoardPressed();
		} else if (item.getItemId() == R.id.quit_item) {
			showExitConfirmationDialog();
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		showExitConfirmationDialog();
	}

	@Override
	public boolean onTouch(View view, MotionEvent me) {

		// Calculate the touch event in terms of the GL surface
		float x = me.getX() / mGLSurfaceView.getWidth();
		float glx = mGLRenderer.getRight() - mGLRenderer.getLeft();
		x = mGLRenderer.getLeft() + (x * glx);

		float y = me.getY() / mGLSurfaceView.getHeight();
		float gly = mGLRenderer.getTop() - mGLRenderer.getBottom();
		y = mGLRenderer.getTop() - (y * gly);

		Game.getInstance().onTouchGLSurface(me, x, y);
		return true;
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		refreshOptionsMenu();
	}

	private void refreshOptionsMenu() {
		Runnable r = new Runnable() {
			@Override
			public void run() {
				GameActivity.this.supportInvalidateOptionsMenu();
			}
		};
		runOnUiThread(r);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {
	}

	@Override
	public void onAnimationStart(Animation arg0) {
	}

	@Override
	public void onGameStateChanged() {
		refreshOptionsMenu();
		if (Game.getInstance().getState() == GameState.TAKING_TURN) {
			mGLRenderer.translateCamWithAnimation(0f, 2.0f, BOARD_TRANS_ANIM_DURATION);
			initiateHustling();
		} else if (Game.getInstance().getState() == GameState.WAITING_FOR_OPPONENT) {
			Log.d("", "waiting for opponent");
			mGLRenderer.translateCamWithAnimation(0f, 0f, BOARD_TRANS_ANIM_DURATION);
			stopHustling();
		} else if (Game.getInstance().getState() == GameState.GAME_OVER_WIN) {
			showGameoverDialog(true);
		} else if (Game.getInstance().getState() == GameState.GAME_OVER_LOSS) {
			showGameoverDialog(false);
		}
		
		// Setting turn image
		Bitmap bm;
		if (Game.getInstance().getState() == GameState.WAITING_FOR_OPPONENT) {
			bm = Game.getInstance().getOpponentImage();
		} else {
			bm = mPlayerProfileBitmap;
		}
		
		if (bm != null) {
			mCurrentTurnImage.setImageBitmap(bm);
		} else {
			mCurrentTurnImage.setImageResource(R.drawable.profile_img_placeholder);
		}
	}

	private void initiateHustling() {
		// allow player to decide for 30 seconds before hustling
		mHustleHandler.postDelayed(mHustleRunnable, 30000);
	}

	private void stopHustling() {
		mHustleHandler.removeCallbacks(mHustleRunnable);
		MusicManager.getInstance().stop(Music.THINKING);
		MusicManager.getInstance().play(Music.GAME);
	}

	private void showGameoverDialog(boolean won) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

		if (won)
			dialogBuilder.setMessage(R.string.dialog_win_message);
		else
			dialogBuilder.setMessage(R.string.dialog_loss_message);
		dialogBuilder.setNegativeButton(R.string.dialog_cancel, null);
		final boolean didWin = won;
		dialogBuilder.setNeutralButton("Share", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent.putExtra(Intent.EXTRA_TEXT, 
						(didWin?getString(R.string.dialog_win_message_p1):
							getString(R.string.dialog_loss_message_p1)) + "opponentName" + getString(R.string.dialog_game_over_message_p2));
				shareIntent.setType("text/plain"); 
				startActivity(Intent.createChooser(shareIntent, "Share your result via..."));
			}
		});
		dialogBuilder.setPositiveButton(R.string.dialog_confirm,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Game.getInstance().forfeit();
						GameActivity.this.finish();
					}
				});
		dialogBuilder.show();
	}

	private class hustleRunnable implements Runnable {

		@Override
		public void run() {
			MusicManager.getInstance().stop(Music.GAME);
			MusicManager.getInstance().play(Music.THINKING);
		}
	}

	private void showExitConfirmationDialog() {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setMessage(R.string.dialog_quit_conf_message);
		dialogBuilder.setNegativeButton(R.string.dialog_cancel, null);
		dialogBuilder.setPositiveButton(R.string.dialog_confirm,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Game.getInstance().forfeit();
						GameActivity.this.finish();
					}
				});
		dialogBuilder.show();
	}

	@Override
	public void onProfileDataReceived(String name, String taunt, Bitmap image) {
		if (name != null) {
			mOpponentName.setText(name);
		} else {
			mOpponentName.setText(R.string.game_vs_bar_opponent_name_placeholder);
		}
		
		if (taunt != null) {
			mOpponentTaunt.setText(taunt);
		} else {
			mOpponentTaunt.setText(R.string.game_vs_bar_opponent_taunt_placeholder);
		}
		
		setProfileImage(mOpponentImage, image);
		
		if (Game.getInstance().getState() == GameState.WAITING_FOR_OPPONENT) {		
			setProfileImage(mCurrentTurnImage, image);
		} else {
			setProfileImage(mCurrentTurnImage, mPlayerProfileBitmap);
		}
	}
	
	private void setProfileImage(ImageView iv, Bitmap bm) {
		if (bm != null) {
			iv.setImageBitmap(bm);
		} else {
			iv.setImageResource(R.drawable.profile_img_placeholder);
		}
	}
}
