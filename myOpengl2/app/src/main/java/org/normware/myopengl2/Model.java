package org.normware.myopengl2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.support.annotation.NonNull;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import static javax.microedition.khronos.opengles.GL10.GL_BLEND;
import static javax.microedition.khronos.opengles.GL10.GL_REPEAT;
import static javax.microedition.khronos.opengles.GL10.GL_TEXTURE_2D;
import static javax.microedition.khronos.opengles.GL10.GL_TEXTURE_WRAP_S;
import static javax.microedition.khronos.opengles.GL10.GL_TEXTURE_WRAP_T;
import static org.normware.myopengl2.Constants.BYTES_PER_FLOAT;
import static org.normware.myopengl2.Constants.BYTES_PER_INT;
import static org.normware.myopengl2.Constants.BYTES_PER_SHORT;
import static org.normware.myopengl2.Constants.INPUT_BUFFER_SIZE;

public class Model {
    // blender OBJ export y up, write normals, include UVs, write materials, triangulate faces (extrude faces, then remove 0,0 vt's)


    private FloatBuffer vertexBuffer;  // Buffer for vertex-array
    private FloatBuffer normalBuffer; // Buffer for normals array
    private FloatBuffer texBuffer; // Buffer for textures
    private ShortBuffer indexBuffer;    // Buffer for index-array
    protected int vertexCount;
    protected int indexCount;
    protected int textureCount;
    protected int normalsCount;
    int textureIndex;
    InputStream textureInputStream;
    Bitmap bitmap;
    public boolean transparent;
    public boolean lighted;
    public float alpha;

    /*private int texPointer = 0;
    private int normalsPointer = 0;*/

    // load OBJ file (must include)
    // position/texture/normals
    // blender export options - write normals
    // include UV's
    // write materials
    // triangulate faces
    // Objects as OBJ Objects
    Model(boolean transparent, boolean lighted, float alpha){
        this.lighted = lighted;
        this.transparent = transparent;
        this.alpha = alpha;
    }

