package com.group10.battleship;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.group10.battleship.graphics.GL20Renderer;
import com.group10.battleship.graphics.GL20Renderer.RendererListener;
import com.group10.battleship.graphics.TexturedRect;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;

/**
 * http://www.learnopengles.com/android-lesson-one-getting-started/
 *
 */
public class GameActivity extends SherlockActivity implements RendererListener {
	
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
        mGLRenderer.setRendererListener(this);
        mGLSurfaceView.setRenderer(mGLRenderer);
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
	public void onSurfaceCreated() {
		// TODO This is all test code to demo the drawing
		TexturedRect rect = new TexturedRect(this, R.drawable.main_menu_background);
		rect.setSize(0.3f, 0.3f);
		//rect.setPosition(0f, 0.5f);
		mGLRenderer.addDrawable(rect);
		rect = new TexturedRect(this, R.drawable.main_menu_background);
		rect.setSize(0.1f, 0.1f);
		rect.setPosition(0.5f, 1f);
		mGLRenderer.addDrawable(rect);
		rect = new TexturedRect(this, R.drawable.white_pix);
		rect.setSize(0.4f, 0.4f);
		rect.setPosition(0f, 0.0f);
		rect.setColor(1.0f, 0.0f, 1.0f, 1.0f);
		mGLRenderer.addDrawable(rect);
		rect = new TexturedRect(this, R.drawable.white_pix);
		rect.setSize(0.4f, 0.4f);
		rect.setPosition(-0.5f, 0.8f);
		rect.setColor(0.1f, 0.0f, 1.0f, 1.0f);
		mGLRenderer.addDrawable(rect);
		rect = new TexturedRect(this, R.drawable.white_pix);
		rect.setSize(0.4f, 0.4f);
		rect.setPosition(-0.5f, 0.3f);
		rect.setColor(0.5f, 0.0f, 1.0f, 1.0f);
		mGLRenderer.addDrawable(rect);
	}

	@Override
	public void onFrameDrawn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceChanged() {
		// TODO Auto-generated method stub
		
		// More tests for screen bounds (shows in upper left) 
		Log.d(TAG, "surface changed "+mGLRenderer.getXBound());
		TexturedRect rect = new TexturedRect(this, R.drawable.main_menu_background);
		rect.setSize(0.1f, 0.1f);
		rect.setPosition(-mGLRenderer.getXBound(), mGLRenderer.getYBound());
		mGLRenderer.addDrawable(rect);
	}
    
}
