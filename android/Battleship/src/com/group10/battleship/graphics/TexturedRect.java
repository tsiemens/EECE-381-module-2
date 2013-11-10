package com.group10.battleship.graphics;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;
import android.util.SparseIntArray;


/**
 * Based off http://stackoverflow.com/questions/12793341/draw-a-2d-image-using-opengl-es-2-0
 *
 */
public class TexturedRect implements GL20Drawable{
	
	private static final String TAG = TexturedRect.class.getSimpleName();

	private static final int BYTES_PER_FLOAT = 4;
	
	private static final int TEXTURE_COORD_DATA_SIZE = 2;

	private static final String vertexShaderCode =
	"attribute vec2 a_TexCoordinate;" +
	"varying vec2 v_TexCoordinate;" +
	"uniform mat4 uMVPMatrix;" +
	"attribute vec4 vPosition;" +
	"void main() {" +
	"	gl_Position = uMVPMatrix * vPosition;" +
	"	v_TexCoordinate = a_TexCoordinate;" +
	"}";

	private static final String fragmentShaderCode =
	"precision mediump float;" +
	"uniform vec4 vColor;" +
	"uniform sampler2D u_Texture;" +
	"varying vec2 v_TexCoordinate;" +
	"void main() {" +
	"	gl_FragColor = (vColor * texture2D(u_Texture, v_TexCoordinate));" +
	"}";
	
	private static int sShaderProgramHandle;
	
	// A key-value (resource id-gl tex handle) map
	private static SparseIntArray sTexHandleMap;

	// number of coordinates per vertex in this array
	static final int COORDS_PER_VERTEX = 2;

	private static final short sDrawOrder[] = { 0, 1, 2, 0, 2, 3 }; //Order to draw vertices
	private static final int VERTEX_STRIDE = COORDS_PER_VERTEX * BYTES_PER_FLOAT; //Bytes per vertex

	//Reference to Activity Context
	private Context mActivityContext;

	//Added for Textures
	private FloatBuffer mTextureBuffer;
	private int mTextureUniformHandle;
	private int mTextureCoordinateHandle;
	private int mTextureDataHandle;
	private int mTextureResId;
   
	private FloatBuffer mVertexBuffer;
	private ShortBuffer mDrawListBuffer;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;

	
	// Set color with red, green, blue and alpha (opacity) values
	private float mColor[] = { 1f, 1f, 1f, 1.0f };
	private float mCoords[] = { -0.5f,  0.5f,   // top left
        						-0.5f, -0.5f,   // bottom left
        						0.5f, -0.5f,   // bottom right
        						0.5f,  0.5f }; //top right
	
	// S, T (or X, Y)
    // Texture coordinate data.
    // Because images have a Y axis pointing downward (values increase as you move down the image) while
    // OpenGL has a Y axis pointing upward, we adjust for that here by flipping the Y axis.
    // What's more is that the texture coordinates are the same for every face.
	private float mTextureCoords[] = { 0.0f, 0.0f,  // top left
            						0.0f, 1.0f,  // bottom left
            						1.0f, 1.0f,  // bottom right
            						1.0f, 0.0f}; // top right
	
	private static void loadShaderProgram()
	{
		final int[] linkStatus = new int[1];
		GLES20.glGetProgramiv(sShaderProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
		 
	    // We don't want to repeat if the program is still valid
	    if (linkStatus[0] != 0)
	    {
	        return;
	    }
	    
	    // Context may have been lost, since recompiling, so reset loaded textures.
	    sTexHandleMap = null;
	    
		int vertexShader = GL20Renderer.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
	    int fragmentShader = GL20Renderer.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

	    sShaderProgramHandle = GLES20.glCreateProgram();
	    GLES20.glAttachShader(sShaderProgramHandle, vertexShader);
	    GLES20.glAttachShader(sShaderProgramHandle, fragmentShader);

	    //Texture Code
	    GLES20.glBindAttribLocation(sShaderProgramHandle, 0, "a_TexCoordinate");

	    GLES20.glLinkProgram(sShaderProgramHandle);
	    
	    // Get the link status.
	    GLES20.glGetProgramiv(sShaderProgramHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);
	 
	    // If the link failed, delete the program.
	    Log.d(TAG, "Loaded Shader program status: "+linkStatus[0]);
	    if (linkStatus[0] == 0)
	    {
	        GLES20.glDeleteProgram(sShaderProgramHandle);
	        sShaderProgramHandle = 0;
	        throw new RuntimeException("Failed to load shader program!");
	    }
	}

	public TexturedRect(Context activityContext, int texResId)
	{
	    mActivityContext = activityContext;

	    mVertexBuffer = toFloatBuffer(mCoords);

	    mTextureBuffer = toFloatBuffer(mTextureCoords);

	    //Initialize byte buffer for the draw list
	    ByteBuffer dlb = ByteBuffer.allocateDirect(mCoords.length * 2);
	    dlb.order(ByteOrder.nativeOrder());
	    mDrawListBuffer = dlb.asShortBuffer();
	    mDrawListBuffer.put(sDrawOrder);
	    mDrawListBuffer.position(0);

	    TexturedRect.loadShaderProgram();
	    
	    //Load the texture
	    mTextureResId = texResId;
	    mTextureDataHandle = loadTexture(mActivityContext, texResId);
	}
	
	public void reloadTexture() {
		mTextureDataHandle = loadTexture(mActivityContext, mTextureResId);
	}

	@Override
	public void draw(float[] mvpMatrix)
	{
	    //Add program to OpenGL ES Environment
	    GLES20.glUseProgram(sShaderProgramHandle);
	    
	    GLES20.glEnable(GLES20.GL_BLEND);
	    GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

	    //Get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(sShaderProgramHandle, "vPosition");

	    //Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);

	    //Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX, GLES20.GL_FLOAT, false, VERTEX_STRIDE, mVertexBuffer);

	    //Get Handle to Fragment Shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(sShaderProgramHandle, "vColor");

	    //Set the Color for drawing the triangle
	    GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);

	    //Set Texture Handles and bind Texture
	    mTextureUniformHandle = GLES20.glGetAttribLocation(sShaderProgramHandle, "u_Texture");
	    mTextureCoordinateHandle = GLES20.glGetAttribLocation(sShaderProgramHandle, "a_TexCoordinate");

	    //Set the active texture unit to texture unit 0.
	    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

	    //Bind the texture to this unit.
	    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureDataHandle);

