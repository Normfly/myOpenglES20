package org.normware.myopengl2;

import android.content.Context;
import android.graphics.PointF;

public class MapTile {
    private int image = -1;
    private RectangleModel model;
    private Context context;

    public MapTile() {
    }

    //returns next texture index
    public int LoadTile(Globals globals, Context context,  int image, int textureIndex, int bumpMapImage){
        this.context = context;
        this.image = image;
        model = new RectangleModel(new PointF(1f, 1f), false, true, false);
        model.RandomizeBumpTex(200);
        //model.RandomizeTextureCoords(500);
        return model.LoadTexture(globals, context, image, textureIndex, bumpMapImage);
    }

    public void Draw(Globals globals, Vector3f location){
        model.Draw(globals, location, new Vector3f(0f, 0f, 0f), 1f);
    }

}
