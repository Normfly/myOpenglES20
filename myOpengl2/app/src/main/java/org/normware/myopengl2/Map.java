package org.normware.myopengl2;

import android.content.Context;
import android.opengl.GLES20;

public class Map {

    public final int GRASS = 0;

    MapTile[] tile = new MapTile[1];//0-grass,
    int width = 10;
    int height = 10;
    MapType[] tileInfo = new MapType[width * height];

    //returns next texture index, 0 for bumpMapImage will load without a bump map
    public int Load(Globals globals, Context context, int textureIndex){

        tile[0] = new MapTile();
        textureIndex = tile[0].LoadTile(globals, context, R.raw.grass, textureIndex, R.raw.grassbump);//grass = 0 tile

        // build map
        for (int i = 0; i < tileInfo.length; i++){
            tileInfo[i] = new MapType();
            tileInfo[i].type = GRASS;
        }

        return textureIndex;
    }

    public void Draw(Globals globals){

        int i = 0;
        for (int z = 0; z < height; z++){
            for (int x = 0; x < width; x++){
                tile[tileInfo[i++].type].Draw(globals, new Vector3f(-(width/2) + x, 0f, -(height/2) + z));
            }
        }

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);// must do this after bumptext

    }

    public class MapType{
        public int type;
    }
}
