precision mediump float;

varying vec2 v_texCoord;
varying vec3 v_Position;        // Interpolated position for this fragment.
varying vec3 v_Normal;          // Interpolated normal for this fragment.
varying vec4 v_Color;           // light color and ambient light brightness
varying float v_alpha;          // used for opacity


uniform sampler2D u_texture;
uniform vec4 u_LightPos;        // The position of the light in eye space. 4th position is brightness
//uniform vec3 u_LightDir;        // The direction of the light

void main()
{
        // Will be used for attenuation.
        vec3 lightPos = vec3(u_LightPos.x, u_LightPos.y, u_LightPos.z);
        float brightness = 1.0 - u_LightPos.w;
        float distance = length(lightPos - v_Position);// distance between light position and vertex

        // Get a lighting direction vector from the light to the vertex.
        vec3 lightVector = normalize(lightPos + v_Position);

        // Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
        // pointing in the same direction then it will get max illumination.
        float diffuse = max(dot(v_Normal, lightVector), 0.1);

        // Add attenuation.
        diffuse = diffuse * (1.0 / (1.0 + (brightness * distance)));

        // Add ambient lighting
        diffuse = diffuse + v_Color.w;

        // max lighting add to 1.0
        diffuse = min(1.0, diffuse);

        // Multiply the color by the diffuse illumination level and texture value to get final output color.
        vec4 texColor = texture2D(u_texture, v_texCoord);// get texture color
        float originalAlpha = texColor.w;//record original alpha channel, so object doesn't become opaque, unless v_alpha > 0
        gl_FragColor = (v_Color * diffuse * texColor);// add ambient and directional lighting (modifies alpha channel as well, fix in next line)
        gl_FragColor.a = originalAlpha;// return original alpha channel

        if (gl_FragColor.a > 0.0)//make non transparent pixels opaque
        {
          gl_FragColor.a *= v_alpha;//opacity value
        }
}