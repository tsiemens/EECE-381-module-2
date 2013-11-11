package com.group10.battleship;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.group10.battleship.game.Game;
import com.group10.battleship.game.Game.GameState;
import com.group10.battleship.graphics.GL20Drawable;
import com.group10.battleship.graphics.GL20Renderer;
import com.group10.battleship.graphics.GL20Renderer.RendererListener;
import com.group10.battleship.graphics.TexturedRect;
import com.group10.battleship.model.Board;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * http://www.learnopengles.com/android-lesson-one-getting-started/
 *
 */
public class GameActivity extends SherlockActivity implements RendererListener, OnTouchListener {
	
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
        //mGLRenderer.addRendererListener(this);
        mGLSurfaceView.setRenderer(mGLRenderer);
        mGLSurfaceView.setOnTouchListener(this);
        
        Game game = Game.getInstance();
        if (game.getState() == GameState.UNITIALIZED) {
        	game.configure(this, mGLRenderer);
        	game.start();
        }
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
        getSupportMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public void onSurfaceCreated(GL20Renderer renderer) {
		Log.d(TAG, "GL surface created");
	}

	@Override
	public void onFrameDrawn(GL20Renderer renderer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceChanged(GL20Renderer renderer) {
		Log.d(TAG, "GL surface changed");
	}
	
	@Override
	public boolean onTouch(View view, MotionEvent me) {
		
		// Calculate the touch event in terms of the GL surface
		float x = me.getX()/mGLSurfaceView.getWidth();
		float glx = mGLRenderer.getXRight() - mGLRenderer.getXLeft();
		x = mGLRenderer.getXLeft() + (x * glx);
		
		float y = me.getY()/mGLSurfaceView.getHeight();
		float gly = mGLRenderer.getYTop() - mGLRenderer.getYBottom();
		y = mGLRenderer.getYTop() - (y * gly);
		
		Game.getInstance().onTouchGLSurface(x, y);

		return false;
	}
    
}
