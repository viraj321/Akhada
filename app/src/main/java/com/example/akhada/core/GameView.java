package com.example.akhada.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameLoop gameLoop;

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        gameLoop = new GameLoop(this, holder);
        gameLoop.setRunning(true);
        gameLoop.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        boolean retry = true;
        gameLoop.setRunning(false);
        while (retry) {
            try {
                gameLoop.join();
                retry = false;
            } catch (InterruptedException ignored) {}
        }
    }

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {}

    public void update() { /* engine.update() goes here later */ }

    public void render(Canvas canvas) {
        canvas.drawColor(Color.rgb(139, 69, 19)); // temp: dirt-brown akhada floor
    }

    public void pause() { if (gameLoop != null) surfaceDestroyed(getHolder()); }
    public void resume() { /* re-created in surfaceCreated automatically */ }
}
