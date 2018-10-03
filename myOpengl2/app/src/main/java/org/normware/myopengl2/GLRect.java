package org.normware.myopengl2;


import android.graphics.PointF;

public class GLRect {
    float[] rect = new float[8];
    public PointF leftBottom, leftTop, rightTop, rightBottom;
    /*// Texture coords for the above face
      0,1              0.0f, 0.0f,  // A. left-bottom
      2,3              0.0f, 1.0f,  // B. left top
      4,5              1.0f, 1.0f,  // C. right-top
      6,7              1.0f, 0.0f   // D. right-bottom*/

    public float[] getRect(){
        return rect;
    }
    public float[] getFlippedYRect(){
        float[] result = {
                rect[0], rect[3],
                rect[2], rect[1],
                rect[4], rect[7],
                rect[6], rect[5]};
        return result;
    }
    public PointF getLeftBottom(){return new PointF(rect[0], rect[1]);}
    public PointF getLeftTop(){return new PointF(rect[2], rect[3]);}
    public PointF getRightTop(){return new PointF(rect[4], rect[5]);}
    public PointF getRightBottom(){return new PointF(rect[6], rect[7]);}
    public float getWidth(){return rect[4] - rect[0];}
    public float getHeight(){return rect[3] - rect[1];}
    public float getLeft(){return rect[0];}
    public float getRight(){return rect[4];}
    public float getTop(){return rect[3];}
    public float getBottom(){return rect[1];}
    public void setLeft(float left){
        rect[0] = left;
        rect[2] = left;
    }
    public void setRight(float right){
        rect[4] = right;
        rect[6] = right;
    }
    public void setTop(float top){
        rect[1] = top;
        rect[7] = top;
    }
    public void setBottom(float bottom){
        rect[3] = bottom;
        rect[5] = bottom;
    }
    public void setWidth(float width){
        setRight(rect[0] + width);
    }
    public void setHeight(float height){
        setBottom(rect[1] + height);
    }
}
