package com.example.akhada.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.akhada.physics.AngleConstraint;
import com.example.akhada.physics.Constraint;
import com.example.akhada.physics.PhysicsWorld;
import com.example.akhada.physics.PointMass;
import com.example.akhada.physics.RagdollBody;
import com.example.akhada.physics.Vec2;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameLoop gameLoop;
    private PhysicsWorld world;
    private RagdollBody ragdoll;

    //private PointMass shoulder, elbow, hand;
    //private PointMass pointA, pointB;

   // private PointMass testPoint;
  //  private final Vec2 gravity = new Vec2(0, 800f);

    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        world = new PhysicsWorld();
        ragdoll = new RagdollBody(300, 150); // drops from up high, nothing pinned = full ragdoll fall
        ragdoll.addTo(world);
//        shoulder = new PointMass(300, 100);
//        elbow    = new PointMass(340, 140); // 40px down-right from shoulder
//        hand     = new PointMass(380, 180); // 40px down-right from elbow
//
//        shoulder.pinned = true; // arm hangs from a fixed shoulder
//
//        world.points.add(shoulder);
//        world.points.add(elbow);
//        world.points.add(hand);
//
//        world.constraints.add(new Constraint(shoulder, elbow, 56f)); // upper arm
//        world.constraints.add(new Constraint(elbow, hand, 56f));     // forearm
//
//        // Elbow can bend from fully straight (180°) down to a 40° fold,
//        // but never hyperextend past straight or fold backward past 40°.
//        world.angleConstraints.add(new AngleConstraint(shoulder, elbow, hand, 40f, 180f));
////        pointA = new PointMass(300, 100);
//        pointB = new PointMass(340, 100); // 40px to the right — this becomes restLength
//
//        pointA.pinned = true; // anchor one end so you can see the bone swing, not just fall
//
//        world.points.add(pointA);
//        world.points.add(pointB);
//        world.constraints.add(new Constraint(pointA, pointB, 40f));
        //testPoint = new PointMass(300, 100);
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

    @Override public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        world.setBounds(0, 0, w, h);
    }

    //public void update() { /* engine.update() goes here later */ }
    public void update() {
        world.step(1f / 60f);
//        float dt = 1f / 60f;
//        testPoint.integrate(dt, gravity);
//        testPoint.constrainToBounds(0, 0, getWidth(), getHeight(), 0.6f);
    }


//    public void render(Canvas canvas) {
//        canvas.drawColor(Color.rgb(139, 69, 19)); // temp: dirt-brown akhada floor
//    }
public void render(Canvas canvas) {
    canvas.drawColor(Color.rgb(139, 69, 19));

    Paint bonePaint = new Paint();
    bonePaint.setColor(Color.WHITE);
    bonePaint.setStrokeWidth(8f);

    Paint jointPaint = new Paint();
    jointPaint.setColor(Color.YELLOW);

    // draw every bone
    drawBone(canvas, bonePaint, ragdoll.head, ragdoll.chest);
    drawBone(canvas, bonePaint, ragdoll.chest, ragdoll.hips);
    drawBone(canvas, bonePaint, ragdoll.chest, ragdoll.leftShoulder);
    drawBone(canvas, bonePaint, ragdoll.chest, ragdoll.rightShoulder);
    drawBone(canvas, bonePaint, ragdoll.leftShoulder, ragdoll.leftElbow);
    drawBone(canvas, bonePaint, ragdoll.leftElbow, ragdoll.leftHand);
    drawBone(canvas, bonePaint, ragdoll.rightShoulder, ragdoll.rightElbow);
    drawBone(canvas, bonePaint, ragdoll.rightElbow, ragdoll.rightHand);
    drawBone(canvas, bonePaint, ragdoll.hips, ragdoll.leftHip);
    drawBone(canvas, bonePaint, ragdoll.hips, ragdoll.rightHip);
    drawBone(canvas, bonePaint, ragdoll.leftHip, ragdoll.leftKnee);
    drawBone(canvas, bonePaint, ragdoll.leftKnee, ragdoll.leftFoot);
    drawBone(canvas, bonePaint, ragdoll.rightHip, ragdoll.rightKnee);
    drawBone(canvas, bonePaint, ragdoll.rightKnee, ragdoll.rightFoot);

    for (PointMass p : ragdoll.points) {
        canvas.drawCircle(p.pos.x, p.pos.y, 10f, jointPaint);
    }
}

    private void drawBone(Canvas canvas, Paint paint, PointMass a, PointMass b) {
        canvas.drawLine(a.pos.x, a.pos.y, b.pos.x, b.pos.y, paint);
    }
//    canvas.drawColor(Color.rgb(139, 69, 19));
//
//    Paint linePaint = new Paint();
//    linePaint.setColor(Color.WHITE);
//    linePaint.setStrokeWidth(8f);
//
//    Paint dotPaint = new Paint();
//    dotPaint.setColor(Color.YELLOW);
//
//    canvas.drawLine(shoulder.pos.x, shoulder.pos.y, elbow.pos.x, elbow.pos.y, linePaint);
//    canvas.drawLine(elbow.pos.x, elbow.pos.y, hand.pos.x, hand.pos.y, linePaint);
//
//    canvas.drawCircle(shoulder.pos.x, shoulder.pos.y, 14f, dotPaint);
//    canvas.drawCircle(elbow.pos.x, elbow.pos.y, 14f, dotPaint);
//    canvas.drawCircle(hand.pos.x, hand.pos.y, 14f, dotPaint);
//    canvas.drawColor(Color.rgb(139, 69, 19));
//
//    Paint linePaint = new Paint();
//    linePaint.setColor(Color.WHITE);
//    linePaint.setStrokeWidth(6f);
//
//    Paint dotPaint = new Paint();
//    dotPaint.setColor(Color.YELLOW);
//
//    canvas.drawLine(pointA.pos.x, pointA.pos.y, pointB.pos.x, pointB.pos.y, linePaint);
//    canvas.drawCircle(pointA.pos.x, pointA.pos.y, 15f, dotPaint);
//    canvas.drawCircle(pointB.pos.x, pointB.pos.y, 15f, dotPaint);
//    canvas.drawColor(Color.rgb(139, 69, 19));
//
//    Paint paint = new Paint();
//    paint.setColor(Color.YELLOW);
//    canvas.drawCircle(testPoint.pos.x, testPoint.pos.y, 20f, paint);


    public void pause() { if (gameLoop != null) surfaceDestroyed(getHolder()); }
    public void resume() { /* re-created in surfaceCreated automatically */ }
}
