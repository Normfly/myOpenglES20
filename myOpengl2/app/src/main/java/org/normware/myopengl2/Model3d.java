package org.normware.myopengl2;

import android.content.Context;

public class Model3d {
    public PosAngScale position;
    public String name;
    public Model model;

    public Model3d(String name, Boolean transparent, Boolean lighted, float alpha, PosAngScale location){
        this.name = name;
        this.position = location;
        model = new Model(transparent, lighted, alpha);
    }

    public void Draw(Globals globals){
        model.draw(globals, position);
    }

    public void LoadTexture(Context context, String imageName, int textureIndex){
        model.LoadModel(context, imageName, textureIndex);
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
