package com.group10.battleship;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
	public void onClick(View view) {
		if (view == mStartGameBtn) {
			startActivity(new Intent(this, GameActivity.class));
		}
	}
    
}
