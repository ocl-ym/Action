package com.example.action;

import android.graphics.Canvas;

public class Blank extends Ground{
    public Blank(int left, int top, int right, int bottom) {
        super(left, top, right, bottom);
    }

    @Override
    public void draw(Canvas canvas){
    }

    @Override
    public boolean isSolid(){
        return false;
    }
}
