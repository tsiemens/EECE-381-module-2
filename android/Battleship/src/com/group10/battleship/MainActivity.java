package com.group10.battleship;

import java.io.IOException;
import java.net.UnknownHostException;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.group10.battleship.game.Game;
import com.group10.battleship.network.NIOS2NetworkManager;
import com.group10.battleship.network.NetworkManager;
import com.group10.battleship.network.NetworkManager.OnAndroidSocketSetupListener;
import com.group10.battleship.network.NetworkManager.OnNiosSocketSetupListener;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends SherlockActivity implements OnClickListener, OnAndroidSocketSetupListener, OnNiosSocketSetupListener {

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
		if (pm.getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false)) {
			startActivity(new Intent(this, GameActivity.class));
		}
		// Host
		else if(view == mStartGameBtn)
		{
			Toast.makeText(this, "Starting game...", Toast.LENGTH_SHORT).show();
			try {
				NetworkManager.getInstance().setupAndroidSocket(null, 0, true);
				NetworkManager.getInstance().setOnAndroidSocketSetupListener(this);			
				// If using nios, also set up the nios 
				if (pm.getBoolean(PrefsManager.PREF_KEY_USE_NIOS, true))
				{
					NetworkManager.getInstance().setupNiosSocket(mHostIpEt.getText().toString(), 
							Integer.parseInt(mHostPortEt.getText().toString()));
					NetworkManager.getInstance().setOnNiosSocketSetupListener(this);
				}
			} catch (Exception e) {
				handleSocketError(e);
				e.printStackTrace();
			} 
		}
		// Client
		else if(view == mFindGameBtn)
		{
			Toast.makeText(this, "Finding game...", Toast.LENGTH_SHORT).show();
			try {
				NetworkManager.getInstance().setupAndroidSocket(mHostIpEt.getText().toString(), 
						Integer.parseInt(mHostPortEt.getText().toString()), false); 
				NetworkManager.getInstance().setOnAndroidSocketSetupListener(this);
			} catch(NumberFormatException e)
			{
				Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
				e.printStackTrace();
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
		mHostIpTv.setText("IP: "
				+ NetworkManager.getInstance().getAndroidHostIP() + ":"
				+ NetworkManager.getInstance().getAndroidHostPort());
	}

	@Override
	public void onGameFound() {
		Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
		startActivity(new Intent(this, GameActivity.class));
	}

	@Override
	public void onAndroidSocketSetupError() {
		Toast.makeText(this, "Error: Could not make Android socket", Toast.LENGTH_LONG).show();
	}

}
