package com.group10.battleship;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.group10.battleship.network.NIOS2NetworkManager;
import com.group10.battleship.network.NetworkManager;
import com.group10.battleship.network.NetworkManager.OnGameFoundListener;
import com.group10.battleship.network.NetworkManager.OnIPFoundListener;
import com.group10.battleship.network.NetworkManager.OnNetworkErrorListener;
import com.group10.battleship.network.NetworkManager.OnNiosDataReceivedListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends SherlockActivity implements OnClickListener, OnIPFoundListener, OnGameFoundListener, OnNetworkErrorListener, OnNiosDataReceivedListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private Button mStartGameBtn;
	private Button mFindGameBtn;

	private EditText mHostIpEt;
	private EditText mHostPortEt;

	private TextView mHostIpTv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mStartGameBtn = (Button) findViewById(R.id.btn_start_game);
		mStartGameBtn.setOnClickListener(this);

		mFindGameBtn = (Button) findViewById(R.id.btn_find_game);
		mFindGameBtn.setOnClickListener(this);

		mHostIpEt = (EditText) findViewById(R.id.et_host_ip);
		mHostPortEt = (EditText) findViewById(R.id.et_host_port);

		mHostIpTv = (TextView) findViewById(R.id.tv_host_ip);

	}

	@Override
	public void onResume() {
		super.onResume();
		// Setting visibility of components. Prefs could have changed
		PrefsManager pm = PrefsManager.getInstance();
		if (pm.getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false)
				|| !pm.getBoolean(PrefsManager.PREF_KEY_USE_NIOS, true)) {
			// This should be the current ip
			mHostIpTv.setText("current ip here");
			mStartGameBtn.setVisibility(View.VISIBLE);
		} else {
			// This should be the current ip
			mHostIpTv.setText(R.string.main_menu_nios_ip);
			mStartGameBtn.setVisibility(View.GONE);
		}
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
//		HOST SETUP
		if (view == mStartGameBtn) {
			if (pm.getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false)) {
				startActivity(new Intent(this, GameActivity.class));
			} else if (pm.getBoolean(PrefsManager.PREF_KEY_USE_NIOS, true)) {
				Toast.makeText(
						this,
						"Enter a host ip, or turn on local debugging in settings.",
						Toast.LENGTH_LONG).show();
				// TODO check for game connection, etc.
				try {
					// Set up the NIOS socket & listener
					NetworkManager.getInstance().setupNiosSocket(mHostIpEt.getText().toString(), 
							Integer.parseInt(mHostPortEt.getText().toString()));
					NetworkManager.getInstance().setOnNiosDataReceivedListener(this);
					// Send a game request
//					NIOS2NetworkManager.sendNewGame();
					
				} catch (Exception e) {
					Log.d(TAG, "Error making NIOS socket");
					handleSocketError(e);
				}
				
			} else {
				// Not using nios
				try {
					NetworkManager.getInstance().setupAndroidSocket(null, 0, true);
					NetworkManager.getInstance().setOnIPFoundListener(this);
					NetworkManager.getInstance().setOnGameFoundListener(this);
					NetworkManager.getInstance()
							.setOnNetworkErrorListener(this);
				} catch (Exception e) {
					handleSocketError(e);
				} 
			}
		} 
//		CLIENT SETUP
		else if (view == mFindGameBtn) {
			Toast.makeText(this, "Finding game...",
					Toast.LENGTH_SHORT).show();
			if (pm.getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false)) 
			{
				startActivity(new Intent(this, GameActivity.class));
			} 
			else if (pm.getBoolean(PrefsManager.PREF_KEY_USE_NIOS, true)){
				try {
					NetworkManager.getInstance().setupNiosSocket(mHostIpEt.getText().toString(), 
							Integer.parseInt(mHostPortEt.getText().toString()));
				} catch (Exception e) {
					handleSocketError(e);
				}
			}

			else if (view == mFindGameBtn) {
				Toast.makeText(this, "Finding game...", Toast.LENGTH_SHORT).show();
				try {
					//	CLIENT SETUP
					NetworkManager.getInstance().setupAndroidSocket(mHostIpEt.getText().toString(), 
							Integer.parseInt(mHostPortEt.getText().toString()), false); 
					NetworkManager.getInstance().setOnGameFoundListener(this);
					NetworkManager.getInstance().setOnGameFoundListener(this);
				}
				catch(Exception e)
				{
					handleSocketError(e);
				}
			}
			else
			{
				try {
					NetworkManager.getInstance().setupAndroidSocket(mHostIpEt.getText().toString(), 
						Integer.parseInt(mHostPortEt.getText().toString()), false); 
					NetworkManager.getInstance().setOnGameFoundListener(this);
					NetworkManager.getInstance().setOnGameFoundListener(this);
				}
				catch(NumberFormatException e)
				{
					Toast.makeText(this, "No Port specified", Toast.LENGTH_LONG).show();
				} catch (Exception e) {
					handleSocketError(e);
				}
			}
		}
	}


	@Override
	public void onIPFound(String IP, int port) {
		// TODO Auto-generated method stub
		mHostIpTv.setText("IP: "
				+ NetworkManager.getInstance().getAndroidHostIP() + ":"
				+ NetworkManager.getInstance().getAndroidHostPort());

	}

	@Override
	public void onGameFound() {
		// TODO Auto-generated method stub
		Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
		startActivity(new Intent(this, GameActivity.class));
	}

	@Override
	public void onClientSocketError() {
		Toast.makeText(this, "Error: Could not find game", Toast.LENGTH_LONG)
				.show();

	}

	@Override
	public void ReceivedNiosData(String message) {
		if(message.equals("H")) // Received host confirmation
		{
			try {
				NetworkManager.getInstance().setupAndroidSocket(null, 0, true);
				// TODO: 
			} catch (Exception e) {
				handleSocketError(e);
			}
		}
		else if(message.equals("2"))
		{
			try {
				// TODO: Get the IP & port from the message: message.getIP() / message.getPort() 
				NetworkManager.getInstance().setupAndroidSocket("Insert IP from message here", 1234, false);
			} catch (Exception e) {
				handleSocketError(e);
			}

		}
	}
	
	// Show toast with error message & log the stack trace
	private void handleSocketError(Exception e)
	{
		Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
		e.printStackTrace();
	}

}
