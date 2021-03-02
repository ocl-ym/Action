package com.example.action;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Ground {

    private int COLOR = Color.rgb(153, 76,0);
    private Paint paint = new Paint();

    final Rect rect;
    public Ground(int left, int top, int right, int bottom){
        rect = new Rect(left, top, right, bottom);

        paint.setColor(COLOR);
    }

    public void draw(Canvas canvas){
        canvas.drawRect(rect, paint);
    }

    public void move(int moveToLeft){
        rect.offset(-moveToLeft, 0);
    }

    public boolean isShown(int width, int height) {
        return rect.intersects(0, 0, width, height);
    }
    public boolean isAvailable(){
        return rect.right > 0;
    }

    /**
     * 固定物かどうか判定
     */
    public boolean isSolid(){
        return true;
    }
}