	    //Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0.
	    GLES20.glUniform1i(mTextureUniformHandle, 0); 

	    //Pass in the texture coordinate information
	    mTextureBuffer.position(0);
	    GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXTURE_COORD_DATA_SIZE, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
	    GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

	    //Get Handle to Shape's Transformation Matrix
	    mMVPMatrixHandle = GLES20.glGetUniformLocation(sShaderProgramHandle, "uMVPMatrix");

	    //Apply the projection and view transformation
	    GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

	    //Draw the triangle
	    GLES20.glDrawElements(GLES20.GL_TRIANGLES, sDrawOrder.length, GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

	    //Disable Vertex Array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
	}
	
	/**
	 * Sets the position of the top left of the rectangle
	 * @param x
	 * @param y
	 */
	public void setPosition(float x, float y)
	{
		float width = getWidth();
		float height = getHeight();
		mCoords[0] = x; // top left
		mCoords[1] = y;
		mCoords[2] = x; // bottom left
		mCoords[3] = y - height;
		mCoords[4] = x + width; // bottom right
		mCoords[5] = y - height;
		mCoords[6] = x + width; // top right
		mCoords[7] = y;
		
		mVertexBuffer = toFloatBuffer(mCoords);
	}
	
	public float getXPos()
	{
		return mCoords[0];
	}
	
	public float getYPos()
	{
		return mCoords[1];
	}
	
	public void setSize(float width, float height)
	{
		float x = getXPos();
		float y = getYPos();
		mCoords[2] = x; // bottom left
		mCoords[3] = y - height;
		mCoords[4] = x + width; // bottom right
		mCoords[5] = y - height;
		mCoords[6] = x + width; // top right
		mCoords[7] = y;
		
		mVertexBuffer = toFloatBuffer(mCoords);
	}
	
	public float getWidth()
	{
		return mCoords[6] - mCoords[0];
	}
	
	public float getHeight()
	{
		return mCoords[1] - mCoords[3];
	}
	
	public int getColor()
	{
		return Color.argb((int)(mColor[3]*255), (int)(mColor[0]*255),
				(int)(mColor[1]*255), (int)(mColor[2]*255));
	}
	
	/**
	 * Sets the filter color. Values must be from 0.0f ... 1.0f
	 * @param alpha
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void setColor(float alpha, float red, float green, float blue)
	{
		mColor[3] = alpha;
		mColor[0] = red;
		mColor[1] = green;
		mColor[2] = blue;
	}
	
	/**
	 * Sets the filter color. Value is a color as defined by android.graphics.Color
	 * @param color
	 */
	public void setColor(int color)
	{
		mColor[3] = (float) (Color.alpha(color)/255.0);
		mColor[0] = (float) (Color.red(color)/255.0);
		mColor[1] = (float) (Color.green(color)/255.0);
		mColor[2] = (float) (Color.blue(color)/255.0);
	}

	/**
	 * loads the texture from resources
	 * @param context
	 * @param resourceId
	 * @return the handle for the texture
	 */
	public static int loadTexture(Context context, int resourceId)
	{	
		if (sTexHandleMap == null) {
			sTexHandleMap = new SparseIntArray();
		}
		
		// If the texture has already been loaded into GL, just return the handle
		if (sTexHandleMap.get(resourceId) != 0) {
			return sTexHandleMap.get(resourceId);
		}
		
	    int[] textureHandle = new int[1];

	    GLES20.glGenTextures(1, textureHandle, 0);

	    if (textureHandle[0] != 0)
	    {
	        BitmapFactory.Options options = new BitmapFactory.Options();
	        options.inScaled = false;   // No pre-scaling

	        // Read in the resource
	        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

	        // Bind to the texture in OpenGL
	        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

	        // Set filtering
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
	        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

	        // Load the bitmap into the bound texture.
	        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

	        // Recycle the bitmap, since its data has been loaded into OpenGL.
	        bitmap.recycle();
	    }

	    if (textureHandle[0] == 0)
	    {
	        throw new RuntimeException("Error loading texture.");
	    }

	    sTexHandleMap.put(resourceId, textureHandle[0]);
	    return textureHandle[0];
	}
	
	private static FloatBuffer toFloatBuffer(float[] array)
	{
		//Initialize Byte Buffer
	    ByteBuffer bb = ByteBuffer.allocateDirect(array.length * BYTES_PER_FLOAT); 
	    //Use the Device's Native Byte Order
	    bb.order(ByteOrder.nativeOrder());
	    //Create a floating point buffer from the ByteBuffer
	    FloatBuffer fb = bb.asFloatBuffer();
	    //Add the coordinates to the FloatBuffer
	    fb.put(array);
	    //Set the Buffer to Read the first coordinate
	    fb.position(0);
	    return fb;
	}
	
}
