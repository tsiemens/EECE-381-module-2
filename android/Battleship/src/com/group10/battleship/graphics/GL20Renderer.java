package com.group10.battleship.graphics;

import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;

/**
 * Based off http://stackoverflow.com/questions/12793341/draw-a-2d-image-using-opengl-es-2-0
 *
 */
public class GL20Renderer implements GLSurfaceView.Renderer{
	
	private static final String TAG = GL20Renderer.class.getSimpleName();

	//Matrix Initializations
	private final float[] mMVPMatrix = new float[16];
	private final float[] mProjMatrix = new float[16];
	private final float[] mVMatrix = new float[16];
	private float[] mRotationMatrix = new float[16];

	private float mAngle = 0;

	private List<GL20Drawable> mDrawables;
	
	private List<RendererListener> mRendererListeners;
	private AnimationListener mAnimListener;

	private int mWidth = 1;
	private int mHeight = 1;
	
	private float mCamPosX = 0;
	private float mCamPosY = 0;
	
	private TranslateAnimation mCurrentTransAnim;
	private Transformation mAnimTransformation;
	private float mPreAnimCamX;
	private float mPreAnimCamY;
	
	public static interface RendererListener
	{
		/**
		 * Only after this is called, is it safe to create new GL20Drawables,
		 * and add them to the renderer.
		 */
		public void onSurfaceCreated(GL20Renderer renderer);
		
		public void onFrameDrawn(GL20Renderer renderer);
		
		public void onSurfaceChanged(GL20Renderer renderer);
	}

	public GL20Renderer()
	{
		mRendererListeners = new ArrayList<RendererListener>();
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config)
	{
	    //Set the background frame color
	    GLES20.glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
	    Log.i(TAG, "Renderer surface created");

	    // Notify the listeners
	    for (RendererListener l : mRendererListeners) {
	    	l.onSurfaceCreated(this);
	    }
	}

	public void onDrawFrame(GL10 unused)
	{
	    //Redraw background color
	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
	    
	    // If there is an animation in progress, process it
	    if (mCurrentTransAnim != null) {
	    	if (mCurrentTransAnim.getTransformation(System.currentTimeMillis(), 
	    			mAnimTransformation)) {
	    		android.graphics.Matrix m = mAnimTransformation.getMatrix();
	    		float[] p = new float []{mPreAnimCamX, mPreAnimCamY};
	    		m.mapPoints(p);
	    		mCamPosX = p[0];
	    		mCamPosY = p[1];
	    	} else {
	    		mCurrentTransAnim = null;
	    	}
	    }

	    //Set the camera position (View Matrix)
	    Matrix.setLookAtM(mVMatrix, 0, mCamPosX, mCamPosY, 3f, mCamPosX, mCamPosY, 0f, 0f, 1.0f, 0.0f);

	    //Calculate the projection and view transformation
	    Matrix.multiplyMM(mMVPMatrix, 0, mProjMatrix, 0, mVMatrix, 0);

	    //Create a rotation transformation for the triangle
	    Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, 1.0f);

	    //Combine the rotation matrix with the projection and camera view
	    Matrix.multiplyMM(mMVPMatrix, 0, mRotationMatrix, 0, mMVPMatrix, 0);

	    //Draw Items
	    if (mDrawables != null) {
	    	for (GL20Drawable item : mDrawables) {
	    		item.draw(mMVPMatrix);
	    	}
	    }

	    // Notify the listeners
	    for (RendererListener l : mRendererListeners) {
	    	l.onFrameDrawn(this);
	    }
	}

	public void onSurfaceChanged(GL10 unused, int width, int height)
	{
	    GLES20.glViewport(0, 0, width, height);

	    mWidth = width;
	    mHeight = height;
	    float ratio = (float) width / height;

	    //This Projection Matrix is applied to object coordinates in the onDrawFrame() method
	    Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
	    
	    // Notify the listeners
	    for (RendererListener l : mRendererListeners) {
	    	l.onSurfaceChanged(this);
	    }
	}
	
	public float getRight() {
		return mCamPosX + (float)mWidth/(float)mHeight;
	}
	
	public float getDefaultRight() {
		return (float)mWidth/(float)mHeight;
	}
	
	public float getLeft() {
		return mCamPosX - (float)mWidth/(float)mHeight;
	}
	
	public float getDefaultLeft() {
		return - (float)mWidth/(float)mHeight;
	}
	
	public float getTop() {
		return mCamPosY + 1;
	}
	
	public float getDefaultTop() { return 1;}
	
	public float getBottom() {
		return mCamPosY - 1;
	}
	
	public float getDefaultBottom() { return -1;}
	
	public float getCamPosX() { return mCamPosX; }
	public float getCamPosY() { return mCamPosY; }
	public void setCamPosX(float x) { mCamPosX = x; }
	public void setCamPosY(float y) { mCamPosY = y; }
	
	public boolean translateCamWithAnimation(float newCamX, float newCamY, long durationMilis) {
		if (mCurrentTransAnim != null)
			return false;
		
		mAnimTransformation = new Transformation();
		mPreAnimCamX = mCamPosX;
		mPreAnimCamY = mCamPosY;
		mCurrentTransAnim = new TranslateAnimation(Animation.ABSOLUTE, 0, Animation.ABSOLUTE, newCamX - mCamPosX, 
				Animation.ABSOLUTE, 0, Animation.ABSOLUTE, newCamY - mCamPosY );
		mCurrentTransAnim.setDuration(durationMilis);
		mCurrentTransAnim.setFillEnabled(true);
		mCurrentTransAnim.setFillAfter(true);
		mCurrentTransAnim.setInterpolator(new AccelerateDecelerateInterpolator());
		mCurrentTransAnim.setAnimationListener(mAnimListener);
		mCurrentTransAnim.initialize(0, 0, 0, 0);
		mCurrentTransAnim.start();
		return true;
	}
	
	public void setAnimationListener(AnimationListener al) {
		mAnimListener = al;
		if (mCurrentTransAnim != null)
			mCurrentTransAnim.setAnimationListener(al);
	}
	
	public void addRendererListener(RendererListener listener)
	{
		mRendererListeners.add(listener);
	}
	
	public void removeRendererListener(RendererListener listener)
	{
		mRendererListeners.remove(listener);
	}
	
	public void setDrawList(List<GL20Drawable> items)
	{
		mDrawables = items;
	}

	public static int loadShader(int type, String shaderCode)
	{
	    //Create a Vertex Shader Type Or a Fragment Shader Type (GLES20.GL_VERTEX_SHADER OR GLES20.GL_FRAGMENT_SHADER)
	    int shader = GLES20.glCreateShader(type);

	    //Add The Source Code and Compile it
	    GLES20.glShaderSource(shader, shaderCode);
	    GLES20.glCompileShader(shader);
	    
	    // Get the compilation status.
	    final int[] compileStatus = new int[1];
	    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
	 
	    // If the compilation failed, delete the shader.
	    if (compileStatus[0] == 0)
	    {
	        GLES20.glDeleteShader(shader);
	        throw new RuntimeException("Failed to compile shader code! ("+shaderCode.substring(0, 32)+"...)" );
	    }

	    return shader;
	}
}
