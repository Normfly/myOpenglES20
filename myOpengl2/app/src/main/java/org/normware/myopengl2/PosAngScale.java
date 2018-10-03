package org.normware.myopengl2;

public class PosAngScale {
    Vector3f location, angles, scales;
    public PosAngScale(float locx, float locy, float locz,
                       float angx, float angy, float angz,
                       float sizex, float sizey, float sizez){
        location = new Vector3f(locx, locy, locz);
        angles = new Vector3f(angx, angy, angz);
        scales = new Vector3f(sizex, sizey, sizez);
    }
}
