package org.normware.myopengl2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static javax.microedition.khronos.opengles.GL10.GL_REPEAT;
import static javax.microedition.khronos.opengles.GL10.GL_TEXTURE_2D;
import static javax.microedition.khronos.opengles.GL10.GL_TEXTURE_WRAP_S;
import static javax.microedition.khronos.opengles.GL10.GL_TEXTURE_WRAP_T;
import static org.normware.myopengl2.Constants.BYTES_PER_FLOAT;
import static org.normware.myopengl2.Constants.BYTES_PER_SHORT;

/*
 * A square drawn in 2 triangles (using TRIANGLE_STRIP).
 */
public class RectangleModel {
    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private FloatBuffer texBuffer;    // Buffer for texture-coords-array
    private ShortBuffer indexBuffer; // Buffer for vertex draw order index array
    private int textureIndex;
    public boolean transparent = true;
    public PointF size;
    public boolean lighted;
    public PosAngScale posAngScale = new PosAngScale(0, 0, 0, 0, 0, 0, 1, 1, 1);

    private float[] vertices = {  // Vertices for the square
            -1.0f, -1.0f,  0.0f,  // 0. left-bottom
            1.0f, -1.0f,  0.0f,  // 1. right-bottom
            -1.0f,  1.0f,  0.0f,  // 2. left-top
            1.0f,  1.0f,  0.0f   // 3. right-top
    };

    // The order of vertexrendering for a quad
    short[] indices = new short[] {0, 1, 2, 0, 2, 3};

    float[] texCoords = { // Texture coords for the above face
            0.0f, 0.0f,  // A. left-bottom
            0.0f, 1.0f,  // B. left top
            1.0f, 1.0f,  // C. right-top
            1.0f, 0.0f   // D. right-bottom
    };

    public RectangleModel(PointF size, boolean transparent, boolean lighted, boolean centered) {
        this.size = size;
        this.lighted = lighted;
        if (centered){
            // centered vertices
            vertices = new float[]{
                    -size.x/2, size.y/2, 0,//top left
                    -size.x/2, -size.y/2, 0,//bottom left
                    size.x/2, -size.y/2, 0,//bottom right
                    size.x/2, size.y/2, 0};//top right
        }else{// left/top = 0,0
            vertices = new float[]{
                    0, size.y, 0, // 0. top left
                    0, 0, 0, // 1. bottom left
                    size.x, 0, 0, // 2. bottom right
                    size.x, size.y,0};  // 3 top right
        }

        this.transparent = transparent;

        // Setup vertex array buffer. Vertices in float. A float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind

        CreateTextureCoordsBuffer();

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * BYTES_PER_SHORT);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);

    }

    public void UpdateTextureCoords(float[] texCoords){
        this.texCoords = texCoords;
        CreateTextureCoordsBuffer();
    }

    private void CreateTextureCoordsBuffer(){
        // Setup texture-coords-array buffer, in float. An float has 4 bytes
        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * BYTES_PER_FLOAT);
        tbb.order(ByteOrder.nativeOrder());
        texBuffer = tbb.asFloatBuffer();
        texBuffer.put(texCoords);
        texBuffer.position(0);
    }

    // Render this shape
    public void Draw(float[] viewMatrix, Vector3f position, float scale) {

        // Matrix transformations
        float[] modelMatrix = new float[16];
        float[] finalMatrix;
        float[] projectionMatrix = new float[16];

        //translate and scale
        Matrix.setIdentityM(modelMatrix, 0);//set to 0
        Matrix.scaleM(modelMatrix, 0, scale, scale, 1);//scale
        Matrix.translateM(modelMatrix, 0, position.x, position.y, position.z);//move

        //choose ortho or perspective view matrix
        Matrix.multiplyMM(projectionMatrix, 0, viewMatrix, 0, modelMatrix, 0);//projection matrix

        finalMatrix = projectionMatrix.clone();//final matrix created

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);  // Enable texture
        //texture filtering
        GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        //GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        //GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIndex);// bind texture for drawing

        // lighted
        if (lighted){
            //GLES20.glEnable(GL10.GL_LIGHTING);
        }else{
            //GLES20.glDisable(GL10.GL_LIGHTING);
        }

        GLES20.glEnable(GLES20.GL_FRONT_AND_BACK);// draw front and back faces

        GLES20.glUseProgram(GraphicTools.sp_Image);//use shader programs

        // transparency stuff
        if (transparent){
            GLES20.glEnable(GLES20.GL_BLEND);       // Turn blending on
            //gl.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);// transparency and lighting
        }

        /*// Get handle to color
        int colorHandle = GLES20.glGetUniformLocation(GraphicTools.sp_Image, "a_Color");
        // pass color info to shader program
        GLES20.glUniform4fv(colorHandle, 1, color, 0);*/


        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(GraphicTools.sp_Image, "uMVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, finalMatrix, 0);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(GraphicTools.sp_Image, "vPosition");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(GraphicTools.sp_Image, "a_texCoord" );

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, texBuffer);

        // Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (GraphicTools.sp_Image, "s_texture" );

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);


    }

    // Generate textures
    public void LoadTexture(Context context, int iD, int textureIndex, float[] texCoords){

        this.texCoords = texCoords;

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iD);

        LoadTexture(context, bitmap, textureIndex);
    }


    public void LoadTexture(Context context, int iD, int textureIndex){
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), iD);

        LoadTexture(context, bitmap, textureIndex);
    }

    public void LoadTexture(Context context, Bitmap bitmap, int textureIndex, float[] texCoords) {

        this.texCoords = texCoords;
        CreateTextureCoordsBuffer();

        LoadTexture(context, bitmap, textureIndex);
    }

    public void LoadTexture(Context context, Bitmap bitmap, int textureIndex) {
            this.textureIndex = textureIndex;

            // Bind texture to texturename
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIndex);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // We are done using the bitmap so we should recycle it.
            bitmap.recycle();

    }
}