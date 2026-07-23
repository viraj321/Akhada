package com.example.akhada.core;

public class GameLoop extends Thread {
    private final SurfaceHolder surfaceHolder;
    private final GameView gameView;
    private volatile boolean running = false;

    public GameLoop(GameView gameView, SurfaceHolder holder) {
        this.gameView = gameView;
        this.surfaceHolder = holder;
    }

    public void setRunning(boolean running) { this.running = running; }

    @Override
    public void run() {
        while (running) {
            Canvas canvas = null;
            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    gameView.update();
                    gameView.render(canvas);
                }
            } finally {
                if (canvas != null) surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
