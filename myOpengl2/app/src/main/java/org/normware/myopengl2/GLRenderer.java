package org.normware.myopengl2;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.Matrix;
import android.view.KeyEvent;
import android.view.MotionEvent;
import static org.normware.myopengl2.Constants.TOUCH_SCALE_FACTOR;

public class GLRenderer implements Renderer {

	public String testText = "HELLO";
	public Globals globals = new Globals();

	//3d models
	RectangleModel modelRec = new RectangleModel(new PointF(20f, 20f), true, false, true, true);
	/*Model3d keep = new Model3d("test", true, true, 1.0f,
				new PosAngScale(0f, 5f, 0f,//location
								0f, 0f, 0f,//rotations
								.2f, .2f, .2f));//size*/
    Model3d keep = new Model3d("test", true, true, 1.0f,
            new PosAngScale(0f, 0f, -5f,//location
                    0f, 0f, 0f,//rotations
                    1f, 1f, 1f));//size
	GLText glText = new GLText();
	MapTile grassTile[] = new MapTile[1];


	Vector3f cameraAngles = new Vector3f(0f, 0f, 0f);
	float scale = 1.0f;
	PointF oldTouch = new PointF();
	public boolean perspectiveView = true;//perspective or ortho
	PointF lastScreenTouch = new PointF();

	private int[] textureIDs = new int[32]; //texture image ID's
	private int texturePntr = 0;

	// Our screenresolution
	float	mScreenWidth = 1280;
	float	mScreenHeight = 768;

	// Misc
	Context mContext;
	long mLastTime;
	int mProgram;
	
	public GLRenderer(Context c)
	{
		mContext = c;
		mLastTime = System.currentTimeMillis() + 100;
	}
	
	public void onPause()
	{
		/* Do stuff to pause the renderer */
	}
	
	public void onResume()
	{
		/* Do stuff to resume the renderer */
		mLastTime = System.currentTimeMillis();
	}
	
	@Override
	public void onDrawFrame(GL10 unused) {
		
		// Get the current time
    	long now = System.currentTimeMillis();
    	
    	// We should make sure we are valid and sane
    	if (mLastTime > now) return;
        
    	// Get the amount of time the last frame took.
    	long elapsed = now - mLastTime;
		
		// Update our example
		
		// Render our example
		Render();
		
		// Save the current time to see how long it took :).
        mLastTime = now;
		
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		// We need to know the current width and height.
		mScreenWidth = width;
		mScreenHeight = height;
		
		// Redo the Viewport, making it fullscreen.
		GLES20.glViewport(0, 0, (int)mScreenWidth, (int)mScreenHeight);
		
		UpdateWorldMatrix();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
		//load textures
		GLES20.glGenTextures(textureIDs.length, textureIDs, 0);  // Generate texture-ID array
		modelRec.LoadTexture(mContext, R.drawable.ic_launcher, texturePntr++);
		keep.LoadTexture(mContext, "cube", texturePntr++);
		glText.LoadFont(mContext, Color.argb(255,255,255,255),
												Color.argb(0,0,0,0),
												true, .5f, texturePntr++);

		grassTile[0] = new MapTile();
		grassTile[0].LoadTile(mContext, R.raw.grass, texturePntr++);

		// Set the clear color to black
		GLES20.glClearColor(0.0f, 0.5f, 0.5f, 1);

		SetupShaderPrograms();

	}

