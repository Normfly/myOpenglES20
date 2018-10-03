package org.normware.myopengl2;

public class PosRotation {
    Vector3f location, angles;

    public PosRotation(float locx, float locy, float locz,
                       float angx, float angy, float angz){
        location = new Vector3f(locx, locy, locz);
        angles = new Vector3f(angx, angy, angz);
    }
}
