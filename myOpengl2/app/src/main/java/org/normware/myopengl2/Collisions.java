package org.normware.myopengl2;

import android.graphics.PointF;
import android.graphics.RectF;

public class Collisions {

    // point collides with rectangle
    public static boolean RectCollision(RectF rect, PointF point){
        if (point.x >= rect.left && point.x <= rect.right &&
                point.y >= rect.top && point.y <= rect.bottom){
            return true;
        }else{
            return false;
        }
    }

    // check for point/box collision after vector has had location and rotation taken out (true / false)
    public static boolean CheckHit(Cube boundingBox, LocAngScale objectPosition, Vector3f vector){
        Vector3f v = vector.ReverseMoveRotateScale(vector, objectPosition);
        return CubeCollision(boundingBox, v);
    }

    // check for point/box collision after vector has had location and rotation taken out (0 no hit / # for location)
    public static int CheckHitLocation(Cube boundingBox, LocAngScale objectPosition, Vector3f vector){
        Vector3f v = vector.ReverseMoveRotateScale(vector, objectPosition);
        if (CubeCollision(boundingBox, v)){
            return boundingBox.GetHitLocation(v);
        }else{
            return 0;
        }
    }

    // check for ray/box collision after vector has had location and rotation taken out (true / false)
    public static boolean CheckRayHit(Cube boundingBox, LocAngScale objectPosition, Vector3f origin, Vector3f direction){
        Vector3f vOrigin = origin.ReverseMoveRotateScale(origin, objectPosition);
        Vector3f vDirection = direction.ReverseMoveRotateScale(direction, objectPosition);
        return CubeRayCollision(boundingBox, vOrigin, vDirection);
    }

    // check for ray/box collision after vector has had location and rotation taken out (0 no hit / # for location)
    public static int CheckRayHitLocation(Cube boundingBox, LocAngScale objectPosition, Vector3f origin, Vector3f direction){
        Vector3f vOrigin = origin.ReverseMoveRotateScale(origin, objectPosition);
        Vector3f vDirection = direction.ReverseMoveRotateScale(direction, objectPosition);
        if (CubeRayCollision(boundingBox, vOrigin, vDirection)){
            return CubeRayHitLocation(boundingBox, vOrigin, vDirection);
        }else{
            return 0;//no hit
        }
    }

    // check raw untranslated/unrotated collision
    public static boolean CubeCollision(Cube cube, Vector3f vector){
        boolean leftRightHit, topBottomHit, frontBackHit;
        PointF p;
        
        //do distance check first
        if (vector.Length() > cube.longestSize) return false;

        //left/right, x is set
        p = new PointF(vector.z, vector.y);//z = -left/right+, y = -top/bottom+
        if (RectCollision(cube.leftRight, p)){
            leftRightHit = true;
        }else{
            leftRightHit = false;
        }
        //top/bottom, y is set
        p = new PointF(vector.x, vector.z);//x = -left/right+, z = -top/bottom+
        if (RectCollision(cube.topBottom, p)){
            topBottomHit = true;
        }else{
            topBottomHit = false;
        }
        //front/back, z is set
        p = new PointF(vector.x, vector.y);//x/y
        if (RectCollision(cube.frontBack, p)){
            frontBackHit = true;
        }else{
            frontBackHit = false;
        }

        if (leftRightHit && topBottomHit && frontBackHit){
            return true;
        }else{
            return false;
        }
    }

