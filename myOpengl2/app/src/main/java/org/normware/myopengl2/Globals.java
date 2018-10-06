package org.normware.myopengl2;

import android.app.Application;
import android.content.Context;

public class Globals extends Application {
    // Lighting
    public float[] lightAmbient = {1.0f, 1.0f, 1.0f, 0.2f};//ambient light color, last number is brightness
    public float[] lightPosition = new float[]{-5.0f, -5.0f, 5.0f, 1.0f};//sun position, last number is brightness, leave first three 0.0f to just use light direction, not position (the sun)
    //public float[] lightDirection = new float[]{0.0f, -1.0f, 0.0f};//light direction, keep 1.0f and under

    // Our matrices
    public float[] perspectiveMatrix = new float[16];
    public float[] orthoMatrix = new float[16];
    public float[] cameraViewMatrix = new float[16];
    public float[] viewMatrix = new float[16];
    public float[] viewProjMatrix = new float[16];

}
