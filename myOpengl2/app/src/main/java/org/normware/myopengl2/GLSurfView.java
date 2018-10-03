package org.normware.myopengl2;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class GLSurfView extends GLSurfaceView {

    private final GLRenderer mRenderer;

    public GLSurfView(Context context) {
        super(context);

        // Create an OpenGL ES 2.0 context.
        setEGLContextClientVersion(2);

        // Set the Renderer for drawing on the GLSurfaceView
        mRenderer = new GLRenderer(context);
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data
        setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

        // Request focus, otherwise key/button won't react
        this.requestFocus();
        this.setFocusableInTouchMode(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        mRenderer.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mRenderer.onResume();
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mRenderer.processTouchEvent(e);
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        mRenderer.ProcessKeyUp(keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        mRenderer.ProcessKeyDown(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }
}
