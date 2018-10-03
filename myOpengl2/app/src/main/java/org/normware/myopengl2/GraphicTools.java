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


    /* SHADER Solid
     *
     * This shader is for rendering a colored primitive.
     *
     */
    public static String vs_SolidColor;/* =
            "uniform 	mat4 		uMVPMatrix;" +
                    "attribute 	vec4 		vPosition;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";*/

    public static String fs_SolidColor;/* =
            "precision mediump float;" +
                    "void main() {" +
                    "  gl_FragColor = vec4(0.5,0,0,1);" +
                    "}";*/

    public static int sp_Text;

    /* SHADER Text
     *
     * This shader is for rendering 2D text textures straight from a texture
     * Color and alpha blended.
     *
     */
    public static String vs_Text;/* =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec4 a_Color;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec4 v_Color;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "  v_Color = a_Color;" +
                    "}";*/
    public static String fs_Text;/* =
            "precision mediump float;" +
                    "varying vec4 v_Color;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord ) * v_Color;" +
                    "  gl_FragColor.rgb *= v_Color.a;" +
                    "}";*/


    /* SHADER Image
     *
     * This shader is for rendering 2D images straight from a texture
     * No additional effects.
     *
     */
    public static String vs_Image;/* =
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "attribute vec2 a_texCoord;" +
                    "varying vec2 v_texCoord;" +
                    "void main() {" +
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "  v_texCoord = a_texCoord;" +
                    "}";*/

    public static String fs_Image;/* =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D s_texture;" +
                    "void main() {" +
                    "  gl_FragColor = texture2D( s_texture, v_texCoord );" +
                    "}";*/

    public static String vs_Image_Lighting;
    public static String fs_Image_Lighting;

   /* //fragment shader with texture and alpha channel
    public static final String fs_Image =
            "precision medium float;" +
                    "uniform sampler2D s_texture;" +//texture
                    "uniform float red;" +
                    "uniform float green;" +
                    "uniform float blue;" +
                    "uniform float calpha;" +
                    "varying vec2 v_texCoord;" +//texcoord
                    "void main() {" +
                    " vec4 alpha = texture2D(s_texture, v_texCoord).aaaa;" +
                    " gl_FragColor = alpha*vec4(red,green,blue,calpha);" +
                    "}";*/

    /*precision mediump float;
    uniform float Opacity; // range 0.0 to 1.0
    varying vec2 v_texCoords;
    uniform sampler2D u_baseMap;

    void main(void)
    {
        gl_FragColor = texture2D(u_baseMap, v_texCoords) ;
        gl_FragColor.a *= Opacity;
    }*/

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


