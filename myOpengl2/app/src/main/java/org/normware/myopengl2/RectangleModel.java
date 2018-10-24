package org.normware.myopengl2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Random;

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
    private FloatBuffer bumpTexBuffer; // Buffer for bump map texture coords array
    private ShortBuffer indexBuffer; // Buffer for vertex draw order index array
    private int textureIndex;
    private int bumpMapIndex = -1;
    public boolean transparent = true;
    public PointF size;

    private float[] vertices;

    // The order of vertexrendering for a quad
    short[] indices = new short[] {0, 1, 2, 0, 2, 3};

    float[] texCoords = { // Texture coords for the above face
            0.0f, 0.0f,  // A. left-bottom
            0.0f, 1.0f,  // B. left top
            1.0f, 1.0f,  // C. right-top
            1.0f, 0.0f   // D. right-bottom
    };

    float[] bumpTexCoords = { // Texture coords for the above face
            0.0f, 0.0f,  // A. left-bottom
            0.0f, 1.0f,  // B. left top
            1.0f, 1.0f,  // C. right-top
            1.0f, 0.0f   // D. right-bottom
    };

    public RectangleModel(PointF size, boolean transparent, boolean centered, boolean vertical) {
        this.size = size;
        if (vertical){
            // centered vertices
            if (centered){
                vertices = new float[]{
                    -size.x/2, size.y/2, 0,//top left
                    -size.x/2, -size.y/2, 0,//bottom left
                    size.x/2, -size.y/2, 0,//bottom right
                    size.x/2, size.y/2, 0};//top right
            }else {// left/top = 0,0
                vertices = new float[]{
                        0, size.y, 0, // 0. top left
                        0, 0, 0, // 1. bottom left
                        size.x, 0, 0, // 2. bottom right
                        size.x, size.y, 0};  // 3 top right
            }
        }else{//horizontal
            if (centered){
                // create flat rectangle
                // centered vertices
                vertices = new float[]{
                        -size.x/2, 0f, size.y/2,//bottom left
                        -size.x/2, 0f, -size.y/2,//top left
                        size.x/2, 0f, -size.y/2,//top right
                        size.x/2, 0f, size.y/2};//bottom right
            }else{// left/top = 0,0
                vertices = new float[]{
                        0f, 0f, size.y, // 0. bottom left
                        0f, 0f, 0f, // 1. top left
                        size.x, 0f, 0f, // 2. top right
                        size.x, 0f, size.y};  // 3 bottom right

            }
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

    public void RandomizeTextureCoords(int seed){
        // randomize bump map to blend tiles together
        Random r = new Random();
        int n = (r.nextInt(seed)) - (seed/2);
        texCoords[0] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        texCoords[1] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        texCoords[2] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        texCoords[3] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        texCoords[4] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        texCoords[5] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        texCoords[6] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        texCoords[7] += (float)n / 100;

        CreateTextureCoordsBuffer();

    }

    // Render this shape
    public void Draw(Globals globals, Vector3f position, Vector3f angles, float scale) {
        Draw(globals, position, angles, scale, false);
    }

    public void Draw(Globals globals, Vector3f position, Vector3f angles, float scale, boolean HUD) {

        // Matrix transformations
        float[] modelMatrix = new float[16];
        float[] finalMatrix;
        float[] projectionMatrix = new float[16];

        //translate rotate and scale
        Matrix.setIdentityM(modelMatrix, 0);//set to 0
        Matrix.translateM(modelMatrix, 0, position.x, -position.y, position.z);//move
        //rotate
        Matrix.rotateM(modelMatrix, 0, angles.x, 1f, 0f, 0f);
        Matrix.rotateM(modelMatrix, 0, angles.y, 0f, 1f, 0f);
        Matrix.rotateM(modelMatrix, 0, angles.z, 0f, 0f, 1f);
        //scale
        Matrix.scaleM(modelMatrix, 0, scale, scale, 1);//scale

        //choose ortho or perspective view matrix
        if (HUD){
            Matrix.multiplyMM(projectionMatrix, 0, globals.HUDMatrix, 0, modelMatrix, 0);//projection matrix
        }else{
            Matrix.multiplyMM(projectionMatrix, 0, globals.viewProjMatrix, 0, modelMatrix, 0);//projection matrix
        }

        finalMatrix = projectionMatrix.clone();//final matrix created

        GLES20.glEnable(GLES20.GL_TEXTURE_2D);  // Enable texture
        //texture filtering
        GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        //GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        //GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

        //GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIndex);// bind texture for drawing
        GLES20.glEnable(GLES20.GL_FRONT_AND_BACK);// draw front and back faces

        int shaderProgram, t1, t2;

        if (bumpMapIndex == -1){

            shaderProgram = GraphicTools.sp_Image;
            GLES20.glUseProgram(shaderProgram);//use shader programs
            // Get handle to textures locations
            int texLoc = GLES20.glGetUniformLocation (shaderProgram, "u_texture" );
            //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, globals.textureIDs[textureIndex]);
            // Set the sampler texture unit to 0, where we have saved the texture.
            GLES20.glUniform1i ( texLoc, 0);

        }else{//use bumpmap texture as well

            shaderProgram = GraphicTools.sp_ImageBump;
            GLES20.glUseProgram(shaderProgram);//use shader programs

            t1 = GLES20.glGetUniformLocation(shaderProgram, "u_texture");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, globals.textureIDs[textureIndex]);
            GLES20.glUniform1i(t1, 0);

            t2 = GLES20.glGetUniformLocation(shaderProgram, "u_bumptex");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, globals.textureIDs[bumpMapIndex]);
            GLES20.glUniform1i(t2, 1);

            // Get handle to texture coordinates location
            int bumpTexCoordLoc = GLES20.glGetAttribLocation(shaderProgram, "a_bumpCoord" );

            // Enable generic vertex attribute array
            GLES20.glEnableVertexAttribArray ( bumpTexCoordLoc );

            // Prepare the texturecoordinates
            GLES20.glVertexAttribPointer ( bumpTexCoordLoc, 2, GLES20.GL_FLOAT,
                    false,
                    0, bumpTexBuffer);

        }

        // transparency stuff
        if (transparent){
            GLES20.glEnable(GLES20.GL_BLEND);       // Turn blending on
            //gl.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);// transparency and lighting
        }

        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(shaderProgram, "u_MVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, finalMatrix, 0);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(shaderProgram, "a_Position");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Get handle to texture coordinates location
        int mTexCoordLoc = GLES20.glGetAttribLocation(shaderProgram, "a_texCoord" );

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray ( mTexCoordLoc );

        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer ( mTexCoordLoc, 2, GLES20.GL_FLOAT,
                false,
                0, texBuffer);

        /*// Get handle to textures locations
        int mSamplerLoc = GLES20.glGetUniformLocation (shaderProgram, "u_texture" );

        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i ( mSamplerLoc, 0);*/

        // Draw the triangle
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glDisableVertexAttribArray(mTexCoordLoc);

        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    }

    // Generate textures, returns next texture index
    public int LoadTexture(Globals globals, Context context, Bitmap bitmap, int textureIndex, float[] texCoords){//bind textures from bitmap, no bump map, GLText
        this.textureIndex = textureIndex;
        this.texCoords = texCoords;
        CreateTextureCoordsBuffer();

        BindTexture(globals, bitmap, textureIndex);

        return textureIndex++;
    }


    public int LoadTexture(Globals globals, Context context, int iD, int textureIndex){//load without bump map
        return LoadTexture(globals, context, iD, textureIndex, 0);
    }

    public int LoadTexture(Globals globals, Context context, int imageiD, int textureIndex, int bumpMapiD){//load with bump map
        int nextTextureIndex = textureIndex;

        try{
            // load image texture
            Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imageiD);

            this.textureIndex = textureIndex;
            nextTextureIndex += 1;

            //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            BindTexture(globals, bitmap, textureIndex);

            bitmap.recycle();

            // load bump map
            if (bumpMapiD > 0){

                CreateBumpTexCoordsBuffer();

                // load bump map image and bind it to a texture
                nextTextureIndex += 1;

                bitmap = BitmapFactory.decodeResource(context.getResources(), bumpMapiD);

                bumpMapIndex = textureIndex + 1;

                //GLES20.glActiveTexture(GLES20.GL_TEXTURE1);// texture1 used for multi texture shader

                BindTexture(globals, bitmap, bumpMapIndex);

                // We are done using the bitmap so we should recycle it.
                bitmap.recycle();



                /*GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.raw.catapult);
                BindTexture(globals, bitmap, 3);
                bitmap.recycle();*/


            }

        }catch (Exception e){
            Toast.makeText(context, "Unable to load image", Toast.LENGTH_LONG);
        }

        return nextTextureIndex;
    }

    private void BindTexture(Globals globals, Bitmap bitmap, int textureIndex){
        //int[] texId = new int[1];
        //texId[0] = textureIndex;
        //GLES20.glGenTextures(1, texId, 0);  // Generate texture-ID array

        // Bind texture to texturename
        //GLES20.glActiveTexture(GLES20.GL_TEXTURE0);// + textureIndex);
        //
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, globals.textureIDs[textureIndex]);

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        // We are done using the bitmap so we should recycle it.
        bitmap.recycle();
    }

    public void RandomizeBumpTex(int seed){
        // randomize bump map to blend tiles together
        Random r = new Random();
        int n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[0] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[1] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[2] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[3] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[4] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[5] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[6] += (float)n / 100;
        n = (r.nextInt(seed)) - (seed/2);
        bumpTexCoords[7] += (float)n / 100;

        CreateBumpTexCoordsBuffer();

    }

    private void CreateBumpTexCoordsBuffer(){
        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * BYTES_PER_FLOAT);
        tbb.order(ByteOrder.nativeOrder());
        bumpTexBuffer = tbb.asFloatBuffer();
        bumpTexBuffer.put(bumpTexCoords);
        bumpTexBuffer.position(0);
    }

}