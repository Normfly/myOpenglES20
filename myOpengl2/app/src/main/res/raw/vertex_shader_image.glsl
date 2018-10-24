uniform mat4 u_MVPMatrix;

attribute vec4 a_Position;
attribute vec2 a_texCoord;

varying vec2 v_texCoord;
 
void main() {
 gl_Position = u_MVPMatrix * a_Position;
 v_texCoord = a_texCoord;
}