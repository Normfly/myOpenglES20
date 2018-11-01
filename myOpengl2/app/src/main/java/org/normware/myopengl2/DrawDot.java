package org.normware.myopengl2;

import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.content.ContentValues.TAG;
import static org.normware.myopengl2.Constants.BYTES_PER_FLOAT;
import static org.normware.myopengl2.Constants.BYTES_PER_SHORT;

public class DrawDot {
    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private float[] vertices = new float[3];//x,y,z
    private float[] color = new float[]{1f, 1f, 1f, 1f};//used to draw white color

    public DrawDot() {//Vector3f location){
        vertices[0] = 0f;//location.x;
        vertices[1] = 0f;//location.y;
        vertices[2] = 0f;//location.z;

        // Setup vertex array buffer. Vertices in float. A float has 4 bytes
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder()); // Use native byte order
        vertexBuffer = vbb.asFloatBuffer(); // Convert from byte to float
        vertexBuffer.put(vertices);         // Copy data into buffer
        vertexBuffer.position(0);           // Rewind
    }

    public void Draw(Globals globals, Vector3f location) {

        float[] modelMatrix = new float[16];
        float[] projectionMatrix = new float[16];
        float[] finalMatrix = new float[16];

        //translate
        Matrix.setIdentityM(modelMatrix, 0);//set to 0

        Matrix.translateM(modelMatrix, 0, location.x,
                -location.y,
                location.z);//move

        //perspective view matrix
        Matrix.multiplyMM(projectionMatrix, 0, globals.viewProjMatrix, 0, modelMatrix, 0);//perspective/model/view projection matrix
        finalMatrix = projectionMatrix.clone();//final matrix created


        GLES20.glUseProgram(GraphicTools.sp_SolidColor);//use shader programs

        GLES20.glLineWidth(8);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        GLES20.glEnable(GLES20.GL_CCW);

        // Get handle to color
        int colorHandle = GLES20.glGetUniformLocation(GraphicTools.sp_SolidColor, "u_Color");
        // pass color info to shader program
        color = new float[]{0.0f, 1.0f, 0.0f, 1.0f};
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

        // Draw the point
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

    }

}
