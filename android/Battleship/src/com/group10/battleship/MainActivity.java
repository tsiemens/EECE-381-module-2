package com.group10.battleship;

import java.util.List;

import com.group10.battleship.PrefsActivity;
import com.group10.battleship.audio.MusicManager;
import com.group10.battleship.audio.MusicManager.Music;
import com.group10.battleship.database.ConnectionHistoryRepository;
import com.group10.battleship.database.ConnectionHistoryRepository.HistoryItem;
import com.group10.battleship.game.Game;
import com.group10.battleship.game.Game.GameState;
import com.group10.battleship.network.NIOS2NetworkManager;
import com.group10.battleship.network.NetworkManager;
import com.group10.battleship.network.NetworkManager.OnAndroidSocketSetupListener;
import com.group10.battleship.network.NetworkManager.OnNiosSocketSetupListener;
import com.group10.battleship.network.UDPManager;
import com.group10.battleship.network.UDPManager.OnBroadcastFoundListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;

public class MainActivity extends Activity implements OnClickListener, OnAndroidSocketSetupListener, OnNiosSocketSetupListener, OnBroadcastFoundListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private static final int DEFAULT_PORT = 50002;

	private Button mStartGameBtn;

	private UDPManager mUDPManager;
	private Button mProfileButton;

	private Button mHostButton; 
	private Button mGuestButton; 
	private Button mSinglePlayerButton; 
	private int mSelectedModeId;

	private String mHostIp;

	private List<HistoryItem> mHistoryItems;

	private ImageView mLogo;
	private View mFadeOut;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mStartGameBtn = (Button) findViewById(R.id.btn_start_game);
		mStartGameBtn.setOnClickListener(this);
		mProfileButton = (Button) findViewById(R.id.btn_view_profile);
		mProfileButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				startActivity(new Intent(MainActivity.this, ProfileActivity.class));
			}
		});

		mUDPManager = new UDPManager(); 
		mUDPManager.setOnBroadcastFoundListener(this);

		mHostButton = (Button) findViewById(R.id.rb_host);
		mHostButton.setOnClickListener(this);
		mGuestButton = (Button) findViewById(R.id.rb_guest);
		mGuestButton.setOnClickListener(this);
		mSinglePlayerButton = (Button) findViewById(R.id.rb_single);
		mSinglePlayerButton.setOnClickListener(this);
		mSelectedModeId = R.id.rb_host;
		mHostButton.setSelected(true);
		mGuestButton.setSelected(false);
		mSinglePlayerButton.setSelected(false);

		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		mLogo = (ImageView) findViewById(R.id.logo);
		mLogo.bringToFront();
		mFadeOut = (View) findViewById(R.id.fade_view);		
		performLaunchAnimation();
	}

	@Override
	public void onResume() {
		super.onResume();

		MusicManager.getInstance().play(Music.MENU);

		if (PrefsManager.getInstance().getString(PrefsManager.KEY_PROFILE_NAME, null) == null) {
			startActivity(new Intent(this, ProfileActivity.class));
		}
	}

	@Override
	public void onPause() {
		MusicManager.getInstance().pause();
		super.onPause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.app_settings) {
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private void performRadioButtonLikeActions(int id)
	{
		if(id == R.id.rb_host)
		{
			mHostButton.setSelected(true);
			mGuestButton.setSelected(false);
			mSinglePlayerButton.setSelected(false);
		}
		else if(id == R.id.rb_guest)
		{
			mHostButton.setSelected(false);
			mGuestButton.setSelected(true);
			mSinglePlayerButton.setSelected(false);
		}
		else if(id == R.id.rb_single)
		{
			mHostButton.setSelected(false);
			mGuestButton.setSelected(false);
			mSinglePlayerButton.setSelected(true);
		}
		mSelectedModeId = id;
	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if( id != R.id.btn_start_game)
			performRadioButtonLikeActions(id);

		if( id == R.id.btn_start_game)
		{
			if (mSelectedModeId == R.id.rb_single) 
			{
				Game game = Game.getInstance();
				if (game.getState() != GameState.UNINITIALIZED) {
					// Game was in progress before, so we have to restart it.
					game.invalidate();
				}
				showLevelSelectDialog();
			}
			else if (mSelectedModeId == R.id.rb_host)
			{
				mUDPManager.new SendBroadcast().execute(null, null, null);

				Toast.makeText(this, "Starting game...", Toast.LENGTH_SHORT).show();
				try {
					NetworkManager.getInstance().setupAndroidSocket(null, 0, true);
					NetworkManager.getInstance().setOnAndroidSocketSetupListener(this);			
					setUpNiosSocket();
				} catch (Exception e) {
					handleSocketError(e);
					e.printStackTrace();
				} 
			}
			else if (mSelectedModeId == R.id.rb_guest){
				showGuestDialog(null);
			}
		}
	}

	private void setUpNiosSocket()
	{
		PrefsManager pm = PrefsManager.getInstance();
		// If using nios, set up the nios 
		if (pm.getBoolean(PrefsManager.KEY_USE_NIOS, true))
		{
			NetworkManager.getInstance().close();
			String ip = pm.getString(PrefsManager.KEY_MM_IP, null);
			int port = pm.getInt(PrefsManager.KEY_MM_PORT, -1);
			Log.d(TAG, ip+":"+port);
			if (ip != null && port != -1 && ip.length() != 0) {
				try {
					NetworkManager.getInstance().setOnNiosSocketSetupListener(this);
					NetworkManager.getInstance().setupNiosSocket(ip, port);
				} catch (Exception e) {
					handleSocketError(e);
					e.printStackTrace();
				}
			}
			else
			{
				Toast.makeText(this, "NIOS IP & port not specified, not connected to NIOS", Toast.LENGTH_LONG).show();
			}
		}
	}

	// Show toast with error message & log the stack trace
	private void handleSocketError(Exception e)
	{
		Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		e.printStackTrace();
	}

	@Override
	public void onSuccessfulNiosSetup() {
		NIOS2NetworkManager.sendNewGame();
		Toast.makeText(this, "Successfully set up the Nios socket", Toast.LENGTH_LONG).show();
		Game.getInstance().onNiosGameStarted();
	}

	@Override
	public void onNiosSocketSetupError() {
		Toast.makeText(this, "Error: Could not find NIOS", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onFoundIPAddress(String ip, int port) {
		mHostIp = "IP: " + NetworkManager.getInstance().getAndroidHostIP() + ":"
				+ NetworkManager.getInstance().getAndroidHostPort() + "\nPress Back to Cancel!";

		ProgressDialog.show(this, "Waiting for Player...", mHostIp, true, true, new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				// TODO Auto-generated method stub
				Log.i("Info", "Cancel");

			}
		});
	}

	@Override
	public void onGameFound() {
		Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
		Game game = Game.getInstance();
		if (game.getState() != GameState.UNINITIALIZED) {
			// Game was in progress before, so we have to restart it.
			game.invalidate();
		}
		game.start(true);
		startActivity(new Intent(this, GameActivity.class));
	}

	@Override
	public void onAndroidSocketSetupError() {
		Toast.makeText(this, "Error: Could not make Android socket", Toast.LENGTH_LONG).show();
	}

	private void showGuestDialog(String ip) { 
		showGuestDialog(ip, Integer.toString(DEFAULT_PORT)); 
	}

	private void showGuestDialog(String ip, String port){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();

		// Inflate and set the layout for the dialog
		final View dialogView = inflater.inflate(R.layout.dialog_guest_findgame, null);
		// Inflate and set the layout for the dialog
		EditText iptext = (EditText)dialogView.findViewById(R.id.et_ip);
		iptext.setText(ip); 
		EditText portText = (EditText)dialogView.findViewById(R.id.et_port);
		portText.setText(port);
		builder.setView(dialogView)
		.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				Toast.makeText(MainActivity.this, "Finding game...", Toast.LENGTH_SHORT).show();
				try {
					String ip = ((EditText)dialogView.findViewById(R.id.et_ip)).getText().toString();
					int port = Integer.parseInt(((EditText)dialogView.findViewById(R.id.et_port)).getText().toString());
					Log.d(TAG, "should find game at "+ip+":"+port);
					NetworkManager.getInstance().setupAndroidSocket(ip, port, false); 
					NetworkManager.getInstance().setOnAndroidSocketSetupListener(MainActivity.this);
				} catch(NumberFormatException e)
				{
					Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
					e.printStackTrace();
				} catch (Exception e) {
					handleSocketError(e);
				}
			}
		})
		.setNeutralButton(R.string.dialog_history, new DialogInterface.OnClickListener() {				
			@Override
			public void onClick(DialogInterface dialog, int id) {
				showIPHistoryDialog();
			}
		})
		.setNegativeButton(R.string.dialog_scan, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				mUDPManager.new RecieveBroadcast().execute(null, null, null);
				Toast.makeText(getApplication(), "Scanning for a game...", Toast.LENGTH_LONG).show();
			}
		});
		builder.show();
	}

	private void showIPHistoryDialog() {
		mHistoryItems = ConnectionHistoryRepository.getSortedHistory();

		if (mHistoryItems.size() == 0) {
			Toast.makeText(this, "No history", Toast.LENGTH_SHORT).show();
			showGuestDialog(null);
			return;
		}

		String[] itemStrings = new String[mHistoryItems.size()];
		for (int i = 0; i < mHistoryItems.size(); i++) {
			itemStrings[i] = mHistoryItems.get(i).toString();
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.dialog_title_history)
		.setItems(itemStrings, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
				showGuestDialog(mHistoryItems.get(which).ip);
			}
		})
		.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showGuestDialog(null);
			}
		})
		.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				showGuestDialog(null);
			}
		});
		builder.show();
	}

	private void showLevelSelectDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setItems(R.array.dialog_level_select, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int selected) {
				setUpNiosSocket();
				Game.getInstance().setDifficulty(3 - selected);
				Game.getInstance().start(false);
				dialog.dismiss();
				startActivity(new Intent(MainActivity.this, GameActivity.class));
			}
		});
		builder.show();
	}

	public void onBroadcastFound() {
		Toast.makeText(getApplication(), "Found a game!", Toast.LENGTH_SHORT).show();
		this.showGuestDialog(mUDPManager.getIPString(), mUDPManager.getPortString());
	}

	public void performLaunchAnimation() {
		Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_drop);
		animation.setAnimationListener(new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {			
			}
			@Override
			public void onAnimationRepeat(Animation animation) {				
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
				animation2.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}					
					@Override
					public void onAnimationRepeat(Animation animation) {						
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						mFadeOut.setVisibility(View.GONE);						
					}
				});
				mFadeOut.startAnimation(animation2);
			}
		});

		mLogo.startAnimation(animation);
	}
}
