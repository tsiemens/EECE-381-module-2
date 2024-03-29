package com.group10.battleship;

import java.io.IOException;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.group10.battleship.audio.MusicManager;
import com.group10.battleship.audio.SoundManager;
import com.group10.battleship.audio.MusicManager.Music;
import com.group10.battleship.game.Game;
import com.group10.battleship.game.Game.GameState;
import com.group10.battleship.game.Game.GameStateChangedListener;
import com.group10.battleship.game.Game.ProfileDataReceivedListener;
import com.group10.battleship.graphics.BitmapUtils;
import com.group10.battleship.graphics.GL20Renderer;
import com.group10.battleship.graphics.GifAnimation;

/**
 * http://www.learnopengles.com/android-lesson-one-getting-started/
 * 
 */
public class GameActivity extends Activity implements OnTouchListener,
AnimationListener, GameStateChangedListener,
ProfileDataReceivedListener {

	private static final String TAG = GameActivity.class.getSimpleName();

	public static final int BOARD_TRANS_ANIM_DURATION = 500;
	public static final int SMOKE_ANIM_DURATION = 1000;

	private GLSurfaceView mGLSurfaceView;
	private GL20Renderer mGLRenderer;

	private Handler mHustleHandler = new Handler();
	private Runnable mHustleRunnable = new HustleRunnable();

	private GifAnimation mSmokeView;
	private Handler mHideSmokeHandler = new Handler();
	private Runnable mHideSmokeRunnable = new HideSmokeRunnable();

	private int mSmokeSizeX = (int) (500 / 2.50);
	private int mSmokeSizeY = (int) (667 / 3.50);

	private RelativeLayout mBannerAd;

	private ImageView mOpponentImage;
	private TextView mOpponentName;
	private TextView mOpponentTaunt;

	private RelativeLayout mPlayerHelpOverlay;
	private RelativeLayout mEnemyHelpOverlay;

	private ImageView mCurrentTurnImage;
	private Bitmap mPlayerProfileBitmap;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSmokeView = new GifAnimation(this, "explosion.gif", mSmokeSizeX,
				mSmokeSizeY);
		FrameLayout rl = (FrameLayout) LayoutInflater.from(this).inflate(
				R.layout.activity_game, null);
		rl.addView(mSmokeView.getView());
		mSmokeView.getView().setVisibility(View.INVISIBLE);

		setContentView(rl);

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

		invalidateOptionsMenu();
		MusicManager.getInstance().stop(Music.MENU);
		MusicManager.getInstance().play(Music.GAME);

		// Set the user's profile image
		String profileImgUriStr = PrefsManager.getInstance().getString(
				PrefsManager.KEY_PROFILE_IMAGE_URI, null);
		if (profileImgUriStr != null) {
			try {
				mPlayerProfileBitmap = BitmapUtils.decodeSampledBitmapFromUri(
						Uri.parse(profileImgUriStr), 100, 100);
			} catch (IOException e) {
				e.printStackTrace();
				mPlayerProfileBitmap = null;
			}
		} else {
			mPlayerProfileBitmap = null;
		}

		// Set up overlays
		Typeface tf = Typeface.createFromAsset(getAssets(),
				"fonts/BuxtonSketch.ttf");
		TextView doneText = (TextView) findViewById(R.id.text_done);
		TextView reposText = (TextView) findViewById(R.id.text_move_ships);
		TextView rotateText = (TextView) findViewById(R.id.text_rotate);
		TextView seeOtherText = (TextView) findViewById(R.id.text_seeOther);
		TextView fireText = (TextView) findViewById(R.id.text_fire);
		TextView chooseText = (TextView) findViewById(R.id.text_choose_tile);
		reposText.setTypeface(tf);
		rotateText.setTypeface(tf);
		doneText.setTypeface(tf);
		seeOtherText.setTypeface(tf);
		fireText.setTypeface(tf);
		chooseText.setTypeface(tf);

		mPlayerHelpOverlay = (RelativeLayout) findViewById(R.id.help_overlay_player);
		mPlayerHelpOverlay.setOnTouchListener(this);
		if(!PrefsManager.getInstance().getBoolean(PrefsManager.KEY_HAS_RUN_BEFORE, false))
			mPlayerHelpOverlay.setVisibility(View.VISIBLE);
		else 
			mPlayerHelpOverlay.setVisibility(View.INVISIBLE);
		mEnemyHelpOverlay = (RelativeLayout) findViewById(R.id.help_overlay_enemy);
		mEnemyHelpOverlay.setVisibility(View.INVISIBLE);
		mEnemyHelpOverlay.setOnTouchListener(this);

		// Set up banner ad
		Animation slideUp = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.slide_up);
		final Animation slideDown = AnimationUtils.loadAnimation(
				getApplicationContext(), R.anim.slide_down);
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

		if (mBannerAd.getVisibility() == View.INVISIBLE) {
			Animation slideUp = AnimationUtils.loadAnimation(
					getApplicationContext(), R.anim.slide_up);
			mBannerAd.setVisibility(View.VISIBLE);
			mBannerAd.startAnimation(slideUp);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		GameState state = Game.getInstance().getState();

		MenuItem mi;
		for (int i = 0; i < menu.size(); i++) {
			mi = menu.getItem(i);
			if (mi.getItemId() == R.id.switch_boards_item
					&& mGLRenderer != null) {
				if (state == GameState.PLACING_SHIPS)
					mi.setVisible(false);
				else {
					mi.setVisible(true);
					if (mGLRenderer.getCamPosY() > 1.0f) {
						mi.setIcon(R.drawable.ic_find_next_holo_light);
						mi.setTitle(R.string.menu_item_goto_pboard);
					} else {
						mi.setIcon(R.drawable.ic_find_previous_holo_light);
						mi.setTitle(R.string.menu_item_goto_oboard);
					}
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
			} else if(mi.getItemId() == R.id.mute_music_item) {
				if(MusicManager.getInstance().isMuted())
					mi.setTitle(R.string.menu_item_music_unmute);
				else 
					mi.setTitle(R.string.menu_item_music_mute);
			}
			else if(mi.getItemId() == R.id.mute_sound_item) {
				if(Game.getInstance().getSoundManager().isMuted())
					mi.setTitle(R.string.menu_item_sound_unmute);
				else 
					mi.setTitle(R.string.menu_item_sound_mute);
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.switch_boards_item) {
			hideHelpOverlayIfVisible();
			if (mGLRenderer.getCamPosY() > 1.0f) {
				mGLRenderer.translateCamWithAnimation(0f, 0f, 500);
			} else {
				mGLRenderer.translateCamWithAnimation(0f, 2.0f, 500);
				if (!PrefsManager.getInstance().getBoolean(
						PrefsManager.KEY_HAS_RUN_BEFORE, false))
					mEnemyHelpOverlay.setVisibility(View.VISIBLE);
			}
		} else if (item.getItemId() == R.id.rotate_item) {
			Game.getInstance().onRotateButtonPressed();
		} else if (item.getItemId() == R.id.fire_item) {
			hideHelpOverlayIfVisible();
			Game.getInstance().onFireButtonPressed();
		} else if (item.getItemId() == R.id.confirm_item) {
			hideHelpOverlayIfVisible();
			Game.getInstance().onConfirmBoardPressed();
		} else if(item.getItemId() == R.id.mute_sound_item) {
			if(Game.getInstance().getSoundManager().isMuted())
				Game.getInstance().getSoundManager().unmute();
			else 
				Game.getInstance().getSoundManager().mute();
			invalidateOptionsMenu();

		} else if(item.getItemId() == R.id.mute_music_item) {
			if(MusicManager.getInstance().isMuted())
				MusicManager.getInstance().unmute();
			else 
				MusicManager.getInstance().mute();
			invalidateOptionsMenu();

		} else if (item.getItemId() == R.id.show_help_item) {

			GameState state = Game.getInstance().getState();

			if (state == Game.GameState.PLACING_SHIPS) {
				if (mPlayerHelpOverlay.getVisibility() == View.INVISIBLE)
					mPlayerHelpOverlay.setVisibility(View.VISIBLE);
			}
			else if(state == Game.GameState.WAITING_FOR_OPPONENT || state == Game.GameState.GAME_OVER_LOSS || state == Game.GameState.GAME_OVER_WIN)
			{
				if(Game.getInstance().isMultiplayer())
				{
					TextView fireText = (TextView)findViewById(R.id.text_fire);
					ImageView fireArr = (ImageView)findViewById(R.id.arrow_fire);
					TextView chooseText = (TextView) findViewById(R.id.text_choose_tile);
					chooseText.setVisibility(View.GONE);
					fireText.setVisibility(View.GONE);
					fireArr.setVisibility(View.GONE);
				}
				mEnemyHelpOverlay.setVisibility(View.VISIBLE);
			}			
			else if (state == Game.GameState.TAKING_TURN) {
				TextView fireText = (TextView)findViewById(R.id.text_fire);
				ImageView fireArr = (ImageView)findViewById(R.id.arrow_fire);
				TextView chooseText = (TextView) findViewById(R.id.text_choose_tile);
				chooseText.setVisibility(View.VISIBLE);
				fireText.setVisibility(View.VISIBLE);
				fireArr.setVisibility(View.VISIBLE);
				if (mEnemyHelpOverlay.getVisibility() == View.INVISIBLE)
					mEnemyHelpOverlay.setVisibility(View.VISIBLE);
			} 
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
		hideHelpOverlayIfVisible();
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
				GameActivity.this.invalidateOptionsMenu();
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
			smokeAnimation();
			initiateHustling();

			if (!PrefsManager.getInstance().getBoolean(
					PrefsManager.KEY_HAS_RUN_BEFORE, false)
					&& mGLRenderer.getCamPosY() > 1.0f)
				mEnemyHelpOverlay.setVisibility(View.VISIBLE);
		} else if (Game.getInstance().getState() == GameState.WAITING_FOR_OPPONENT) {
			Log.d("", "waiting for opponent");
			smokeAnimation();
			stopHustling();
		} else if (Game.getInstance().getState() == GameState.GAME_OVER_WIN) {
			showGameoverDialog(true);
			stopHustling();
		} else if (Game.getInstance().getState() == GameState.GAME_OVER_LOSS) {
			showGameoverDialog(false);
			stopHustling();
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
			mCurrentTurnImage
			.setImageResource(R.drawable.profile_img_placeholder);
		}
	}

	private void smokeAnimation() {

		if (Game.getInstance().isGameStarted()) {
			float yOffset = mGLSurfaceView.getY();

			float glx = mGLRenderer.getRight() - mGLRenderer.getLeft();
			int x = (int) (mGLSurfaceView.getWidth() / glx * (Game
					.getInstance().getFirePosition()[0] - mGLRenderer.getLeft()));

			float gly = mGLRenderer.getTop() - mGLRenderer.getBottom();
			int y = (int) (mGLSurfaceView.getHeight() / gly * (mGLRenderer
					.getTop() - Game.getInstance().getFirePosition()[1]));

			x = x - mSmokeSizeX;
			y = (int) (y - mSmokeSizeY + yOffset);

			mSmokeView.show((int) (x + mSmokeSizeX * 0.2), y, mSmokeSizeX);
			mSmokeView.getView().setVisibility(View.VISIBLE);
			mHideSmokeHandler.postDelayed(mHideSmokeRunnable,
					SMOKE_ANIM_DURATION);
		} else {
			mHideSmokeRunnable.run();
		}
	}

	private class HideSmokeRunnable implements Runnable {
		@Override
		public void run() {
			mSmokeView.getView().setVisibility(View.GONE);
			mSmokeView.clear();
			if (Game.getInstance().getState() == GameState.TAKING_TURN)
				mGLRenderer.translateCamWithAnimation(0f, 2.0f,
						BOARD_TRANS_ANIM_DURATION);
			else
				mGLRenderer.translateCamWithAnimation(0f, 0f,
						BOARD_TRANS_ANIM_DURATION);
		}
	}

	private void hideHelpOverlayIfVisible() {
		if (mPlayerHelpOverlay.getVisibility() == View.VISIBLE)
			mPlayerHelpOverlay.setVisibility(View.INVISIBLE);
		if (mEnemyHelpOverlay.getVisibility() == View.VISIBLE) {
			mEnemyHelpOverlay.setVisibility(View.INVISIBLE);
			if (!PrefsManager.getInstance().getBoolean(
					PrefsManager.KEY_HAS_RUN_BEFORE, false))
				PrefsManager.getInstance().putBoolean(
						PrefsManager.KEY_HAS_RUN_BEFORE, true);
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
		if (this.isFinishing())
			return;
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		LayoutInflater inflator = this.getLayoutInflater();
		View view = inflator.inflate(R.layout.dialog_game_over, null);
		dialogBuilder.setView(view);
		dialogBuilder.setNegativeButton(R.string.dialog_cancel, null);
		final boolean didWin = won;
		dialogBuilder.setNeutralButton("Share",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				Intent shareIntent = new Intent(Intent.ACTION_SEND);
				shareIntent
				.putExtra(
						Intent.EXTRA_TEXT,
						(didWin ? getString(R.string.dialog_win_message_p1)
								: getString(R.string.dialog_loss_message_p1))
								+ mOpponentName.getText()
								+ getString(R.string.dialog_game_over_message_p2));
				// TODO: can also add #taunt
				shareIntent.setType("text/plain");
				startActivity(Intent.createChooser(shareIntent,
						"Share your result via..."));
			}
		});
		dialogBuilder.setPositiveButton(R.string.dialog_confirm,
				new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				GameActivity.this.finish();
				Game.getInstance().invalidate();
			}
		});
		ImageView iv = (ImageView) view
				.findViewById(R.id.game_over_dialog_image);
		if (iv != null) {
			if (won)
				iv.setImageResource(R.drawable.dialog_img_won);
			else
				iv.setImageResource(R.drawable.dialog_img_lost);
		}
		dialogBuilder.show();
	}

	private class HustleRunnable implements Runnable {

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
			mOpponentName
			.setText(R.string.game_vs_bar_opponent_name_placeholder);
		}

		if (taunt != null) {
			mOpponentTaunt.setText(taunt);
		} else {
			mOpponentTaunt
			.setText(R.string.game_vs_bar_opponent_taunt_placeholder);
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
