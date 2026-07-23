package com.example.akhada;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

        private GameView gameView;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            gameView = new GameView(this);
            setContentView(gameView);
            // Optional: hide system UI for fullscreen immersive gameplay
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }

        @Override protected void onPause() { super.onPause(); gameView.pause(); }
        @Override protected void onResume() { super.onResume(); gameView.resume(); }
    }

