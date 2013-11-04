package com.group10.battleship.graphics;

import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

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
	
	private RendererListener mListener;

	private int mWidth = 1;
	private int mHeight = 1;
	
	private float mCamPosX = 0;
	private float mCamPosY = 0;
	
	public static interface RendererListener
	{
		/**
		 * Only after this is called, is it safe to create new GL20Drawables,
		 * and add them to the renderer.
		 */
		public void onSurfaceCreated();
		
		public void onFrameDrawn();
		
		public void onSurfaceChanged();
	}

	public GL20Renderer()
	{
		
	}

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config)
	{
	    //Set the background frame color
	    GLES20.glClearColor(0.7f, 0.7f, 0.7f, 1.0f);
	    Log.i(TAG, "Renderer surface created");

	    if (mListener != null) {
	    	mListener.onSurfaceCreated();
	    }
	}

	public void onDrawFrame(GL10 unused)
	{
	    //Redraw background color
	    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

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
	    //sprite.draw(mMVPMatrix);
	    if (mListener != null) {
	    	mListener.onFrameDrawn();
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
	    
	    if (mListener != null) {
	    	mListener.onSurfaceChanged();
	    }
	}
	
	public float getXMax()
	{
		return mCamPosX + (float)mWidth/(float)mHeight;
	}
	
	public float getXMin()
	{
		return mCamPosX - (float)mWidth/(float)mHeight;
	}
	
	public float getYMax()
	{
		return mCamPosY + 1;
	}
	
	public float getYMin()
	{
		return mCamPosY - 1;
	}
	
	public float getCamPosX() { return mCamPosX; }
	public float getCamPosY() { return mCamPosY; }
	public void setCamPosX(float x) { mCamPosX = x; }
	public void setCamPosY(float y) { mCamPosY = y; }
	
	public void setRendererListener(RendererListener listener)
	{
		mListener = listener;
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
