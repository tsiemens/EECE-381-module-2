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
import android.widget.Toast;

public class MainActivity extends SherlockActivity implements OnClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	
	private Button mStartGameBtn;
	private EditText mHostIpEt;
	private EditText mHostPortEt;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mStartGameBtn = (Button)findViewById(R.id.btn_start_game);
        mStartGameBtn.setOnClickListener(this);
        
        mHostIpEt = (EditText)findViewById(R.id.et_host_ip);
        mHostPortEt = (EditText)findViewById(R.id.et_host_port);
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
		if (view == mStartGameBtn) {
			if (PrefsManager.getInstance().getBoolean(PrefsManager.PREF_KEY_LOCAL_DEBUG, false)) {
				startActivity(new Intent(this, GameActivity.class));
			} else {
				Toast.makeText(this, "Enter a host ip, or turn on local debugging in settings.",
						Toast.LENGTH_LONG).show();
				// TODO check for game connection, etc.
			}
		}
	}
    
}
