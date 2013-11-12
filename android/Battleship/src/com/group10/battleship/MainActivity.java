package com.group10.battleship;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends SherlockActivity implements OnClickListener {

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
        
        mStartGameBtn = (Button)findViewById(R.id.btn_start_game);
        mStartGameBtn.setOnClickListener(this);
        
        mFindGameBtn = (Button)findViewById(R.id.btn_find_game);
        mFindGameBtn.setOnClickListener(this);
        
        mHostIpEt = (EditText)findViewById(R.id.et_host_ip);
        mHostPortEt = (EditText)findViewById(R.id.et_host_port);
        
        mHostIpTv = (TextView)findViewById(R.id.tv_host_ip);
    }
    
    @Override
    public void onResume() {
    	super.onResume();
    	// Setting visibility of components. Prefs could have changed
    	PrefsManager pm = PrefsManager.getInstance();
    	if ( pm.getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false) ||
    			!pm.getBoolean(PrefsManager.PREF_KEY_USE_NIOS, true)) {
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
    public boolean onOptionsItemSelected(MenuItem item)
    {
    	if (item.getItemId() == R.id.app_settings) {
    		startActivity(new Intent(this, PreferenceActivity.class));
    		return true;
    	}
    	return super.onOptionsItemSelected(item);
    }

	@Override
	public void onClick(View view) {
		PrefsManager pm = PrefsManager.getInstance();
		if (view == mStartGameBtn) {
			if (pm.getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false)) {
				startActivity(new Intent(this, GameActivity.class));
			} else if (pm.getBoolean(PrefsManager.PREF_KEY_USE_NIOS, true)){
				Toast.makeText(this, "Enter a host ip, or turn on local debugging in settings.",
						Toast.LENGTH_LONG).show();
				// TODO check for game connection, etc.
			} else {
				// Not using nios
				mHostIpTv.setText("Your ip here");
			}
		} else if (view == mFindGameBtn) {
			Toast.makeText(this, "Starting game host server...",
					Toast.LENGTH_SHORT).show();
			// TODO
		}
	}
    
}