    public void LoadModel(Context context, String fileName, int textureIndex){
        //load file
        this.textureIndex = textureIndex;

        String data;
        try {
            InputStream is = context.getAssets().open(fileName + ".obj");
            readOBJText(context, is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readOBJText(Context context, @NonNull InputStream stream) throws IOException {
        // position/texture/normals
        // blender export options - write normals
        // include UV's
        // write materials
        // triangulate faces
        // Objects as OBJ Objects
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream), INPUT_BUFFER_SIZE);

        String line;
        List<Vector3f> vertices = new ArrayList<Vector3f>();
        List<PointF> textures = new ArrayList<PointF>();
        List<Vector3f> normals = new ArrayList<Vector3f>();
        List<Integer> indices = new ArrayList<Integer>();
        float[] verticesArray = null;
        float[] normalsArray = null;
        float[] textureArray = null;
        short[] indicesArray = null;
        String mtlFilename = null;
        List<String> facesArray = new ArrayList<>();

        try{

            while(true){
                line = reader.readLine();
                if (line == null){break;}
                String[] currentLine = line.split(" ");

                if (line.startsWith("v ")){
                    Vector3f vertex = new Vector3f(Float.parseFloat(currentLine[1]),
                                                    Float.parseFloat(currentLine[2]),
                                                    Float.parseFloat(currentLine[3]));
                    vertices.add(vertex);
                }else if (line.startsWith("vt ")){
                    PointF texture = new PointF(Float.parseFloat(currentLine[1]),
                                                    Float.parseFloat(currentLine[2]));
                    if (texture.x != 0.0f && texture.y != 0.0f){
                        textures.add(texture);// add texture coordinates as long as they are not 0.0 for unassigned texture coordinates
                    }
                }else if (line.startsWith("vn ")) {
                    Vector3f normal = new Vector3f(Float.parseFloat(currentLine[1]),
                            Float.parseFloat(currentLine[2]),
                            Float.parseFloat(currentLine[3]));
                    normals.add(normal);
                }else if(line.startsWith("mtllib")){
                    mtlFilename = currentLine[1];
                }else if (line.startsWith("f ")){
                    facesArray.add(line);
                }
            }

            textureArray = new float[(facesArray.size() * 3) * 2];//3 floats / 2 uv coordinates
            normalsArray = new float[facesArray.size() * 3 * 3];//3 floats / 3 indicies per face

            for (int face = 0; face < facesArray.size(); face++){
                line = facesArray.get(face);
                if (line.startsWith("f ")) {
                    String[] currentLine = line.split(" ");
                    String[] vertex1 = currentLine[1].split("/");
                    String[] vertex2 = currentLine[2].split("/");
                    String[] vertex3 = currentLine[3].split("/");

                    processVertex(vertex1, indices, textures, normals, textureArray, normalsArray);
                    processVertex(vertex2, indices, textures, normals, textureArray, normalsArray);
                    processVertex(vertex3, indices, textures, normals, textureArray, normalsArray);
                }
            }
            reader.close();

        } catch (Exception e){
            Toast.makeText(context, "Error loading 3d OBJ model", Toast.LENGTH_LONG);
        }

        verticesArray = new float[vertices.size() * 3];
        indicesArray = new short[indices.size()];

        int vertexPointer = 0;
        for (Vector3f vertex:vertices){
            verticesArray[vertexPointer++] = vertex.x;
            verticesArray[vertexPointer++] = vertex.y;
            verticesArray[vertexPointer++] = vertex.z;
        }

        for (int i = 0; i < indices.size(); i++){
            int ii = indices.get(i);
            short s = (short)ii;
            indicesArray[i] = s;//indices.get(i);
        }

        // Build non indexed vertex array
        float[] tempArray = verticesArray;
        verticesArray = new float[indicesArray.length * 3];//3 floats x,y,z per index
        for (int i = 0; i < indicesArray.length; i++){//once per x,y,z
            verticesArray[i * 3] = vertices.get(indices.get(i)).x;
            verticesArray[(i * 3) + 1] = vertices.get(indices.get(i)).y;
            verticesArray[(i * 3) + 2] = vertices.get(indices.get(i)).z;
        }

        // Build vertex buffer
        vertexCount = verticesArray.length;
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertexCount * BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asFloatBuffer();
        vertexBuffer.put(verticesArray);
        vertexBuffer.position(0);

        // Build texture buffer
        textureCount = textureArray.length;
        ByteBuffer tbb = ByteBuffer.allocateDirect(textureCount * BYTES_PER_FLOAT);
        tbb.order(ByteOrder.nativeOrder());
        texBuffer = tbb.asFloatBuffer();
        texBuffer.put(textureArray);
        texBuffer.position(0);

        // Build normals buffer
        normalsCount = normalsArray.length;
        vbb = ByteBuffer.allocateDirect(normalsCount * BYTES_PER_FLOAT);
        vbb.order(ByteOrder.nativeOrder());
        normalBuffer = vbb.asFloatBuffer();
        normalBuffer.put(normalsArray);
        normalBuffer.position(0);

        // this is not used, this was for drawing a indexed array, but then texture coordinates per vertex get messed up
        /*// Build index buffer
        indexCount = indicesArray.length;
        vbb = ByteBuffer.allocateDirect(indexCount * BYTES_PER_SHORT);
        vbb.order(ByteOrder.nativeOrder());
        indexBuffer = vbb.asShortBuffer();
        indexBuffer.put(indicesArray);
        indexBuffer.position(0);*/

        // Load textures
        //get the resource id from the file name
        //load file
        String textureFileName;
        String data;
        try {
            reader.close();
            InputStream is = context.getAssets().open(mtlFilename);
            reader = new BufferedReader(new InputStreamReader(is), INPUT_BUFFER_SIZE);

            // decode string
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("map_Kd")){
                    // Get texture file name
                    textureFileName = line;
                    int i = textureFileName.lastIndexOf("\\");
                    textureFileName = textureFileName.substring(i+1, textureFileName.length());//remove path
                    textureFileName = textureFileName.substring(0, textureFileName.lastIndexOf('.'));//remove file extension

                    // Load texture
                    int id = context.getResources().getIdentifier(textureFileName, "raw", context.getPackageName());// get ID
                    textureInputStream = context.getResources().openRawResource(id);
                    bitmap = BitmapFactory.decodeStream(textureInputStream);

                    //GLES20.glGenTextures(1, textureIDs, 0);  // Generate texture-ID array

                    // Create Nearest Filtered Texture and bind it to texture 0 (NEW)
                    GLES20.glBindTexture(GL_TEXTURE_2D, textureIndex);
                    /*GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
                    GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
                    GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
                    GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);*/
                    GLUtils.texImage2D(GL_TEXTURE_2D, 0, bitmap, 0);

                    bitmap.recycle();

                    textureInputStream.close();
                }
            }
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processVertex(String[] vertexData, List<Integer> indices, List<PointF> textures,
                                      List<Vector3f> normals, float[] textureArray, float[] normalsArray){

        int index = indices.size();
        int currrentVertexPointer = Integer.parseInt(vertexData[0]) - 1;
        indices.add(currrentVertexPointer);
        // texture coords
        PointF currentTex = textures.get(Integer.parseInt(vertexData[1]) - 1);
        textureArray[index * 2] = currentTex.x;
        textureArray[(index * 2) + 1] = 1 - currentTex.y;
        // normals
        Vector3f currentNorm = normals.get(Integer.parseInt(vertexData[2]) - 1);
        normalsArray[index * 3] = currentNorm.x;
        normalsArray[(index * 3) + 1] = currentNorm.y;
        normalsArray[(index * 3) + 2] = currentNorm.z;
    }

    // Render this shape
    public void draw(Globals globals, PosAngScale modelPos) {

        // Matrix transformations
        //Set a mModelMatrix to identity Matrix
        float[] modelMatrix = new float[16];
        float[] modelViewMatrix = new float[16];
        float[] finalMatrix;
        float[] projectionMatrix = new float[16];
        float[] tempMatrix = new float[16];

        //translate scale and rotate
        Matrix.setIdentityM(modelMatrix, 0);//set to 0
        Matrix.scaleM(modelMatrix, 0, modelPos.scales.x,
                                                modelPos.scales.y,
                                                modelPos.scales.z);//scale
        Matrix.translateM(modelMatrix, 0, modelPos.location.x,
                                                    -modelPos.location.y,
                                                    modelPos.location.z);//move
        //rotate
        Matrix.rotateM(modelMatrix, 0, modelPos.angles.x, 1f, 0f, 0f);
        Matrix.rotateM(modelMatrix, 0, modelPos.angles.y, 0f, 1f, 0f);
        Matrix.rotateM(modelMatrix, 0, modelPos.angles.z, 0f, 0f, 1f);
        //choose ortho or perspective view matrix
        Matrix.multiplyMM(projectionMatrix, 0, globals.viewProjMatrix, 0, modelMatrix, 0);//perspective/model/view projection matrix
        finalMatrix = projectionMatrix.clone();//final matrix created
        Matrix.multiplyMM(tempMatrix, 0, globals.cameraViewMatrix, 0, modelMatrix, 0);//model/view matrix
        modelViewMatrix = tempMatrix.clone();//movel/view matrix created no projection

        GLES20.glFrontFace(GLES20.GL_CCW);    // Front face in counter-clockwise orientation
        GLES20.glEnable(GLES20.GL_TEXTURE_2D);
        //GLES20.glEnable(GLES20.GL_FRONT_AND_BACK);
        GLES20.glEnable(GLES20.GL_CULL_FACE);//draw front faces only
        GLES20.glEnable(GLES20.GL_FRONT);//draw front faces only
        //GLES20.glDisable(GLES20.GL_BACK);
        GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
        GLES20.glTexParameterf(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
        GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameterf(GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIndex);// bind texture for drawing

        GLES20.glUseProgram(GraphicTools.sp_ImageLighting);//which shaders to use

        // Pass matrix model and model + projection matrix to shaders
        // Get handle to view matrix
        int uMVmatrix = GLES20.glGetUniformLocation(GraphicTools.sp_ImageLighting, "u_MVMatrix");//load model/view matrix
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(uMVmatrix, 1, false, modelViewMatrix , 0);
        // Get handle to shape's transformation matrix
        int uMVPmatrix = GLES20.glGetUniformLocation(GraphicTools.sp_ImageLighting, "u_MVPMatrix");//load model/view/projection matrix
        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(uMVPmatrix, 1, false, finalMatrix, 0);

        // opacity
        int aAlpha = GLES20.glGetAttribLocation(GraphicTools.sp_ImageLighting, "a_alpha");
        GLES20.glEnableVertexAttribArray(aAlpha);
        GLES20.glVertexAttrib1f(aAlpha, alpha);

        // directional lighting direction, 4th is brightness
        int aColor = GLES20.glGetAttribLocation(GraphicTools.sp_ImageLighting, "a_Color");
        // Apply the color to the shader programs
        // Lighting
        float[] ambientLight = globals.lightAmbient.clone();
        if (!lighted){
            ambientLight[3] = 1.0f;// not lighted, set ambient lighting full brightness
        }
        GLES20.glVertexAttrib4fv(aColor, ambientLight, 0);

        //light direction
        //int uLightDir = GLES20.glGetUniformLocation(GraphicTools.sp_ImageLighting, "u_LightDir");
        //GLES20.glUniform3fv(uLightDir, 1, globals.lightDirection, 0);

        //light position
        int uLightPos = GLES20.glGetUniformLocation(GraphicTools.sp_ImageLighting, "u_LightPos");
        GLES20.glUniform4fv(uLightPos, 1, globals.lightPosition, 0);

        // get handle to vertex shader's vPosition member
        int aPosition = GLES20.glGetAttribLocation(GraphicTools.sp_ImageLighting, "a_Position");
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray(aPosition);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(aPosition, 3,
                GLES20.GL_FLOAT, false,
                0, vertexBuffer);

        // Texture and texture coordinates
        // Get handle to texture coordinates location
        int aTexCoord = GLES20.glGetAttribLocation(GraphicTools.sp_ImageLighting, "a_texCoord" );
        // Enable generic vertex attribute array
        GLES20.glEnableVertexAttribArray (aTexCoord);
        // Prepare the texturecoordinates
        GLES20.glVertexAttribPointer (aTexCoord, 2, GLES20.GL_FLOAT,
                false,
                0, texBuffer);

        // Get handle to textures locations
        int uTexture = GLES20.glGetUniformLocation (GraphicTools.sp_ImageLighting, "u_texture" );
        // Set the sampler texture unit to 0, where we have saved the texture.
        GLES20.glUniform1i (uTexture, 0);

        // normals stuff
        // normals program handler
        int aNormal = GLES20.glGetAttribLocation(GraphicTools.sp_ImageLighting, "a_Normal");
        GLES20.glEnableVertexAttribArray(aNormal);
        // load normals buffer
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, 0, normalBuffer);

        if (transparent){
            GLES20.glEnable(GLES20.GL_BLEND);       // Turn blending on
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);// transparency and lighting
        }else{
            GLES20.glDisable(GLES20.GL_BLEND);
        }

        // Draw the model
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES,0, vertexCount/3);
        /*GLES20.glDrawElements(GLES20.GL_TRIANGLES, indexCount,
                GLES20.GL_UNSIGNED_SHORT, indexBuffer);// draw indexed buffer*/


        // Disable vertex array
        GLES20.glDisableVertexAttribArray(aAlpha);
        GLES20.glDisableVertexAttribArray(aColor);
        GLES20.glDisableVertexAttribArray(aPosition);
        GLES20.glDisableVertexAttribArray(aTexCoord);
        GLES20.glDisableVertexAttribArray(aNormal);
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_BLEND);// disable blending
        //GLES20.glDisable(GLES20.GL_LIGHTING);// disable lighting

    }

}