package org.normware.myopengl2;

import android.graphics.RectF;

public class Cube {
    //-left/right+
    //-top/bottom+
    //-far/near+
    public RectF topBottom, leftRight, frontBack;
    public final int LEFTTOPFRONT = 0;
    public final int LEFTBOTTOMFRONT = 1;
    public final int RIGHTBOTTOMFRONT = 2;
    public final int RIGHTTOPFRONT = 3;
    public final int LEFTTOPBACK = 4;
    public final int LEFTBOTTOMBACK = 5;
    public final int RIGHTBOTTOMBACK = 6;
    public final int RIGHTTOPBACK = 7;

    public static final int HEAD = 1;
    public static final int FEET = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;
    public static final int CENTER = 5;
    public static final int FRONT = 6;
    public static final int BACK = 7;

    public float longestSize = 0;
    public Vector3f center = new Vector3f(0f, 0f, 0f);

    public Cube(){
        leftRight = new RectF();
        frontBack = new RectF();
        topBottom = new RectF();
        BuildLongestDistance();
    }

    public Cube(RectF leftRight, RectF frontBack){
        //- is allways left and top / + is allways right and bottom
       this.leftRight = leftRight;
       this.frontBack = frontBack;
       this.topBottom = new RectF(frontBack.left, leftRight.left, frontBack.right, leftRight.right);//top is far - / near is bottom +
       BuildLongestDistance();
       center = GetCenter();
    }

    public Cube(Vector3f center, Vector3f size){
        this.leftRight = new RectF(center.z + size.z, center.y - size.y, center.z - size.z, center.y + size.y);//z,y
        this.topBottom = new RectF(center.x - size.x, center.z + size.z, center.x + size.x, center.z - size.z);//x,z
        this.frontBack = new RectF(center.x - size.x, center.y - size.y, center.x + size.x, center.y + size.y);//x,y
        BuildLongestDistance();
        center = GetCenter();
    }

    public void ReScale(Vector3f scales){
        leftRight = new RectF(leftRight.left * scales.z, leftRight.top * scales.y, leftRight.right * scales.z, leftRight.bottom * scales.y);
        topBottom = new RectF(topBottom.left * scales.x, topBottom.top * scales.z, topBottom.right * scales.x, topBottom.bottom * scales.z);
        frontBack = new RectF(frontBack.left * scales.x, frontBack.top  * scales.y, frontBack.right * scales.x, frontBack.bottom * scales.y);
        BuildLongestDistance();
        center = GetCenter();
    }
    
    private void BuildLongestDistance(){
        //find furthest size, for distance compare first over bounding box collision
        longestSize = Math.abs(frontBack.top);
        if (Math.abs(frontBack.left - frontBack.right) > longestSize){longestSize = Math.abs(frontBack.left - frontBack.right);}
        if (Math.abs(frontBack.bottom - frontBack.top) > longestSize){longestSize = Math.abs(frontBack.bottom - frontBack.top);}
        if (Math.abs(topBottom.top - topBottom.bottom) > longestSize){longestSize = Math.abs(topBottom.top - topBottom.bottom);}
    }

    private Vector3f GetCenter(){
        Vector3f result = new Vector3f(0f, 0f, 0f);
        Vector3f dist = new Vector3f((Math.abs(frontBack.left - frontBack.right)) / 2,
                                    (Math.abs(frontBack.bottom + frontBack.top)) / 2,
                                    (Math.abs(topBottom.bottom + topBottom.top)) / 2);
        result.x = frontBack.left + dist.x;
        result.y = frontBack.top + dist.y;
        result.z = topBottom.bottom + dist.z;
        return result;
    }

    public int GetHitLocation(Vector3f vector){
        //find closest distance, for distance compare first over bounding box collision

        int hitLocation = CENTER;//start off with distance and location from center
        float closestDistance = Math.abs(center.Length() - vector.Length());

        if (Math.abs(frontBack.left - vector.x) < closestDistance){
            hitLocation = LEFT;
            closestDistance = Math.abs(frontBack.left - vector.x);
        }
        if (Math.abs(frontBack.right - vector.x) < closestDistance){
            hitLocation = RIGHT;
            closestDistance = Math.abs(frontBack.right - vector.x);
        }
        if (Math.abs(frontBack.bottom - vector.y) < closestDistance){
            hitLocation = FEET;
            closestDistance = Math.abs(frontBack.bottom - vector.y);
        }
        if (Math.abs(frontBack.top - vector.y) < closestDistance){
            hitLocation = HEAD;
            closestDistance = Math.abs(frontBack.top - vector.y);
        }
        if (Math.abs(topBottom.top - vector.z) < closestDistance){
            hitLocation = BACK;
            closestDistance = Math.abs(topBottom.top - vector.z);
        }
        if (Math.abs(topBottom.bottom - vector.z) < closestDistance){
            hitLocation = FRONT;
            closestDistance = Math.abs(topBottom.bottom - vector.z);
        }
        return hitLocation;
    }

    public Vector3f GetVector3f(int vecLocation){
        switch (vecLocation){

            //front plane
            case LEFTTOPFRONT:
                return new Vector3f(frontBack.left, frontBack.top, topBottom.top);
            case LEFTBOTTOMFRONT:
                return new Vector3f(frontBack.left, frontBack.bottom, topBottom.top);
            case RIGHTBOTTOMFRONT:
                return new Vector3f(frontBack.right, frontBack.bottom, topBottom.top);
            case RIGHTTOPFRONT:
                return new Vector3f(frontBack.right, frontBack.top, topBottom.top);

            //rear plane
            case LEFTTOPBACK:
                return new Vector3f(frontBack.left, frontBack.top, topBottom.bottom);
            case LEFTBOTTOMBACK:
                return new Vector3f(frontBack.left, frontBack.bottom, topBottom.bottom);
            case RIGHTBOTTOMBACK:
                return new Vector3f(frontBack.right, frontBack.bottom, topBottom.bottom);
            case RIGHTTOPBACK:
                return new Vector3f(frontBack.right, frontBack.top, topBottom.bottom);

        }
        return new Vector3f(0f, 0f, 0f);//not valid location
    }
}
