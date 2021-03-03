package com.example.action;

import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogRecord;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
//    private static final Paint PAINT = new Paint();

    /**
     * パワーゲージを表示する
     * @param POER_GAUGE_HEIGHT パワーゲージの高さ
     * @param PAINT_POWER_GAUGE 色の設定
     */
    private static final float POWER_GAUGE_HEIGHT = 30;
    private static final Paint PAINT_POWER_GAUGE = new Paint();

    static {
        PAINT_POWER_GAUGE.setColor(Color.RED);
    }

    private static final long DRAW_INTERVAL = 1000 / 100;

    private class DrawThread extends Thread{
        private final AtomicBoolean isFinished = new AtomicBoolean(false);

        public void finish(){
            isFinished.set(true);
        }

        @Override
        public void run(){
            SurfaceHolder holder = getHolder();

            while (!isFinished.get()){
                if (holder.isCreating()){
                    continue;
                }
                Canvas canvas = holder.lockCanvas();
                if (canvas == null){
                    continue;
                }

                drawGame(canvas);

                holder.unlockCanvasAndPost(canvas);
                synchronized (this){
                    try {
                        wait(DRAW_INTERVAL);
                    }catch (InterruptedException e){
                    }
                }
            }
        }
    }

    private DrawThread drawThread;

    public void startDrawThread(){
        stopDrawThread();

        drawThread = new DrawThread();
        drawThread.start();
    }

    public boolean stopDrawThread(){
        if (drawThread == null){
            return false;
        }
        drawThread.finish();
        drawThread = null;

        return true;
    }

    private static final int GROUND_MOVE_TO_LEFT = 10;
    private static final int GROUND_HEIGHT = 50;

    private static final int ADD_GROUND_COUNT = 5;

    private static final int GROUND_WIDTH = 340;
    private static final int GROUND_BLOCK_HEIGHT = 100;

    private Ground lastGround;

    private final List<Ground> groundList = new ArrayList<>();
    private final Random rand = new Random(System.currentTimeMillis());

    private Bitmap droidBitmap;
    private Droid droid;

    private final Droid.Callback droidCallback = new Droid.Callback() {
        @Override
        public int getDistanceFromGround(Droid droid) {
            int width = getWidth();
            int height = getHeight();

            for (Ground ground : groundList){
                if (!ground.isShown(width, height)){
                    continue;
                }
                boolean horizontal = !(droid.hitRect.left >= ground.rect.right || droid.hitRect.right <= ground.rect.left);
                if (horizontal){

                    if (!ground.isSolid()){
                        return Integer.MAX_VALUE;
                    }
                    
                    int distanceFromGround =  ground.rect.top - droid.hitRect.bottom;
                    if (distanceFromGround < 0){
                        gameOver();
                        return Integer.MAX_VALUE;
                    }
                    return distanceFromGround;
                }
            }

            return Integer.MAX_VALUE;
        }
    };

    private final Handler handler = new Handler();

    public interface GameOverCallback{
        void onGameOver();
    }

    private GameOverCallback gameOverCallback;

    public void setCallback(GameOverCallback callback){
        gameOverCallback = callback;
    }

    private final AtomicBoolean isGameOver = new AtomicBoolean();

    private void gameOver(){
        if (isGameOver.get()){
            return;
        }

        isGameOver.set(true);
        droid.stop();

        handler.post(new Runnable(){
            @Override
            public void run(){
                gameOverCallback.onGameOver();
            }
        });
    }

    public GameView(Context context){
        super(context);

        droidBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.droid_twins);
        droid = new Droid(droidBitmap,0,0, droidCallback);

        getHolder().addCallback(this);
    }

    protected void drawGame(Canvas canvas){
        canvas.drawColor(Color.WHITE);

        int width = canvas.getWidth();
        int height = canvas.getHeight();

        if (lastGround == null){
            int top = height - GROUND_HEIGHT;
            lastGround = new Ground(0, top, width, height);
            groundList.add(lastGround);
        }

        if (lastGround.isShown(width, height)){
            for (int i = 0; i < ADD_GROUND_COUNT; i++){
                int left = lastGround.rect.right;

                int groundHeight = rand.nextInt(height / GROUND_BLOCK_HEIGHT) * GROUND_BLOCK_HEIGHT / 2 + GROUND_BLOCK_HEIGHT;

                int top = height - groundHeight;
                int right = left + GROUND_WIDTH;

                if (i % 2 == 0){
                    lastGround = new Ground(left, top, right, height);
                }else{
                    lastGround = new Blank(left, height, right, height);
                }
                groundList.add(lastGround);
            }
        }

        for (int i = 0; i < groundList.size(); i++){
            Ground ground = groundList.get(i);

            if(ground.isAvailable()){
                ground.move(GROUND_MOVE_TO_LEFT);
                if (ground.isShown(width, height)){
                    ground.draw(canvas);
                }
            }else{
                groundList.remove(ground);
                i--;
            }
        }
        droid.move();
        droid.draw(canvas);

        if (touchDownStartTime > 0){
            float elapsedTime = System.currentTimeMillis() - touchDownStartTime;
            canvas.drawRect(0, 0, width * (elapsedTime / MAX_TOUCH_TIME), POWER_GAUGE_HEIGHT, PAINT_POWER_GAUGE);
        }
//        invalidate();
    }

    private static final long MAX_TOUCH_TIME = 500;
    private long touchDownStartTime;

    @Override
    public boolean onTouchEvent(MotionEvent event){
        switch(event.getAction()){
            case MotionEvent.ACTION_DOWN:
                touchDownStartTime = System.currentTimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                float time = System.currentTimeMillis() - touchDownStartTime;
                jumpDroid(time);
                touchDownStartTime = 0;
                break;
        }
        return super.onTouchEvent(event);
    }

    private void jumpDroid(float time) {
        if (droidCallback.getDistanceFromGround(droid) > 0){
            return;
        }

        droid.jump(Math.min(time, MAX_TOUCH_TIME) / MAX_TOUCH_TIME);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        startDrawThread();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();
    }
}
