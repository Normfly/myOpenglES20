precision mediump float;

uniform sampler2D u_bumptex;
//uniform sampler2D u_texture[2];
uniform sampler2D u_texture;

varying vec2 v_texCoord;
varying vec2 v_bumpCoord;




void main()
{
    vec4 bumpColor = texture2D(u_bumptex, v_bumpCoord);//v_bumpCoord);// get bump map color, just use green channel for brightness
    //vec4 texColor = texture2D (u_texture, v_texCoord);
    //gl_FragColor = texture2D( u_texture[0], v_texCoord );// * bumpColor.g;//adjust brightness with bumpmap green channel
    //gl_FragColor = texture2D(u_bumptex, v_texCoord);
    gl_FragColor = texture2D(u_texture, v_texCoord) * bumpColor.g;
}