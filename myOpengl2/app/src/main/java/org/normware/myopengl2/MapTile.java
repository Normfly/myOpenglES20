package org.normware.myopengl2;

import android.content.Context;
import android.graphics.PointF;

public class MapTile {
    private int image = -1;
    private RectangleModel model;
    private Context context;
    public Vector3f location = new Vector3f(0f, 0f, 0f);
    public Vector3f angles = new Vector3f(0f, 0f, 0f);
    public float scale = 1f;

    public MapTile() {
    }

    public void LoadTile(Context context,  int image, int textureIndex){
        this.context = context;
        this.image = image;
        model = new RectangleModel(new PointF(1f, 1f), false, true, true, false);
        model.LoadTexture(context, image,textureIndex);
        angles.x = 0f;//rotate a bit off level
        location.y = 0f;
    }

    public void Draw(Globals globals){
        model.Draw(globals.viewProjMatrix, location, angles, scale);
    }

}
