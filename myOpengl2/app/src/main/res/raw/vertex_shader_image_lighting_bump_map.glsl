precision mediump float;

uniform mat4 u_MVPMatrix;// model/view/projection matrix
uniform mat4 u_MVMatrix;// model/view matrix

attribute vec4 a_Position;
attribute vec2 a_texCoord;
//attribute vec2 a_bumpCoord;
attribute vec3 a_Normal;
attribute vec4 a_Color;
attribute float a_alpha;

varying vec2 v_bumpCoord;// bump map uv coordinates
varying vec2 v_texCoord;// texture uv coordinates
varying vec3 v_Normal;// texture normals before translation/scale/rotation(which way the face is pointing for lighting brightness reflection)
varying vec3 v_Position;// vector position before translation/scale/rotation
varying vec4 v_Color;// ambient light color, 4th position is brightness
varying float v_alpha;// used for opacity, must be passed in or it defaults to 0, wich is fully opaque (invisible)
// u_LightPos directional light position, 4th position is brightness

void main() {
 // Transform the normal's orientation into eye space.
 v_Normal = normalize(vec3(u_MVMatrix * vec4(a_Normal, 0.0)));
 v_Color = a_Color;
 v_Position = vec3(u_MVPMatrix * a_Position);
 gl_Position = u_MVPMatrix * a_Position;
 v_texCoord = a_texCoord;
 v_bumpCoord = a_texCoord;//a_bumpCoord;
 v_alpha = a_alpha;
}