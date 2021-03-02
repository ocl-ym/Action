package com.example.action;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements GameView.GameOverCallback{

    private GameView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        gameView = new GameView(this);
        gameView.setCallback(this);
        setContentView(gameView);
    }

    @Override
    public void onGameOver() {
        Toast.makeText(this, "Game Over", Toast.LENGTH_LONG).show();
    }
}