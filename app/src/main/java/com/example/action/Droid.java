package com.example.action;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

public class Droid {

    private static final int HIT_MARGIN_LEFT = 30;
    private static final int HIT_MARGIN_RIGHT = 10;

    private static final float GRAVITY = 0.8f;
    private static final float WEIGHT = GRAVITY * 60;

    private final Paint paint = new Paint();

    private Bitmap bitmap;

    final Rect rect;
    final Rect hitRect;

    public interface Callback{
        int getDistanceFromGround(Droid droid);
    }

    private final Callback callback;

    public Droid(Bitmap bitmap, int left, int top, Callback callback){
        int right = left + bitmap.getWidth();
        int bottom = top + bitmap.getHeight();
        this.rect = new Rect(left, top, right, bottom);
        this.hitRect = new Rect(left, top, right, bottom);
        this.hitRect.left += HIT_MARGIN_LEFT;
        this.hitRect.right -= HIT_MARGIN_RIGHT;
        this.bitmap = bitmap;
        this.callback = callback;
    }

    public void draw(Canvas canvas){
        canvas.drawBitmap(bitmap, rect.left, rect.top, paint);
    }

    private float velocity = 0;

    public void jump(float power){
        velocity = (power * WEIGHT);
    }

    public void stop(){
        velocity = 0;
    }

    public void move(){
        int distanceFromGround = callback.getDistanceFromGround(this);

        if (velocity < 0 && velocity < -distanceFromGround){
            velocity = -distanceFromGround;
        }

        rect.offset(0, Math.round(-1 * velocity));
        hitRect.offset(0, Math.round(-1 * velocity));

        if (distanceFromGround == 0){
            return;
        }else if (distanceFromGround < 0){
            rect.offset(0,distanceFromGround);
            hitRect.offset(0,distanceFromGround);
            return;
        }

//        rect.offset(0,5);
        velocity -= GRAVITY;
    }
}
