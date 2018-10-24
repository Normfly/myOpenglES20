package org.normware.myopengl2;

import android.content.Context;

import java.util.ArrayList;

public class Wall {

    public static final int WALLSTRAIGHT = 0;
    public static final int WALLSINGLE = 1;
    public static final int WALLX = 2;
    public static final int WALLV = 3;

    private Model[] model = new Model[4];//0=wallstraight
    public ArrayList<WallInfo> wallInfo = new ArrayList<>();

    public int LoadWall(Globals globals, Context context, int textureIndex){
        for (int i = 0; i < model.length; i++){
            model[i] = new Model(false, true, 1.0f);
        }
        model[WALLSTRAIGHT].LoadModel(globals, context, "wallstraight", textureIndex++, false);
        model[WALLSINGLE].LoadModel(globals, context, "wallsingle", textureIndex++, false);
        model[WALLX].LoadModel(globals, context, "wallx", textureIndex++, false);
        model[WALLV].LoadModel(globals, context, "wallv", textureIndex++, false);

        return textureIndex;
    }

    public void AddWall(int type, Vector3f location, Vector3f angles, int player){
        WallInfo w = new WallInfo(type, location, angles, player);
        wallInfo.add(w);
    }

    public int GetPlayer(int wallIndex){
        return wallInfo.get(wallIndex).player;
    }

    public void Draw(Globals globals){
        for (int i = 0; i < wallInfo.size(); i++){
            model[wallInfo.get(i).type].Draw(globals, GetLocAngScale(i));
        }
    }

    private LocAngScale GetLocAngScale(int index){
        return new LocAngScale(wallInfo.get(index).location, wallInfo.get(index).angles, Vector3f.ONE);
    }

    private class WallInfo{
        private Vector3f location;
        private Vector3f angles;
        private int type;
        private int player;

        private WallInfo(int type, Vector3f location, Vector3f angles, int player){
            this.type = type;
            this.location = location;
            this.angles = angles;
            this.player = player;
        }
    }
}
