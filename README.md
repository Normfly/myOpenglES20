# myOpenglES20
Opengl ES 2.0 starter. Loads OBJ file / draw rectangle / draw text / lighted texture shaders / bump map texturing / send 2 textures to shaders / create a shadow from obj file object.

The OBJ loader only supports a single texture. I used draw straight from vertex array instead of from a indexed array, so that uv coordinates from OBJ file from Blender maps coorectly.

Blender export options 

y up

write normals

export uv's

write materials



remember opengl es -y is up and +y is down.

bump map brighness adjust. - The bump map loads a second texture to the shader to adjust pixel brightness on the bumpmap texture green channel.

shadow map. - not as fancy as some of the online examples, but very simple, and looks good. 

It renders to a texture (textureIDs[0]) from the light's point of view (ortho) matrix, adjusted to camera distance. This uses a simple shader. Just send position and color, which is a solid dark color, no blending. Shadow rectangle 1x1.
Then, draw objects under the shadow (map, grass, etc).
Then draw the shadow rectangle with blending, from the normal perspective, camera matrix point of view. Shadow rectangle resized to 1x1.4 (2 - aspect ratio).
Then draw objects that are not shaded.

Enjoy
