package org.normware.myopengl2;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class Animation {
    public String[] frameName;
    public Model[] frames;
    public int currentFrame = 0;// current frame to draw
    public ArrayList<AnimationInfo> animationInfo = new ArrayList<AnimationInfo>();
    public int updateCounter = 0;// update count
    private int updateDir = 1;// +1 animation frame increasing or -1 decreasing
    public LocAngScale position;// overall loc/ang/scale
    private LocAngScale[] frameAdjust;// adjust individual frame loc/ang/scale
    private int currentAnimation;
    private BoundingBox[] boundingBox;
    private boolean hasBoundingBox;

    public void Draw(Globals globals){
        frames[currentFrame].Draw(globals, AdjustPosition(currentFrame));

        if (globals.debuging && hasBoundingBox) {
            boundingBox[currentFrame].Draw(globals.viewProjMatrix, position);
        }
    }

    /*public void BoundingBoxRescale(int frame, Vector3f scales){
        boundingBox[frame] = new BoundingBox(boundingBox[frame].dimentions, scales.Add(position.scales));
    }*/

    public int LoadTexture(Globals globals, Context context, String[] imageName, int startTextureIndex, Boolean transparent, Boolean lighted, float alpha, boolean hasBoundingBox,
                           LocAngScale framePosition, boolean shadowed){
        this.hasBoundingBox = hasBoundingBox;
        this.position = framePosition;
        frameName = imageName;
        int frameNumbers = imageName.length;
        frames = new Model[frameNumbers];
        frameAdjust = new LocAngScale[frameNumbers];
        int textureIndex = startTextureIndex;

        for (int frame = 0; frame < frameNumbers; frame++){
            frames[frame] = new Model(transparent, lighted, alpha);// model
            frameAdjust[frame] = new LocAngScale(0f, 0f, 0f,
                                                    0f, 0f, 0f,
                                                    1f, 1f, 1f);// used to move/rotate/scale individual frames
        }

        if (hasBoundingBox){boundingBox = new BoundingBox[frameNumbers];}

        for (int frame = 0; frame < frameNumbers; frame++){
            frames[frame].LoadModel(globals, context, imageName[frame], textureIndex++, shadowed);//load OBJ 3d models
            //create bounding boxs
            if (hasBoundingBox){boundingBox[frame] = new BoundingBox(frames[frame].boundingBoxCube);}//, position.scales);}
        }

        //initialize animation to start frame to last and reverse
        animationInfo.add(new AnimationInfo("default", 0, frameNumbers, true, -1));// create default animation start frame to last, reversable

        return textureIndex;
    }

    public void SetPosition(LocAngScale position){this.position = position;}
    public void SetLocation(Vector3f location){this.position.location = location;}
    public void Move(Vector3f amount){position.location.Add(amount);}
    public void SetAngles(Vector3f angles){this.position.angles = angles;}
    public void Rotate(Vector3f amount){this.position.angles.Add(amount);}
    public void SetScales(Vector3f scales){this.position.scales = scales;}
    public void ReScale(Vector3f amount){this.position.scales.Multiply(amount);}

    private LocAngScale AdjustPosition(int frame){
        return new LocAngScale(position.location.Add(frameAdjust[frame].location),//location adjust
                                position.angles.Add(frameAdjust[frame].angles),//angles adjust
                                position.scales.Multiply(frameAdjust[frame].scales));//scales adjust
    }

    public void SetCurrentAnimation(String animationName){
        for (int i = 0; i < animationInfo.size(); i++){
            if (animationInfo.get(i).name == animationName){
                currentAnimation = i;
                break;
            }
        }
    }

    public String GetCurrentAnimationName(){return animationInfo.get(currentAnimation).name;}

    public void Update(){
        AnimationInfo ai = animationInfo.get(currentAnimation);
        if (ai.updateInterval == -1){return;}// do not automatically update animation frame
        updateCounter++;
        //advance animation frame
        if (updateCounter >= ai.updateInterval){
            updateCounter = 0;
            currentFrame += updateDir;
            // at last frame, reverse animation direction backwards
            if (currentFrame >= ai.endFrame && ai.reverseAnimation){
                updateDir *= -1;
                // at first frame, reverse animation direction forward
            }else if (currentFrame <= ai.startFrame && ai.reverseAnimation){
                updateDir *= -1;
            }
        }
    }

    public void UpdateNow(){
        updateCounter = animationInfo.get(currentAnimation).updateInterval;
        Update();
    }

    public void IncreaseFrameNow(){
        updateDir = 1;
        UpdateNow();
    }

    public void DecreaseFrameNow(){
        updateDir = -1;
        UpdateNow();
    }


    public void AddAnimation(String name, int startAnimationFrame, int endAnimationFrame, boolean reverseAnimation, int updateInterval){
        animationInfo.add(new AnimationInfo(name, startAnimationFrame, endAnimationFrame, reverseAnimation, updateInterval));
    }

    private class AnimationInfo extends Animation{
        public String name;
        public int startFrame = 0;// start animation at this frame
        public int endFrame = 0;// stop animation at this frame or reverse
        public boolean reverseAnimation = true;// reverse animation at first and last frames
        public int updateInterval = -1;// never update frame or update interval

        private AnimationInfo(String name, int startAnimationFrame, int endAnimationFrame, boolean reverseAnimation, int updateInterval){
            this.name = name;
            this.startFrame = startAnimationFrame;
            this.endFrame = endAnimationFrame;
            this.reverseAnimation = reverseAnimation;
            this.updateInterval = updateInterval;
        }

    }

    public boolean CheckHit(Vector3f vector){
        return Collisions.CheckHit(boundingBox[currentFrame].dimentions, position, vector);
    }

    public int CheckHitLocation(Vector3f vector){
        return Collisions.CheckHitLocation(boundingBox[currentFrame].dimentions, position, vector);
    }

}
