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
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static android.opengl.GLES20.GL_CLAMP_TO_EDGE;
import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_RGBA;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_S;
import static android.opengl.GLES20.GL_TEXTURE_WRAP_T;
import static android.opengl.GLES20.GL_UNSIGNED_BYTE;
import static org.normware.myopengl2.Collisions.GetAngle;
import static org.normware.myopengl2.Collisions.RotatePointF;
import static org.normware.myopengl2.Constants.BYTES_PER_FLOAT;
import static org.normware.myopengl2.Constants.TOUCH_SCALE_FACTOR;

public class GLRenderer implements Renderer {

	//x left- / right+
	//y up-   / down+
	//z close+/ far-

    private boolean mHasDepthTextureExtension = false;
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
    public RectangleModel shadowRec = new RectangleModel(new PointF(1f, 1f), true, true, false);
    public RectangleModel sky = new RectangleModel(new PointF(1f, 1f), false, false, true);
    public Model catapult = new Model(false, true, 1.0f);
    public Map map = new Map();
	GLText glText = new GLText();
	DrawDot dot = new DrawDot();

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
		globals.screenWidth = width;
		globals.screenHeight = height;
		if (height > width){
			globals.aspectRatio = (float)width/height;
			globals.revAspectRatio = (float)height/width;
			globals.glScreenWidth = globals.glScreenSize * globals.aspectRatio;
			globals.glScreenHeight = globals.glScreenSize;
		}else{
			globals.aspectRatio = (float)height/width;
			globals.revAspectRatio = (float)width/height;
            globals.glScreenWidth = globals.glScreenSize;
            globals.glScreenHeight = globals.glScreenSize * globals.aspectRatio;
		}
		
		// Redo the Viewport, making it fullscreen.
		GLES20.glViewport(0, 0, (int)globals.screenWidth, (int)globals.screenHeight);

		UpdateWorldMatrix();

        GenerateShadowFBO();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        SetupShaderPrograms();


        //load textures
		GLES20.glGenTextures(globals.textureIDs.length, globals.textureIDs, 0);  // Generate texture-ID array
		//modelRec.LoadTexture(mContext, R.drawable.ic_launcher, texturePntr++);

        //if going more than 10, make sure to update textureIDs

        texturePntr = 1;//save 0 for shadow
		texturePntr = map.Load(globals, mContext, texturePntr);
        sky.LoadTexture(globals, mContext, R.raw.sky, texturePntr++);

        glText.LoadFont(globals, mContext, Color.argb(255,255,255,255),
												Color.argb(0,0,0,0),
												true, .5f, texturePntr++);

        catapult.LoadModel(globals, mContext, "Catapult", texturePntr++, true);

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
	    globals.lightPosition[0] = globals.test.x;
	    globals.lightPosition[1] = globals.test.y;
	    globals.lightPosition[2] = globals.test.z;



	    UpdateWorldMatrix();

	    //create shadows, and draw them to the render buffer and textureIDs[0], not to the screen yet
        EraseShadows();
        catapult.DrawShadow(globals, LocAngScale.ZERO_ONE());

        // Bind the default framebuffer (to render to the screen) - indicated by '0', this has been added because of shadow map FBO
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

		// clear Screen and Depth Buffer, we have set the clear color as black.
        // Set the clear color to black
        GLES20.glClearColor(0.0f, 0.5f, 0.0f, 1);
		GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		//draw 3d objects

        //draw map/floor
		map.Draw(globals);

        //draw shadows
        shadowRec.DrawShadow(globals);

        //draw full textured objects
        catapult.Draw(globals, LocAngScale.ZERO_ONE());

        glText.DrawHUD(globals, testText, new PointF(0f, 0f));

