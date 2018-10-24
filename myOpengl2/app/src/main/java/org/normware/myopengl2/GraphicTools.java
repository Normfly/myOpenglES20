package org.normware.myopengl2;

import android.content.Context;
import android.content.res.Resources;
import android.opengl.GLES20;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class GraphicTools {

    // Program variables
    public static int sp_SolidColor;
    public static int sp_Image;
    public static int sp_ImageLighting;
    //public static int sp_ImageLightingBump;
    public static int sp_ImageBump;
    public static String vs_SolidColor;
    public static String fs_SolidColor;
    public static int sp_Text;
    public static String vs_Text;
    public static String fs_Text;
    public static String vs_Image;
    public static String fs_Image;
    public static String vs_Image_Lighting;
    public static String fs_Image_Lighting;
    public static String vs_Image_Bump;
    public static String fs_Image_Bump;

    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        // return the shader
        return shader;
    }

    public static void ReadShaderText(Context context){
       vs_Text = ReadTextFileFromResource(context, R.raw.vertex_shader_text);
       fs_Text = ReadTextFileFromResource(context, R.raw.fragment_shader_text);
       vs_Image = ReadTextFileFromResource(context, R.raw.vertex_shader_image);
       fs_Image = ReadTextFileFromResource(context, R.raw.fragment_shader_image);
       vs_SolidColor = ReadTextFileFromResource(context, R.raw.simple_vertex_shader);
       fs_SolidColor = ReadTextFileFromResource(context, R.raw.simple_fragment_shader);
       vs_Image_Lighting = ReadTextFileFromResource(context, R.raw.vertex_shader_image_lighting);
       fs_Image_Lighting = ReadTextFileFromResource(context, R.raw.fragment_shader_image_lighting);
       vs_Image_Bump = ReadTextFileFromResource(context, R.raw.vertex_shader_image_bump_map);
       fs_Image_Bump = ReadTextFileFromResource(context, R.raw.fragment_shader_image_bump_map);
    }

    public static String ReadTextFileFromResource(Context context, int resourceId) {
        StringBuilder body = new StringBuilder();

        try {
            InputStream inputStream = context.getResources().openRawResource(resourceId);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String nextLine;
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not open resource:" + resourceId, e);
        } catch (Resources.NotFoundException nfe) {
            throw new RuntimeException("Resource not found :" + resourceId, nfe);
        }
        return body.toString();
    }
}