    // raw untranslated / unrotated ray collision
    public static boolean CubeRayCollision(Cube cube, Vector3f vector, Vector3f direction){
        // - front/head / back/feet +

        PointF p;
        Vector3f v;

        Vector3f relativeDirection = direction.Subtract(vector);

        //left, x is set
        v = RayIntersectX(vector, direction, cube.frontBack.left);//where ray intersects left rectangle
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.x > 0){//make sure ray is proper direction
            p = new PointF(v.z, v.y);//z = left/right, y = top/bottom
            if (RectCollision(cube.leftRight, p)){
                return true;
            }
        }
        //right, x is set
        v = RayIntersectX(vector, direction, cube.frontBack.right);//where ray intersects right rectangle
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.x < 0){//make sure ray is proper direction
            p = new PointF(v.z, v.y);//z = left/right, y = top/bottom
            if (RectCollision(cube.leftRight, p)){
                return true;
            }
        }
        //top, y is set
        v = RayIntersectY(vector, direction, cube.frontBack.top);//where ray intersects top rectangle (top is - / bottom is +)
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.y > 0){//make sure ray is proper direction
            p = new PointF(v.x, v.z);//x = left/right, z = top/bottom
            if (RectCollision(cube.topBottom, p)){
                return true;
            }
        }
        //bottom, y is set
        v = RayIntersectY(vector, direction, cube.frontBack.bottom);//where ray intersects bottom rectangle
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.y < 0){//make sure ray is proper direction
            p = new PointF(v.x, v.z);//x = left/right, z = top/bottom
            if (RectCollision(cube.topBottom, p)){
                return true;
            }
        }
        //front, z is set
        v = RayIntersectZ(vector, direction, cube.topBottom.bottom);//where ray intersects front rectangle (close is + / far is -)
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.z < 0){//make sure ray is proper direction
            p = new PointF(v.x, v.y);//x = left/right, y = top/bottom
            if (RectCollision(cube.frontBack, p)){
                return true;
            }
        }
        //back, z is set
        v = RayIntersectZ(vector, direction, cube.topBottom.top);//where ray intersects back rectangle
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.z > 0){//make sure ray is proper direction
            p = new PointF(v.x, v.y);//x = left/right, y = top/bottom
            if (RectCollision(cube.frontBack, p)){
                return true;
            }
        }

        return false;
    }

    // raw untranslated / unrotated ray collision
    public static int CubeRayHitLocation(Cube cube, Vector3f vector, Vector3f direction){
        // - front/head / back/feet +

        PointF p;
        Vector3f v;

        Vector3f relativeDirection = direction.Subtract(vector);
        int hitLocation = 0;
        float hitDistance = 1000f;
        float distance;
        boolean result = false;

        //left, x is set
        v = RayIntersectX(vector, direction, cube.frontBack.left);//where ray intersects left rectangle
        distance = v.Length();
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.x > 0){//make sure ray is proper direction
            p = new PointF(v.z, v.y);//z = left/right, y = top/bottom
            if (RectCollision(cube.leftRight, p)){
                if (distance < hitDistance){
                    hitLocation = cube.LEFT;
                    hitDistance = distance;
                }
                result = true;
            }
        }
        //right, x is set
        v = RayIntersectX(vector, direction, cube.frontBack.right);//where ray intersects right rectangle
        distance = v.Length();
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.x < 0){//make sure ray is proper direction
            p = new PointF(v.z, v.y);//z = left/right, y = top/bottom
            if (RectCollision(cube.leftRight, p)){
                if (distance < hitDistance){
                    hitLocation = cube.RIGHT;
                    hitDistance = distance;
                }
                result = true;
            }
        }
        //top, y is set
        v = RayIntersectY(vector, direction, cube.frontBack.top);//where ray intersects top rectangle (top is - / bottom is +)
        distance = v.Length();
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.y > 0){//make sure ray is proper direction
            p = new PointF(v.x, v.z);//x = left/right, z = top/bottom
            if (RectCollision(cube.topBottom, p)){
                if (distance < hitDistance){
                    hitLocation = cube.HEAD;
                    hitDistance = distance;
                }
                result = true;
            }
        }
        //bottom, y is set
        v = RayIntersectY(vector, direction, cube.frontBack.bottom);//where ray intersects bottom rectangle
        distance = v.Length();
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.y < 0){//make sure ray is proper direction
            p = new PointF(v.x, v.z);//x = left/right, z = top/bottom
            if (RectCollision(cube.topBottom, p)){
                if (distance < hitDistance){
                    hitLocation = cube.FEET;
                    hitDistance = distance;
                }
                result = true;
            }
        }
        //front, z is set
        v = RayIntersectZ(vector, direction, cube.topBottom.bottom);//where ray intersects front rectangle (close is + / far is -)
        distance = v.Length();
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.z < 0){//make sure ray is proper direction
            p = new PointF(v.x, v.y);//x = left/right, y = top/bottom
            if (RectCollision(cube.frontBack, p)){
                if (distance < hitDistance){
                    hitLocation = cube.FRONT;
                    hitDistance = distance;
                }
                result = true;
            }
        }
        //back, z is set
        v = RayIntersectZ(vector, direction, cube.topBottom.top);//where ray intersects back rectangle
        distance = v.Length();
        if (v != new Vector3f(0f, 0f, 0f) && relativeDirection.z > 0){//make sure ray is proper direction
            p = new PointF(v.x, v.y);//x = left/right, y = top/bottom
            if (RectCollision(cube.frontBack, p)){
                if (distance < hitDistance){
                    hitLocation = cube.BACK;
                    hitDistance = distance;
                }
                result = true;
            }
        }

        if (result){
            return hitLocation;
        }else{
            return 0;//no hit
        }
    }

    public static Vector3f RayIntersectX(Vector3f vector, Vector3f direction, float x){
        Vector3f amount = vector.Direction(direction);

        //Does not intersect X, so return all zeros
        if (Math.signum(x) != Math.signum(amount.x)){
            return new Vector3f(0f, 0f, 0f);
        }

        float ratio = x / amount.x;
        return new Vector3f(x,
                        (amount.y * ratio) + vector.y,
                        (amount.z * ratio) + vector.z);
    }

    public static Vector3f RayIntersectY(Vector3f vector, Vector3f direction, float y){
        Vector3f amount = vector.Direction(direction);

        //Does not intersect Y, so return all zeros
        if (Math.signum(y) != Math.signum(amount.y)){
            return new Vector3f(0f, 0f, 0f);
        }

        float ratio = y / amount.y;
        return new Vector3f((amount.x * ratio) + vector.x,
                            y,
                        (amount.z * ratio) + vector.z);
    }

    public static Vector3f RayIntersectZ(Vector3f vector, Vector3f direction, float z){
        Vector3f amount = vector.Direction(direction);

        //Does not intersect Z, so return all zeros
        if (Math.signum(z) != Math.signum(amount.z)){
            return new Vector3f(0f, 0f, 0f);
        }

        float ratio = z / amount.z;
        return new Vector3f((amount.x * ratio) + vector.x,
                (amount.y * ratio) + vector.y,
                z);
    }

    public static float GetAngle(PointF point){
        float result =  (float)Math.toDegrees(Math.atan2(point.x, point.y));
        return result;
    }

    public static float GetAngle(float x, float y){return GetAngle(new PointF(x, y));}

}
