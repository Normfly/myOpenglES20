package org.normware.myopengl2;

import android.app.Application;
import android.content.Context;
import android.graphics.Point;

public class Globals extends Application {
    public boolean debuging = true;

    //screen size in pixels and 3d coordinates
    public float glScreenWidth;
    public float glScreenHeight;
    public float glScreenSize = 10f;// biggest is this size
    public int screenWidth;//in pixels
    public int screenHeight;//in pixels
    public float aspectRatio;
    public float revAspectRatio;
    float scale = 1.0f;

    Vector3f test = new Vector3f(5f, -5f, 5f);//(1.5f, 7f, 0f);

    public int[] textureIDs = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10}; //texture image ID's
    int[] fb = new int[1];//frame buffer
    int[] depthRb = new int[1];//render buffer

    //camera
    //public Vector3f cameraAngles = new Vector3f(30f, 0f, 0f);
    public Vector3f cameraPosition = new Vector3f(0f, -5f, 10f);

    // Lighting
    public float[] lightAmbient = {1.0f, 1.0f, 1.0f, 0.2f};//ambient light color, last number is brightness
    public float[] lightPosition = new float[]{-10.0f, -5.0f, 0.0f, 1.0f};//sun position, last number is brightness, leave first three 0.0f to just use light direction, not position (the sun)
    //public float[] lightDirection = new float[]{0.0f, -1.0f, 0.0f};//light direction, keep 1.0f and under

    // Our matrices
    public float[] cameraViewMatrix = new float[16];//camera position and angles, (start here)
    public float[] perspectiveMatrix = new float[16];//perspective 3d viewing, the further away, the smaller (next here)
    public float[] orthoMatrix = new float[16];//z distance doesn't change anything, good for 2D games or text HUD or screen mask (or here)
    public float[] viewProjMatrix = new float[16];//combined camera and either perspective or otho
    public float[] HUDMatrix = new float[16];//used for text drawing, 0,0 - glsize, no negative

}
