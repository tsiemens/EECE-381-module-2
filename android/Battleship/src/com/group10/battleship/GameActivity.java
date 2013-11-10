package com.group10.battleship;

import java.util.ArrayList;
import java.util.List;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
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
	
	private List<GL20Drawable> mDrawList;
	
	private Board mPlayerBoard;
	
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
        mGLSurfaceView.setOnTouchListener(this);
        
        mDrawList = new ArrayList<GL20Drawable>();
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
		Log.d(TAG, "GL surface created");
		
		mGLRenderer.setDrawList(mDrawList);
	}

	@Override
	public void onFrameDrawn() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSurfaceChanged() {
		Log.d(TAG, "GL surface changed");
		
		float x = mGLRenderer.getXLeft();
		float y = mGLRenderer.getYTop();
		float width = mGLRenderer.getXRight() - x;
		float height = y - mGLRenderer.getYBottom();
		float sideLength = (height > width) ? width : height;
		
		if (mPlayerBoard == null) {
			mPlayerBoard = new Board(this, sideLength, x, y);
		} else {
			// The screen may have changed, so we need to rebuild the board
			Board b = new Board(this, sideLength, x, y);
			for (int row = 0; row < Board.BOARD_SIZE; row++) {
				for (int col = 0; col < Board.BOARD_SIZE; col++) {
					b.setTileColour(mPlayerBoard.getTileColour(col, row), col, row);
				}
			}
			mDrawList.remove(mPlayerBoard);
			mPlayerBoard = b;
		}
		mDrawList.add(mPlayerBoard);
	}

	@Override
	public boolean onTouch(View view, MotionEvent me) {
		
		float x = me.getX()/mGLSurfaceView.getWidth();
		float glx = mGLRenderer.getXRight() - mGLRenderer.getXLeft();
		x = mGLRenderer.getXLeft() + (x * glx);
		
		float y = me.getY()/mGLSurfaceView.getHeight();
		float gly = mGLRenderer.getYTop() - mGLRenderer.getYBottom();
		y = mGLRenderer.getYTop() - (y * gly);
		
		int[] inx = mPlayerBoard.getTileIndexAtLocation(x, y);
		if (inx != null) {
			Log.d(TAG, "Touched tile: "+inx[0]+","+inx[1]);

			/* Test code. delete later
			if (inx[0] > 4)
				mPlayerBoard.setTileColour(Board.TILE_COLOR_HIT, inx[0], inx[1]);
			else
				mPlayerBoard.setTileColour(Board.TILE_COLOR_MISS, inx[0], inx[1]);
				*/
			
			// TODO do stuff with the touch event, during game
		}
		return false;
	}
    
}