	private void SetupShaderPrograms(){
		// load shader text
	    GraphicTools.ReadShaderText(mContext);

		// Create the shaders, solid color
		int vertexShader = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, GraphicTools.vs_SolidColor);
		int fragmentShader = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, GraphicTools.fs_SolidColor);

		GraphicTools.sp_SolidColor = GLES20.glCreateProgram();             // create empty OpenGL ES Program
		GLES20.glAttachShader(GraphicTools.sp_SolidColor, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(GraphicTools.sp_SolidColor, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(GraphicTools.sp_SolidColor);                  // creates OpenGL ES program executables

		// Create the shaders, images with lighting
		vertexShader = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, GraphicTools.vs_Image_Lighting);
		fragmentShader = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, GraphicTools.fs_Image_Lighting);

		GraphicTools.sp_ImageLighting = GLES20.glCreateProgram();             // create empty OpenGL ES Program
		GLES20.glAttachShader(GraphicTools.sp_ImageLighting, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(GraphicTools.sp_ImageLighting, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(GraphicTools.sp_ImageLighting);                  // creates OpenGL ES program executables

		// Create the shaders, images
		vertexShader = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, GraphicTools.vs_Image);
		fragmentShader = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, GraphicTools.fs_Image);

		GraphicTools.sp_Image = GLES20.glCreateProgram();             // create empty OpenGL ES Program
		GLES20.glAttachShader(GraphicTools.sp_Image, vertexShader);   // add the vertex shader to program
		GLES20.glAttachShader(GraphicTools.sp_Image, fragmentShader); // add the fragment shader to program
		GLES20.glLinkProgram(GraphicTools.sp_Image);                  // creates OpenGL ES program executables

		// Text shader
		int vshadert = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
				GraphicTools.vs_Text);
		int fshadert = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
				GraphicTools.fs_Text);

		GraphicTools.sp_Text = GLES20.glCreateProgram();
		GLES20.glAttachShader(GraphicTools.sp_Text, vshadert);
		GLES20.glAttachShader(GraphicTools.sp_Text, fshadert);
		GLES20.glLinkProgram(GraphicTools.sp_Text);


		// Set our shader programm
		//GLES20.glUseProgram(GraphicTools.sp_Image);
	}

	private void Render() {

	    UpdateWorldMatrix();

		// clear Screen and Depth Buffer, we have set the clear color as black.
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		//draw 3d objects
        //draw map tiles
        //grassTile[0].Draw(globals);

		modelRec.Draw(globals.viewProjMatrix, new Vector3f(0f, 0f, -20f), new Vector3f(0f, 0f, 0f), 1f);
		//rotate model for testing
		keep.position.angles.Add3f(1f, 0f, 0f);
		keep.Draw(globals);
        glText.Draw(globals.orthoMatrix, testText, new PointF(0f, 0f));

	}

	public void processTouchEvent(MotionEvent event) {
		float currentX = event.getX();
        float currentY = event.getY();
        float deltaX, deltaY;
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                // Modify rotational angles according to movement
                deltaX = currentX - lastScreenTouch.x;
                deltaY = currentY - lastScreenTouch.y;
                cameraAngles.x -= deltaY * TOUCH_SCALE_FACTOR;
                cameraAngles.y -= deltaX * TOUCH_SCALE_FACTOR;
                break;
        }
        // Save current x, y
        lastScreenTouch = new PointF(currentX, currentY);
	}

	public void UpdateWorldMatrix(){

	    //testText = Float.toString(keep.position.location.y);

		float[] tempMatrix = new float[16];

		// Clear our matrices
		for(int i=0;i<16;i++)
		{
			globals.perspectiveMatrix[i] = 0.0f;
            globals.cameraViewMatrix[i] = 0.0f;
            globals.orthoMatrix[i] = 0.0f;
		}

		//set camera looking along z axis, good for 2D games
		Matrix.setLookAtM(globals.cameraViewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0.0f);
        globals.viewMatrix = globals.cameraViewMatrix.clone();

		//rotate camera
		float[] rotateMatrix = new float[16];
		Matrix.setIdentityM(rotateMatrix, 0);
		Matrix.rotateM(rotateMatrix, 0, cameraAngles.x, 1f, 0f, 0f);
		Matrix.rotateM(rotateMatrix, 0, cameraAngles.y, 0f, 1f, 0f);
		Matrix.rotateM(rotateMatrix, 0, cameraAngles.z, 0f, 0f, 1f);
		Matrix.multiplyMM(tempMatrix, 0, globals.cameraViewMatrix, 0, rotateMatrix, 0);
        globals.cameraViewMatrix = tempMatrix.clone();

		// Setup our screen width and height for ortho projection
		//Matrix.orthoM(orthoMatrix, 0, -mScreenWidth/2, mScreenWidth/2, -mScreenHeight/2, mScreenHeight/2, 0, 10);
		Matrix.orthoM(globals.orthoMatrix, 0, 0f, 5f * scale, -5f * scale, 0f, -1f, 100f);

		// Setup perspective projection matrix
		Matrix.perspectiveM(globals.perspectiveMatrix, 0, 45f * scale, 1f, 1f, -100f);
		tempMatrix = new float[16];
		Matrix.multiplyMM(tempMatrix, 0, globals.perspectiveMatrix, 0, globals.cameraViewMatrix, 0);//add camera matrix to perspective
        globals.perspectiveMatrix = tempMatrix.clone();

		if (perspectiveView){
			globals.viewProjMatrix = globals.perspectiveMatrix;
		}else{
			globals.viewProjMatrix = globals.orthoMatrix;
		}
	}

	public void ProcessKeyUp(int keyCode) {

		switch (keyCode){
			case KeyEvent.KEYCODE_DPAD_LEFT:
				keep.position.location.x -= .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				keep.position.location.x += .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				keep.position.location.y += 1f;
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				keep.position.location.y -= 1f;
				break;
			case 69://minus
				keep.position.location.z -= 1f;
				break;
			case 70://addition
				keep.position.location.z += 1f;
				break;
            case KeyEvent.KEYCODE_PERIOD:
                keep.position.angles.y += .1f;
                break;
            case KeyEvent.KEYCODE_COMMA:
                keep.position.angles.y -= .1f;
			case KeyEvent.KEYCODE_P:
				perspectiveView = !perspectiveView;
				break;
		}
	}

	public void ProcessKeyDown(int KeyCode, KeyEvent event){
	    testText = "";
        testText += (char) event.getUnicodeChar();
    }
}
