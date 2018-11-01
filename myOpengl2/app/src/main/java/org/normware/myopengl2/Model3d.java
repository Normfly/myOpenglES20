package org.normware.myopengl2;

import android.content.Context;

public class Model3d {
    public LocAngScale position;
    public String name;
    public Model model;

    public Model3d(String name, Boolean transparent, Boolean lighted, float alpha, LocAngScale location){
        this.name = name;
        this.position = location;
        model = new Model(transparent, lighted, alpha);
    }

    public void Draw(Globals globals){
        model.Draw(globals, position);
    }
    public void DrawShadow(Globals globals){}

    public void LoadTexture(Globals globals, Context context, String imageName, int textureIndex, boolean shadowed){
        model.LoadModel( globals, context, imageName, textureIndex, shadowed);
    }

    public Vector3f getPosition(){
        return position.location;
    }

    public Vector3f getAngles(){
        return position.angles;
    }

    public Vector3f getScale(){
        return position.scales;
    }
}
