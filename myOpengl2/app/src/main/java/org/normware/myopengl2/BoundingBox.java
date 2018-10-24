package org.normware.myopengl2;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static org.normware.myopengl2.Constants.BYTES_PER_FLOAT;
import static org.normware.myopengl2.Constants.BYTES_PER_SHORT;

public class BoundingBox {

    public int hitLocation = 0;
    public Cube dimentions;
    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private ShortBuffer indexBuffer; // Buffer for vertex draw order index array
    private float[] vertices = new float[24];//8 points(4front + 4back) x 3xyz
    private short[] indices = new short[]{0, 1, 1, 2, 2, 3, 3, 0,//front plane
                                            3, 7, 7, 6, 6, 2,//right plane 7=right/top/back
                                            7, 4, 4, 5, 5, 6,//back plane 4=left/top/back 5= left/bottom/back 6=right/bottom/back
                                            0, 4, 1, 5};//left plane
    private float[] color = new float[]{1f, 1f, 1f, 1f};//used to draw bounding box for debuging white color

    /*public BoundingBox(Vector3f center, Vector3f size){
        BoundingBox(new Cube(center, size));
    }*/

    public BoundingBox(Cube dimentions){//}, Vector3f scales){
        this.dimentions = dimentions;

        BuildBox();//scales);

    }

    private void BuildBox(){//Vector3f scales){

        //dimentions.ReScale(scales);//rescale bounding box

        int i = 0;// vertices index

        for (int corner = 0; corner < 8; corner++){//2 planes front/back with 4 corners each
            Vector3f v = dimentions.GetVector3f(corner);
            vertices[i++] = v.x;
            vertices[i++] = -v.y;
            vertices[i++] = v.z;
        }

        // Setup vertex array buffer. Vertices in float. A float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(indices.length * BYTES_PER_SHORT);
        dlb.order(ByteOrder.nativeOrder());
        indexBuffer = dlb.asShortBuffer();
        indexBuffer.put(indices);
        indexBuffer.position(0);
    }

    /*public void Resize(Vector3f scales){
       BuildBox(scales);
    }*/

    public void Draw(float[] viewMatrix, LocAngScale position) {

        // Matrix transformations
        float[] modelMatrix = new float[16];
        float[] finalMatrix;
        float[] projectionMatrix = new float[16];

        //translate and scale
        Matrix.setIdentityM(modelMatrix, 0);//set to 0
        //Matrix.scaleM(modelMatrix, 0, scales.x, scales.x, scales.z);//scales
        Matrix.translateM(modelMatrix, 0, position.location.x,
                position.location.y, position.location.z);//move

        //rotate
        Matrix.rotateM(modelMatrix, 0, position.angles.x, 1f, 0f, 0f);
        Matrix.rotateM(modelMatrix, 0, -position.angles.y, 0f, 1f, 0f);
        Matrix.rotateM(modelMatrix, 0, -position.angles.z, 0f, 0f, 1f);

        //scale
        Matrix.scaleM(modelMatrix, 0, position.scales.x,
                position.scales.y,
                position.scales.z);//scale

        //choose ortho or perspective view matrix
        Matrix.multiplyMM(projectionMatrix, 0, viewMatrix, 0, modelMatrix, 0);//projection matrix

        finalMatrix = projectionMatrix.clone();//final matrix created

        GLES20.glUseProgram(GraphicTools.sp_SolidColor);//use shader programs

        GLES20.glLineWidth(8);
        //GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Get handle to color
        int colorHandle = GLES20.glGetUniformLocation(GraphicTools.sp_SolidColor, "u_Color");
        // pass color info to shader program
        GLES20.glUniform4fv(colorHandle, 1, color, 0);


        // Get handle to shape's transformation matrix
        int mtrxhandle = GLES20.glGetUniformLocation(GraphicTools.sp_SolidColor, "u_MVPMatrix");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mtrxhandle, 1, false, finalMatrix, 0);

        // get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(GraphicTools.sp_SolidColor, "a_Position");

        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Draw the box
        GLES20.glDrawElements(GLES20.GL_LINES, indices.length,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);

        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }



}
