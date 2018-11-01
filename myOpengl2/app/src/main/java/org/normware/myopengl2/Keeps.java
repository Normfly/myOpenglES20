package org.normware.myopengl2;

import android.content.Context;

public class Keeps {

    public Model model = new Model(false, true, 1.0f);
    public Vector3f[] location;
    public int numberOfKeeps;

    public Keeps(int numberOfKeeps){
        this.numberOfKeeps = numberOfKeeps;
    }

    public void LoadKeep(Globals globals, Context context, int textureIndex){
        model.LoadModel(globals, context, "keep", textureIndex, true);
        location = new Vector3f[numberOfKeeps];
        for (int i = 0; i < numberOfKeeps; i++){
            location[i] = Vector3f.ZERO;
        }
    }

    public void Draw(Globals globals){
        for (int i = 0; i < numberOfKeeps; i++){
            model.Draw(globals, new LocAngScale(location[i], Vector3f.ZERO, Vector3f.ONE));
        }

    }

    public void DrawShadow(Globals globals){
        for (int i = 0; i < numberOfKeeps; i++){
            model.DrawShadow(globals, new LocAngScale(location[i], Vector3f.ZERO, Vector3f.ONE));
        }

    }

}