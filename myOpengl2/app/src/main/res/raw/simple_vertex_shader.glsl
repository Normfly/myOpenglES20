attribute vec4 a_Position;
uniform mat4 u_MVPMatrix;// model/view/projection matrix

void main()
{
  gl_Position = u_MVPMatrix * a_Position;
  //gl_Position = a_Position;
  gl_PointSize = 50.0;
}
