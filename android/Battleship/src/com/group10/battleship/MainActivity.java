package com.group10.battleship;


import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.Toast;


public class MainActivity extends SherlockActivity implements OnClickListener, OnAndroidSocketSetupListener, OnNiosSocketSetupListener, OnCheckedChangeListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private Button mStartGameBtn;
	private Button mProfileButton;
	
	private RadioGroup mGameModeGroup;
	
	private String mHostIp;
	
	private List<HistoryItem> mHistoryItems;

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

		mGameModeGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		mGameModeGroup.setOnCheckedChangeListener(this);
		
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
	}

	@Override
	public void onResume() {
		super.onResume();
		MusicManager.getInstance().play(Music.MENU);
		
		if (PrefsManager.getInstance().getString(PrefsManager.KEY_PROFILE, null) == null) {
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
		getSupportMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.app_settings) {
			startActivity(new Intent(this, PreferenceActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View view) {
		PrefsManager pm = PrefsManager.getInstance();
		if (mGameModeGroup.getCheckedRadioButtonId() == R.id.rb_single) {
			Game game = Game.getInstance();
			if (game.getState() != GameState.UNINITIALIZED) {
				// Game was in progress before, so we have to restart it.
				game.invalidate();
			}
			showLevelSelectDialog();
		}
		else if (mGameModeGroup.getCheckedRadioButtonId() == R.id.rb_host){
			Toast.makeText(this, "Starting game...", Toast.LENGTH_SHORT).show();
			try {
				NetworkManager.getInstance().setupAndroidSocket(null, 0, true);
				NetworkManager.getInstance().setOnAndroidSocketSetupListener(this);			
				// If using nios, also set up the nios 
				if (pm.getBoolean(PrefsManager.KEY_USE_NIOS, true))
				{
					String ip = pm.getString(PrefsManager.KEY_MM_IP, null);
					int port = pm.getInt(PrefsManager.KEY_MM_PORT, -1);
					Log.d(TAG, ip+":"+port);
					if (ip != null && port != -1) {
						NetworkManager.getInstance().setupNiosSocket(ip, port);
						NetworkManager.getInstance().setOnNiosSocketSetupListener(this);
					}
				}
			} catch (Exception e) {
				handleSocketError(e);
				e.printStackTrace();
			} 
		}
		else if (mGameModeGroup.getCheckedRadioButtonId() == R.id.rb_guest){
			showGuestDialog(null);
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
	}

	@Override
	public void onNiosSocketSetupError() {
		Toast.makeText(this, "Error: Could not find NIOS", Toast.LENGTH_LONG).show();
	}

	@Override
	public void onFoundIPAddress(String ip, int port) {
		mHostIp = "Waiting for player to join.\nIP: " + NetworkManager.getInstance().getAndroidHostIP() + ":"
				+ NetworkManager.getInstance().getAndroidHostPort();
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		
		builder.setNegativeButton(R.string.dialog_cancel, null)
		.setMessage(mHostIp);
		builder.show();
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

	@Override
	public void onCheckedChanged(RadioGroup parent, int checkedID) {
		// TODO Auto-generated method stub
		
	}
	
	private void showGuestDialog(String ip){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		LayoutInflater inflater = this.getLayoutInflater();

	    // Inflate and set the layout for the dialog
		final View dialogView = inflater.inflate(R.layout.dialog_guest_findgame, null);
		EditText iptext = (EditText)dialogView.findViewById(R.id.et_ip);
		iptext.setText(ip); 
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
				// TODO set level on selecting the choice and start the game
				Game.getInstance().start(false);
				dialog.dismiss();
				startActivity(new Intent(MainActivity.this, GameActivity.class));
			}
		});
		builder.show();
	}

}
