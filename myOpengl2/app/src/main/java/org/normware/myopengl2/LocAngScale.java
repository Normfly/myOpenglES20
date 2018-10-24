package org.normware.myopengl2;

public class LocAngScale {
    Vector3f location, angles, scales;

    public LocAngScale(float locx, float locy, float locz,
                       float angx, float angy, float angz,
                       float sizex, float sizey, float sizez){

        location = new Vector3f(locx, locy, locz);
        angles = new Vector3f(angx, angy, angz);
        scales = new Vector3f(sizex, sizey, sizez);
    }

    public LocAngScale(){
        location = new Vector3f(0f, 0f, 0f);
        angles = new Vector3f(0f, 0f, 0f);
        scales = new Vector3f(1f, 1f, 1f);
    }

    public LocAngScale(Vector3f location, Vector3f angles, Vector3f scales){
        this.location = location;
        this.angles = angles;
        this.scales = scales;
    }

    public final static LocAngScale ZERO_ONE(){
        return new LocAngScale(Vector3f.ZERO, Vector3f.ZERO, Vector3f.ONE);
    }

}