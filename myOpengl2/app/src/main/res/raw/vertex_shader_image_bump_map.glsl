uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;
attribute vec2 a_texCoord;
attribute vec2 a_bumpCoord;

varying vec2 v_texCoord;
varying vec2 v_bumpCoord;
 
void main() {
 gl_Position = u_MVPMatrix * a_Position;
 v_texCoord = a_texCoord;
 v_bumpCoord = a_bumpCoord;
}