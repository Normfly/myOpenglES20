package org.normware.myopengl2;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Random;

import static org.normware.myopengl2.Collisions.GetAngle;
import static org.normware.myopengl2.Constants.BYTES_PER_FLOAT;
import static org.normware.myopengl2.Constants.TOUCH_SCALE_FACTOR;

public class GLRenderer implements Renderer {

	//x left- / right+
	//y up-   / down+
	//z close+/ far-

    public int FPS;
    private int FPScounnter;
    public long lastFPSTime;
    public float updateTime = 1f;//update every 1 second
	public long lastUpdateTime;
	public String testText = "HELLO";
	public Globals globals = new Globals();

	private float z = 0;
	private Vector3f testLoc = new Vector3f(0f, 0f, 0f);

	//3d models
	public RectangleModel grass = new RectangleModel(new PointF(10f, 10f), false, true, false);
	public Model keep = new Model(false, true, 1.0f);
	GLText glText = new GLText();
	DrawDot dot = new DrawDot();

	Vector3f cameraAngles = new Vector3f(30f, 0f, 0f);
	Vector3f cameraPosition = new Vector3f(0f, 0f, -10f);
	float scale = 1.0f;
	public boolean perspectiveView = true;//perspective or ortho
	PointF lastScreenTouch = new PointF();

	private int texturePntr = 0;

	// Misc
	Context mContext;
	
	public GLRenderer(Context c)
	{
		mContext = c;
	}
	
	public void onPause()
	{
		/* Do stuff to pause the renderer */
	}
	
	public void onResume()
	{
		/* Do stuff to resume the renderer */
		lastFPSTime = System.currentTimeMillis();
		lastUpdateTime = System.currentTimeMillis();
	}
	
	@Override
	public void onDrawFrame(GL10 unused) {
		
		// Get the current time
    	long now = System.currentTimeMillis();

    	//calculate FPS
        FPScounnter += 1;
    	if (now >= lastFPSTime + 1000){// reset every 1 second
    	    lastFPSTime = now;
    	    FPS = FPScounnter;
    	    FPScounnter = 0;
        };
        
    	//update data
        if (now >= lastUpdateTime + (1000 * updateTime)){
            lastUpdateTime = now;
            Update();
        }

		// Render our example
		Render();

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		
		// We need to know the current width and height.
		globals.sceenWidth = width;
		globals.screenHeight = height;
		if (height > width){
			globals.aspectRatio = (float)width/height;
		}else{
			globals.aspectRatio = (float)height/width;
		}
		
		// Redo the Viewport, making it fullscreen.
		GLES20.glViewport(0, 0, (int)globals.sceenWidth, (int)globals.screenHeight);
		
		UpdateWorldMatrix();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1);

        SetupShaderPrograms();

        //load textures
		GLES20.glGenTextures(globals.textureIDs.length, globals.textureIDs, 0);  // Generate texture-ID array

		texturePntr = grass.LoadTexture(globals, mContext, R.raw.grass,texturePntr++);//load flat rectangle
        keep.LoadModel(globals, mContext, "keep",texturePntr++, true);//load 3d model from obj file

        glText.LoadFont(globals, mContext, Color.argb(255,255,255,255),
												Color.argb(0,0,0,0),
												true, .5f, texturePntr++);

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

