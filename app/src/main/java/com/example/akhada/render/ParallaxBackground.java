package com.example.akhada.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ParallaxBackground {
    public void draw(Canvas canvas, int width, int height) {
        // sky
        Paint skyPaint = new Paint();
        skyPaint.setColor(Color.rgb(255, 200, 130)); // warm dusk tone
        canvas.drawRect(0, 0, width, height * 0.6f, skyPaint);

        // distant temple silhouette shapes (simple triangles/rects)
        Paint templePaint = new Paint();
        templePaint.setColor(Color.rgb(120, 80, 60));
        canvas.drawRect(width * 0.1f, height * 0.35f, width * 0.22f, height * 0.6f, templePaint);
        canvas.drawRect(width * 0.7f, height * 0.3f, width * 0.85f, height * 0.6f, templePaint);

        // mud/akhada floor
        Paint floorPaint = new Paint();
        floorPaint.setColor(Color.rgb(139, 90, 60));
        canvas.drawRect(0, height * 0.6f, width, height, floorPaint);

        // floor texture lines (simple wrestling-pit rings)
        Paint linePaint = new Paint();
        linePaint.setColor(Color.rgb(115, 72, 48));
        linePaint.setStrokeWidth(3f);
        for (int i = 0; i < 5; i++) {
            float y = height * 0.65f + i * 30f;
            canvas.drawLine(0, y, width, y, linePaint);
        }
    }
}
