package org.normware.myopengl2;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.opengl.Matrix;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

public class GLText {
    private RectangleModel rectangleModel;
    private int textColor;
    private int backgroundColor;
    private int timeout = 0;
    public boolean transparent = true;
    public int textureIndex;
    private float size;


    private String fontText = " " + "\n" + "!" + "\n" + " " + "\n" + "#" + "\n" + "$" + "\n" + "%" + "\n" + "&" + "\n" +
            "'" + "\n" + "(" + "\n" + ")" + "\n" + "*" + "\n" + "+" + "\n" + "," + "\n" + "-" + "\n" + "." + "\n" + "/" + "\n" +
            "0" + "\n" + "1" + "\n" + "2" + "\n" + "3" + "\n" + "4" + "\n" + "5" + "\n" + "6" + "\n" +
            "7" + "\n" + "8" + "\n" + "9" + "\n" + ":" + "\n" + ";" + "\n" + "<" + "\n" + "=" + ">" + "\n" + "?" + "\n" + "@" + "\n" +
            "A" + "\n" + "B" + "\n" + "C" + "\n" + "D" + "\n" + "E" + "\n" + "F" + "\n" + "G" + "\n" + "H" + "\n" + "I" + "\n" +
            "J" + "\n" + "K" + "\n" + "L" + "\n" + "M" + "\n" + "N" + "\n" + "O" + "\n" + "P" + "\n" + "Q" + "\n" + "R" + "\n" + "S" + "\n" +
            "T" + "\n" + "U" + "\n" + "V" + "\n" + "W" + "\n" + "X" + "\n" + "Y" + "\n" + "Z" + "\n" +
            "[" + "\n" + " " + "\n" + "]" + "\n" + "^" + "\n" + "_" + "\n" + "'" + "\n" + "a" + "\n" + "b" + "\n" + "c" + "\n" +
            "d" + "\n" + "e" + "\n" + "f" + "\n" + "g" + "\n" + "h" + "\n" + "i" + "\n" + "j" + "\n" + "k" + "\n" + "l" + "\n" + "m" + "\n" +
            "n" + "\n" + "o" + "\n" + "p" + "\n" + "q" + "\n" + "r" + "\n" + "s" + "\n" + "t" + "\n" + "u" + "\n" + "v" + "\n" + "w" + "\n" +
            "x" + "\n" + "y" + "\n" + "z" + "\n" + "{" + "\n" + "|" + "\n" + "}" + "\n" + "~";

    public void LoadFont(Globals globals, Context context, int textColor, int backgroundColor, boolean transparent, float size, int textureIndex){
        this.size = size;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.transparent = transparent;
        this.textureIndex = textureIndex;

        Bitmap bitmap = TextToBitmap(fontText, 20, textColor, backgroundColor);

        //square = new Square(globalVars, new Vector2f((float)bounds.width() / 100, (float)bounds.height() / 100), transparent);// new square, sized based on text bounds
        //rectangleModel = new RectangleModel(new PointF((float)bitmap.getWidth() / 100, (float)bitmap.getHeight() / 100),
        //        transparent, false, false);
        rectangleModel = new RectangleModel(new PointF(size, size), transparent, false, true);

        float[] texCoords = {
                0.005f, 0.0f,  // A. left-bottom
                0.005f, 1.0f,  // B. left top
                1.0f, 1.0f,  // C. right-top
                1.0f, 0.0f   // D. right-bottom
        };

        rectangleModel.LoadTexture(globals, context, bitmap, textureIndex, texCoords);
        bitmap.recycle();
    }

    public static Bitmap TextToBitmap(String text, int textSize, int textColor, int backgroundColor) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG
                | Paint.LINEAR_TEXT_FLAG);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTextSize(textSize);
        //paint.setStyle(Paint.Style.FILL);
        paint.setColor(backgroundColor);

        // Get text dimensions
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG
                | Paint.LINEAR_TEXT_FLAG);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setColor(textColor);
        textPaint.setTextSize(textSize);
        StaticLayout textLayout = new StaticLayout(text, textPaint,
                textSize, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, true);

        // Create bitmap and canvas to draw to
        Bitmap b = Bitmap.createBitmap(textLayout.getWidth(), textLayout.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        // Draw background
        c.drawPaint(paint);

        // Draw text
        c.save();
        c.translate(0, 0);
        textLayout.draw(c);
        c.restore();

        return b;
    }

    /*private float[] GetMatrix(){
        // 10x10 screen size for openGL
        float[] tempMatrix = new float[16];
        float[] cameraViewMatrix = new float[16];
        float[] orthoMatrix = new float[16];

        //set camera looking along z axis, good for 2D games
        Matrix.setLookAtM(cameraViewMatrix, 0, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Setup our screen width and height for ortho projection
        //Matrix.orthoM(orthoMatrix, 0, -mScreenWidth/2, mScreenWidth/2, -mScreenHeight/2, mScreenHeight/2, 0, 10);
        Matrix.orthoM(orthoMatrix, 0, -10f, 10f, -10f, 10f, 1f, 100f);

        // Setup perspective projection matrix
        Matrix.multiplyMM(tempMatrix, 0, orthoMatrix, 0, cameraViewMatrix, 0);//add camera matrix to perspective
        return tempMatrix;
    }*/

    public void DrawHUD(Globals globals, String text, PointF location){
        //float[] HUDmatrix = new float[16];
        //Matrix.orthoM(HUDmatrix, 0, 0f, globals.glScreenWidth, 0f, globals.glScreenHeight, 0.1f, -100f);//GetMatrix();
        Draw(globals, text, new Vector3f(location.x - size, globals.glScreenHeight - location.y - size, 0), true);
    }

    public void Draw(Globals globals, String text, PointF position, boolean HUD){
        Draw(globals, text, new Vector3f(position.x - size, position.y - size, 0), HUD);
    }

    public void Draw(Globals globals, String text, Vector3f position, boolean HUD){

        char c;
        int keyCode;
        GLRect rect;
        float startY;
        float width;
        float height;
        float top;
        for (int i = 0; i < text.length(); i++){
            c = text.charAt(i);
            keyCode = (int)c;//65 = A
            rect = new GLRect();
            startY = 0.0005f;
            width = 1f;
            height = 1.98f / fontText.length();
            top =(keyCode - 32) * height + startY;
            rect.setLeft(0.02f);
            rect.setTop(top);
            rect.setHeight(height);
            rect.setRight(width);
            rectangleModel.UpdateTextureCoords(rect.getRect());
            position.x += size;

            if (HUD){
                rectangleModel.DrawHUD(globals, new Vector3f(position.x, position.y, position.z), new Vector3f(0f, 0f, 0f), 1f);
            }else{
                rectangleModel.Draw(globals, new Vector3f(position.x, position.y, position.z), new Vector3f(0f, 0f, 0f), 1f);
            }

        }
    }
}