		// Create textured shader that combines a bump map
		vertexShader = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER, GraphicTools.vs_Image_Bump);
		fragmentShader = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER, GraphicTools.fs_Image_Bump);
		GraphicTools.sp_ImageBump = GLES20.glCreateProgram();
		GLES20.glAttachShader(GraphicTools.sp_ImageBump, vertexShader);
		GLES20.glAttachShader(GraphicTools.sp_ImageBump, fragmentShader);
		GLES20.glLinkProgram(GraphicTools.sp_ImageBump);

		// Text shader
		int vshadert = GraphicTools.loadShader(GLES20.GL_VERTEX_SHADER,
				GraphicTools.vs_Text);
		int fshadert = GraphicTools.loadShader(GLES20.GL_FRAGMENT_SHADER,
				GraphicTools.fs_Text);

		GraphicTools.sp_Text = GLES20.glCreateProgram();
		GLES20.glAttachShader(GraphicTools.sp_Text, vshadert);
		GLES20.glAttachShader(GraphicTools.sp_Text, fshadert);
		GLES20.glLinkProgram(GraphicTools.sp_Text);

	}

	private void Render() {

	    testText = Integer.toString(FPS) + " FPS";

	    UpdateWorldMatrix();

		// clear Screen and Depth Buffer, we have set the clear color as black.
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		//draw 3d objects
		grass.Draw(globals, Vector3f.ZERO, Vector3f.ZERO, 1.0f);
		keep.DrawShaddow(globals, LocAngScale.ZERO_ONE());
		keep.Draw(globals, LocAngScale.ZERO_ONE());

        glText.DrawHUD(globals, testText, new PointF(0f, 0f));

	}

	private void Update(){

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
                cameraPosition.x += deltaX * TOUCH_SCALE_FACTOR;
                cameraPosition.y -= deltaY * TOUCH_SCALE_FACTOR;
                /*cameraAngles.x -= deltaY * TOUCH_SCALE_FACTOR;
                cameraAngles.y -= deltaX * TOUCH_SCALE_FACTOR;*/
                break;
        }
        // Save current x, y
        lastScreenTouch = new PointF(currentX, currentY);
	}

	public void UpdateWorldMatrix(){

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
		float[] moveMatrix = new float[16];
        Matrix.setIdentityM(moveMatrix, 0);
        Matrix.translateM(moveMatrix, 0, cameraPosition.x,
                                                cameraPosition.y,
                                                cameraPosition.z);//move

		Matrix.rotateM(moveMatrix, 0, cameraAngles.x, 1f, 0f, 0f);
		Matrix.rotateM(moveMatrix, 0, cameraAngles.y, 0f, 1f, 0f);
		Matrix.rotateM(moveMatrix, 0, cameraAngles.z, 0f, 0f, 1f);
		Matrix.multiplyMM(tempMatrix, 0, globals.cameraViewMatrix, 0, moveMatrix, 0);
        globals.cameraViewMatrix = tempMatrix.clone();

		// Setup our screen width and height for ortho projection
		//Matrix.orthoM(orthoMatrix, 0, -mScreenWidth/2, mScreenWidth/2, -mScreenHeight/2, mScreenHeight/2, 0, 10);
        Matrix.orthoM(globals.HUDMatrix, 0, 0f, globals.glScreenWidth, 0f, globals.glScreenHeight, 0.1f, -100f);//GetMatrix();
		Matrix.orthoM(globals.orthoMatrix, 0, 0f, globals.glScreenWidth * scale, globals.glScreenHeight * scale, 0f,
                -1f, 100f);

		// Setup perspective projection matrix
		Matrix.perspectiveM(globals.perspectiveMatrix, 0, 15f * scale, globals.aspectRatio, 0.1f, -100f);
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
            case KeyEvent.KEYCODE_COMMA:
                scale -= .1f;
                break;
            case KeyEvent.KEYCODE_PERIOD:
                scale += .1f;
                break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				cameraPosition.x -= .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				cameraPosition.x += .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				cameraPosition.y += .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				cameraPosition.y -= .1f;
				break;
			case 69://minus
				cameraPosition.z -= .1f;
				break;
			case 70://addition
				cameraPosition.z += .1f;
				break;
            case KeyEvent.KEYCODE_B:
                globals.debuging = !globals.debuging;
                break;
            case KeyEvent.KEYCODE_A:
                testLoc.x -= .01f;
                break;
            case KeyEvent.KEYCODE_D:
                testLoc.x += .01f;
                break;
            case KeyEvent.KEYCODE_W:
                testLoc.z += .01f;
                break;
            case KeyEvent.KEYCODE_S:
                testLoc.z -= .01f;
                break;
            case KeyEvent.KEYCODE_Q:
                testLoc.y -= .01f;
                break;
            case KeyEvent.KEYCODE_E:
                testLoc.y += .01f;
                break;
            case KeyEvent.KEYCODE_T:
                z += .1f;
                break;
            case KeyEvent.KEYCODE_G:
                z -= .1f;
                break;
			case KeyEvent.KEYCODE_1:
				globals.test.x += 1;
				break;
			case KeyEvent.KEYCODE_2:
				globals.test.x -= 1;
				break;
			case KeyEvent.KEYCODE_3:
				globals.test.y += 1;
				break;
			case KeyEvent.KEYCODE_4:
				globals.test.y -= 1;
				break;
            /*case KeyEvent.KEYCODE_COMMA:
                keep.position.angles.y -= .1f;*/
			case KeyEvent.KEYCODE_P:
				perspectiveView = !perspectiveView;
				break;
		}
	}

	public void ProcessKeyDown(int KeyCode, KeyEvent event){
	    /*testText = "";
        testText += (char) event.getUnicodeChar();*/
    }
}