        //collision test
        //dot.Draw(globals, testLoc);
        //testText = Integer.toString(wall.CheckHitLocation(testLoc));
		//testText = Boolean.toString(wall.CheckHit(testLoc));

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
                globals.cameraPosition.x += deltaX * TOUCH_SCALE_FACTOR;
                globals.cameraPosition.y -= deltaY * TOUCH_SCALE_FACTOR;
                /*cameraAngles.x -= deltaY * TOUCH_SCALE_FACTOR;
                cameraAngles.y -= deltaX * TOUCH_SCALE_FACTOR;*/
                break;
        }
        // Save current x, y
        lastScreenTouch = new PointF(currentX, currentY);
	}

	public void UpdateWorldMatrix(){

		float[] tempMatrix = new float[16];

		Vector3f camera = globals.cameraPosition.Copy();
		camera.Multiply(globals.scale);

		//set camera position and angles
		Matrix.setLookAtM(globals.cameraViewMatrix, 0, camera.x, -camera.y, camera.z,
                                                        0f, 0f, 0f,
                                                            0f, 1f, 0.0f);

		// Setup our screen width and height for ortho projection
        Matrix.orthoM(globals.HUDMatrix, 0, 0f, globals.glScreenWidth, 0f, globals.glScreenHeight, 0.01f, -100f);//GetMatrix();
		Matrix.orthoM(globals.orthoMatrix, 0, -(globals.glScreenWidth/2) * globals.scale, (globals.glScreenWidth/2) * globals.scale,
                                                    (globals.glScreenHeight/2) * globals.scale, -(globals.glScreenHeight/2) * globals.scale,
                                                            -1f, 100f);

		// Setup perspective projection matrix
		Matrix.perspectiveM(globals.perspectiveMatrix, 0, 45, globals.aspectRatio, 0.1f, -100f);
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
                globals.scale -= .01f;
                break;
            case KeyEvent.KEYCODE_PERIOD:
                globals.scale += .01f;
                break;
			case KeyEvent.KEYCODE_DPAD_LEFT:
				globals.test.x -= .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_RIGHT:
				globals.test.x += .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_UP:
				globals.test.y += .1f;
				break;
			case KeyEvent.KEYCODE_DPAD_DOWN:
				globals.test.y -= .1f;
				break;
			case 69://minus
				globals.test.z -= .1f;
				break;
			case 70://addition
				globals.test.z += .1f;
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

    public void EraseShadows(){
        // setup to render to frame buffer instead of screen from the sun's point of view, to textureID's[0]
        // bind the generated framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, globals.fb[0]);

        GLES20.glViewport(0, 0, globals.screenWidth, globals.screenHeight);

        // Clear color and buffers
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
    }

    // create a frame buffer object, which will render to textureIDs[0] for later use in shadow mapping
    public void GenerateShadowFBO()
    {
        // create a framebuffer object
        GLES20.glGenFramebuffers(1, globals.fb, 0);

        // create render buffer and bind 16-bit depth buffer
        GLES20.glGenRenderbuffers(1, globals.depthRb, 0);
        GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, globals.depthRb[0]);
        GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, globals.screenWidth, globals.screenHeight);

        // Try to use a texture depth component
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, globals.textureIDs[0]);

        // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        // Remove artifact on the edges of the shadowmap
        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );
        GLES20.glTexParameteri( GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, globals.fb[0]);

        if (!mHasDepthTextureExtension) {
            GLES20.glTexImage2D( GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, globals.screenWidth, globals.screenHeight, 0,
                                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);

            // specify texture as color attachment
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, globals.textureIDs[0], 0);

            // attach the texture to FBO depth attachment point
            // (not supported with gl_texture_2d)
            GLES20.glFramebufferRenderbuffer(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, globals.depthRb[0]);
        }
        else {
            // Use a depth texture
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, globals.screenWidth, globals.screenHeight, 0,
                                GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);

            // Attach the depth texture to FBO depth attachment point
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, globals.textureIDs[0], 0);
        }

        // check FBO status
        int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
        if(FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
            Log.e(TAG, "GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
            throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
        }
    }
}
