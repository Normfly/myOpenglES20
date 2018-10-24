package org.normware.myopengl2;

import android.graphics.PointF;

import static org.normware.myopengl2.Collisions.GetAngle;

public class Vector3f {
    public final static Vector3f ZERO = new Vector3f(0f, 0f, 0f);
    public final static Vector3f ONE = new Vector3f(1f, 1f, 1f);

    public float x;
    public float y;
    public float z;

    public Vector3f(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3f Add(float x, float y, float z){
        this.x += x;
        this.y += y;
        this.z += z;
        return new Vector3f(this.x, this.y, this.z);
    }

    public Vector3f Add(Vector3f vec){return Add(vec.x, vec.y, vec.z);}

    public Vector3f Subtract(float x, float y, float z){
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return new Vector3f(this.x, this.y, this.z);
    }

    public Vector3f Subtract(Vector3f vec){return Subtract(vec.x, vec.y, vec.z);}

    public Vector3f Multiply(float x, float y, float z){
        this.x *= x;
        this.y *= y;
        this.z *= z;
        return new Vector3f(this.x, this.y, this.z);
    }

    public Vector3f Multiply(Vector3f vec){return Multiply(vec.x, vec.y, vec.z);}

    public Vector3f Divide(float x, float y, float z){
        this.x /= x;
        this.y /= y;
        this.z /= z;
        return new Vector3f(this.x, this.y, this.z);
    }

    //get distance
    private static float GetDistance(PointF point){
        return (float)Math.sqrt((point.x * point.x) + (point.y * point.y));
    }

    public Vector3f Divide(Vector3f vec){return Divide(vec.x, vec.y, vec.z);}

    //rotate with 0,0,0 center
    public void Rotate(Vector3f angles){
        RotateXaxis(angles.x);
        RotateYaxis(angles.y);
        RotateZaxis(angles.z);
    }

    public void RotateXaxis(float angle){RotateAroundX(new Vector3f(0f, 0f, 0f), angle);}
    public void RotateYaxis(float angle){RotateAroundY(new Vector3f(0f, 0f, 0f), angle);}
    public void RotateZaxis(float angle){RotateAroundZ(new Vector3f(0f, 0f, 0f), angle);}

    public void RotateAroundVector(Vector3f center, Vector3f angles){
        RotateAroundX(center, angles.x);
        RotateAroundY(center, angles.y);
        RotateAroundZ(center, angles.z);
    }

    public void RotateAroundX(Vector3f center, float angle){//z takes x place
        if (angle == 0  || (z == 0 && y == 0)){return;}//otherwise cos(0) returns 1 * distance
        double rad = Math.toRadians(angle + GetAngle(z, -y));//get current angle and get rotation amount
        float distance = GetDistance(new PointF(z - center.z, y - center.y));

        PointF result = new PointF(((float)Math.sin(rad) * distance),
                                    ((float)-Math.cos(rad) * distance));

        z = result.x + center.z;
        y = result.y + center.y;
    }

    public void RotateAroundY(Vector3f center, float angle){//z takes y place
        if (angle == 0 || (x == 0 && z == 0)){return;}//otherwise cos(0) returns 1 * distance
        double rad = Math.toRadians(angle + GetAngle(x, -z));//get current angle and get rotation amount
        float distance = GetDistance(new PointF(x - center.x, z - center.z));

        PointF result = new PointF(((float)Math.sin(rad) * distance),
                                    ((float)-Math.cos(rad) * distance));

        x = result.x + center.x;
        z = result.y + center.z;
    }

    public void RotateAroundZ(Vector3f center, float angle){
        if (angle == 0  || (x == 0 && y == 0)){return;}//otherwise cos(0) returns 1 * distance
        double rad = Math.toRadians(GetAngle(x, y) + angle);//get current angle and get rotation amount
        float distance = GetDistance(new PointF(x - center.x, y - center.y));

        PointF result = new PointF(((float)Math.sin(rad) * distance),
                                    ((float)-Math.cos(rad) * distance));

        x = result.x + center.x;
        y = result.y + center.y;
    }

    public Vector3f Direction(Vector3f destination){
        return new Vector3f(destination.x - x,
                            destination.y - y,
                            destination.z - z);
    }

    public void MoveRotateScale(LocAngScale amounts){
        Vector3f v = new Vector3f(x, y, z);
        v.Add(amounts.location);
        v.RotateAroundVector(ZERO, amounts.angles);
        v.Multiply(amounts.scales);
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public void ReverseMoveRotate(Vector3f move, Vector3f rotate){
        Vector3f v = new Vector3f(x, y, z);
        v.Add(new Vector3f(-move.x, -move.y, -move.z));
        v.RotateAroundVector(ZERO, new Vector3f(-rotate.x, -rotate.y, -rotate.z));
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public void ReverseMoveRotateScales(LocAngScale position){
        Vector3f v = new Vector3f(x, y, z);
        v.Add(new Vector3f(-position.location.x, -position.location.y, -position.location.z));
        v.RotateAroundVector(ZERO, new Vector3f(-position.angles.x, -position.angles.y, -position.angles.z));
        v.Divide(position.scales);
        x = v.x;
        y = v.y;
        z = v.z;
    }

    public static Vector3f MoveRotateScale(Vector3f originalLocation, LocAngScale amounts){
        Vector3f v = new Vector3f(originalLocation.x, originalLocation.y, originalLocation.z);
        v.Add(amounts.location);
        v.RotateAroundVector(ZERO, amounts.angles);
        v.Multiply(amounts.scales);
        return v;
    }

    public static Vector3f ReverseMoveRotate(Vector3f originalLocation, Vector3f move, Vector3f rotate){
        Vector3f v = new Vector3f(originalLocation.x, originalLocation.y, originalLocation.z);
        v.Add(new Vector3f(-move.x, -move.y, -move.z));
        v.RotateAroundVector(ZERO, new Vector3f(-rotate.x, -rotate.y, -rotate.z));
        return v;
    }

    public static Vector3f ReverseMoveRotateScale(Vector3f originalLocation, LocAngScale position){
        Vector3f v = new Vector3f(originalLocation.x, originalLocation.y, originalLocation.z);
        v.Add(new Vector3f(-position.location.x, -position.location.y, -position.location.z));
        v.RotateAroundVector(ZERO, new Vector3f(-position.angles.x, -position.angles.y, -position.angles.z));
        v.Divide(position.scales);
        return v;
    }

    public float Length(){
        double yUp =  Math.sqrt((x * x) + (y * y));
        double zUp  = Math.sqrt((z * z) + (y * y));
        double xUp = Math.sqrt((x * x) + (z * z));
        return (float)Math.sqrt((xUp * xUp) + (yUp * xUp) + (zUp * zUp));
    }

    public static float Length(Vector3f vector){
        double yUp =  Math.sqrt((vector.x * vector.x) + (vector.y * vector.y));
        double zUp  = Math.sqrt((vector.z * vector.z) + (vector.y * vector.y));
        double xUp = Math.sqrt((vector.x * vector.x) + (vector.z * vector.z));
        return (float)Math.sqrt((xUp * xUp) + (yUp * xUp) + (zUp * zUp));
    }

    public static Vector3f FloatToVector3f(float[] xyz){
        return new Vector3f(xyz[0],xyz[1],xyz[2]);
    }

    public Vector3f Negative(){
        return new Vector3f(x * -1f, y * -1f, z * -1f);
    }

    public static Vector3f FlattenToY(Vector3f vector, Vector3f lightPos, float y){
        float distance = lightPos.y + y;//distance from light position to y plane (ground)
        Vector3f amount = lightPos.Direction(vector);
        //Vector3f amount = vector.Direction(lightPos);

        float ratio = vector.y / distance;
        return new Vector3f((amount.x * ratio) + vector.x,
                y,
                (amount.z * ratio) + vector.z);
    }

    public Vector3f FlipY(){
        return new Vector3f(x, -y, z);
    }

    public final Vector3f Copy(){return new Vector3f(x, y, z);}
}