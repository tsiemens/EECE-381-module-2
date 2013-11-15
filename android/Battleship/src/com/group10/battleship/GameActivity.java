package com.group10.battleship;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.group10.battleship.game.Game;
import com.group10.battleship.game.Game.GameState;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.GL20Renderer;
import com.group10.battleship.graphics.GL20Renderer.RendererListener;
import com.group10.battleship.graphics.TexturedRect;
import com.group10.battleship.model.Board;
import com.group10.battleship.network.NetworkManager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnGenericMotionListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

/**
 * http://www.learnopengles.com/android-lesson-one-getting-started/
 *
 */
public class GameActivity extends SherlockActivity implements OnTouchListener, AnimationListener {
	
	private static final String TAG = GameActivity.class.getSimpleName();
	
	private GLSurfaceView mGLSurfaceView;
	private GL20Renderer mGLRenderer;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        
        Log.d(TAG, "onCreate");
        mGLSurfaceView = (GLSurfaceView)findViewById(R.id.glsv_game_view);
        
        // Check if the system supports OpenGL ES 2.0.
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;
     
        if (!supportsEs2)
        {
            // This is where you could create an OpenGL ES 1.x compatible
            // renderer if you wanted to support both ES 1 and ES 2.
        	throw new RuntimeException("Device not compatible with Open GL ES 2.0");
        }
     
        // Request an OpenGL ES 2.0 compatible context.
        mGLSurfaceView.setEGLContextClientVersion(2);
 
        // Set the renderer to our demo renderer, defined below.
        mGLRenderer = new GL20Renderer();
        mGLSurfaceView.setRenderer(mGLRenderer);
        mGLSurfaceView.setOnTouchListener(this);
        mGLRenderer.setAnimationListener(this);
        
        Game game = Game.getInstance();
        game.configure(this, mGLRenderer);
        
        if (game.getState() == GameState.UNINITIALIZED) {
        	// No game was in progress, so we have to start it.
        	game.start();
        }
        
        supportInvalidateOptionsMenu();
    }

    @Override
	public void onPause() {
    	Log.d(TAG, "onPause");
		mGLSurfaceView.onPause();
    	super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		
		mGLSurfaceView.onResume();
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getSupportMenuInflater().inflate(R.menu.game, menu);
        
        MenuItem mi;
        for (int i = 0; i < menu.size(); i++) {
        	mi = menu.getItem(i);
        	if (mi.getItemId() == R.id.switch_boards_item && mGLRenderer != null) {
        		if (mGLRenderer.getCamPosY() > 1.0f) {
                	mi.setIcon(R.drawable.ic_find_next_holo_light);
                	mi.setTitle(R.string.menu_item_goto_pboard);
                } else {
                	mi.setIcon(R.drawable.ic_find_previous_holo_light);
                	mi.setTitle(R.string.menu_item_goto_oboard);
                }
        	}
        }
        
        return true;
    }
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.switch_boards_item) {
			if (mGLRenderer.getCamPosY() > 1.0f) {
				mGLRenderer.translateCamWithAnimation(0f, 0f, 500);
			} else {
				mGLRenderer.translateCamWithAnimation(0f, 2.0f, 500);
			}
		} else if (item.getItemId() == R.id.rotate_item) {
			Game.getInstance().onRotateButtonPressed();
		} else if (item.getItemId() == R.id.fire_item) {
			Game.getInstance().onFireButtonPressed();
		}
		return true;
	}

	@Override
	public boolean onTouch(View view, MotionEvent me) {
		
		// Calculate the touch event in terms of the GL surface
		float x = me.getX()/mGLSurfaceView.getWidth();
		float glx = mGLRenderer.getRight() - mGLRenderer.getLeft();
		x = mGLRenderer.getLeft() + (x * glx);
		
		float y = me.getY()/mGLSurfaceView.getHeight();
		float gly = mGLRenderer.getTop() - mGLRenderer.getBottom();
		y = mGLRenderer.getTop() - (y * gly);
		
		Game.getInstance().onTouchGLSurface(me, x, y);
		return true;
	}

	@Override
	public void onAnimationEnd(Animation arg0) {
		Runnable r = new Runnable() {
			
			@Override
			public void run() {
					GameActivity.this.supportInvalidateOptionsMenu();					}
		}; 
		runOnUiThread(r);
	}

	@Override
	public void onAnimationRepeat(Animation arg0) {	
	}

	@Override
	public void onAnimationStart(Animation arg0) {
	}
    
}
