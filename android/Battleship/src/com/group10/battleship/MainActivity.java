package com.group10.battleship;

import java.io.IOException;
import java.net.UnknownHostException;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.group10.battleship.game.Game;
import com.group10.battleship.game.Game.GameState;
import com.group10.battleship.network.NIOS2NetworkManager;
import com.group10.battleship.network.NetworkManager;
import com.group10.battleship.network.NetworkManager.OnAndroidSocketSetupListener;
import com.group10.battleship.network.NetworkManager.OnNiosSocketSetupListener;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends SherlockActivity implements OnClickListener, OnAndroidSocketSetupListener, OnNiosSocketSetupListener, OnCheckedChangeListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private Button mStartGameBtn;
	
	private RadioGroup mGameModeGroup;
	
	private String mHostIp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mStartGameBtn = (Button) findViewById(R.id.btn_start_game);
		mStartGameBtn.setOnClickListener(this);

		mGameModeGroup = (RadioGroup) findViewById(R.id.radioGroup1);
		mGameModeGroup.setOnCheckedChangeListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();
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
				if (pm.getBoolean(PrefsManager.PREF_KEY_USE_NIOS, true))
				{
					String ip = pm.getString(PrefsManager.PREF_KEY_MM_IP, null);
					int port = pm.getInt(PrefsManager.PREF_KEY_MM_PORT, -1);
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
		View dialogView = inflater.inflate(R.layout.dialog_guest_findgame, null);
		EditText iptext = (EditText)dialogView.findViewById(R.id.et_ip);
		iptext.setText(ip); 
		builder.setView(dialogView)
		.setPositiveButton(R.string.dialog_confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				Toast.makeText(MainActivity.this, "Finding game...", Toast.LENGTH_SHORT).show();
				try {
					// TODO get host ip
					//	       				NetworkManager.getInstance().setupAndroidSocket(dialog. mHostIpEt.getText().toString(), 
					//	       						Integer.parseInt(mHostPortEt.getText().toString()), false); 
					//	       				NetworkManager.getInstance().setOnAndroidSocketSetupListener(this);
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
		.setNegativeButton(R.string.dialog_cancel, null)
		.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface arg0) {
				// TODO Auto-generated method stub

			}
		});
		builder.show();
	}
	
	private void showIPHistoryDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
		builder.setTitle(R.string.dialog_title_history)
		.setItems(R.array.dialog_recent_plays, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO add Port and correct IP
				dialog.dismiss();
				showGuestDialog("an ip");
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
