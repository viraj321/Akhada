package com.example.akhada.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.akhada.combat.CombatSystem;
import com.example.akhada.entity.components.FighterState;
import com.example.akhada.entity.components.HealthComponent;
import com.example.akhada.physics.AngleConstraint;
import com.example.akhada.physics.BalanceController;
import com.example.akhada.physics.Constraint;
import com.example.akhada.physics.MovementController;
import com.example.akhada.physics.PhysicsWorld;
import com.example.akhada.physics.PointMass;
import com.example.akhada.physics.RagdollBody;
import com.example.akhada.physics.Vec2;

public class GameView extends SurfaceView implements SurfaceHolder.Callback {
    private GameLoop gameLoop;
    private PhysicsWorld world;
    private RagdollBody fighterA, fighterB;
    //private RagdollBody ragdoll;
    private BalanceController balanceA, balanceB;
    private FighterState stateA = FighterState.STANDING;
    private FighterState stateB = FighterState.STANDING;

    //private PointMass shoulder, elbow, hand;
    //private PointMass pointA, pointB;

   // private PointMass testPoint;
  //  private final Vec2 gravity = new Vec2(0, 800f);
   private HealthComponent healthA, healthB;

    private MovementController moverA;
    private RectF leftButtonRect, rightButtonRect;
    private float inputDirection = 0f;
    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        world = new PhysicsWorld();
        fighterA = new RagdollBody(250, 150 , 0);
        fighterB = new RagdollBody(450, 150, 1);
        fighterA.addTo(world);
        fighterB.addTo(world);
        healthA = new HealthComponent(100f);
        healthB = new HealthComponent(100f);
        float groundY = 800f; // will be reset properly in surfaceChanged
        balanceA = new BalanceController(fighterA, groundY, 90f);
        balanceB = new BalanceController(fighterB, groundY, 90f);
        moverA = new MovementController(fighterA);
//        ragdoll = new RagdollBody(300, 150); // drops from up high, nothing pinned = full ragdoll fall
//        ragdoll.addTo(world);
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
        balanceA = new BalanceController(fighterA, h, 90f);
        balanceB = new BalanceController(fighterB, h, 90f);
        leftButtonRect  = new RectF(40, h - 180, 160, h - 60);
        rightButtonRect = new RectF(180, h - 180, 300, h - 60);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();

        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float x = event.getX();
            float y = event.getY();

            if (leftButtonRect.contains(x, y)) {
                inputDirection = -1f;
            } else if (rightButtonRect.contains(x, y)) {
                inputDirection = 1f;
            } else if (action == MotionEvent.ACTION_DOWN) {
                // tap elsewhere on screen = punch, same as before
                if (!healthB.isDead()) {
                    PointMass[] targetPoints = fighterB.points.toArray(new PointMass[0]);
                    CombatSystem.HitResult result = CombatSystem.tryPunch(fighterA.rightHand, targetPoints, 60f, 35f);
                    if (result.landed) {
                        healthB.applyDamage(result.damage);
                        if (result.impulseMagnitude >= CombatSystem.KNOCKDOWN_THRESHOLD || healthB.isDead()) {
                            stateB = FighterState.RAGDOLL;
                        }
                    }
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            inputDirection = 0f; // stop moving when finger lifts
        }

        moverA.setDirection(inputDirection);
        return true;
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            if (healthB.isDead()) return true; // no more hits needed once knocked out
//
//            PointMass[] targetPoints = fighterB.points.toArray(new PointMass[0]);
//            CombatSystem.HitResult result = CombatSystem.tryPunch(fighterA.rightHand, targetPoints, 60f, 35f);
//
//            if (result.landed) {
//                healthB.applyDamage(result.damage);
//
//                if (result.impulseMagnitude >= CombatSystem.KNOCKDOWN_THRESHOLD || healthB.isDead()) {
//                    stateB = FighterState.RAGDOLL;
//                }
//            }
//        }
//        return true;
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            PointMass[] targetPoints = fighterB.points.toArray(new PointMass[0]);
//            float impulseLanded = CombatSystem.tryPunch(fighterA.rightHand, targetPoints, 60f, 20f);
//
//            if (impulseLanded >= CombatSystem.KNOCKDOWN_THRESHOLD) {
//                stateB = FighterState.RAGDOLL; // hit was hard enough — go limp
//            }
//            // below threshold: stateB stays STANDING, balance correction
//            // will visibly absorb/resist the shove — looks like a stagger
//        }
//        return true;
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            // simplest possible test: A's right hand punches toward B's points
//            PointMass[] targetPoints = fighterB.points.toArray(new PointMass[0]);
//            CombatSystem.tryPunch(fighterA.rightHand, targetPoints, 60f, 25f);
//        }
//        return true;
    }

    //public void update() { /* engine.update() goes here later */ }
    public void update() {
        java.util.List<BalanceController> activeBalance = new java.util.ArrayList<>();
        if (stateA == FighterState.STANDING) activeBalance.add(balanceA);
        if (stateB == FighterState.STANDING && !healthB.isDead()) activeBalance.add(balanceB);

        java.util.List<MovementController> activeMovement = new java.util.ArrayList<>();
        if (stateA == FighterState.STANDING) activeMovement.add(moverA);

        world.step(1f / 60f, activeBalance, activeMovement);
//        java.util.List<BalanceController> active = new java.util.ArrayList<>();
//        if (stateA == FighterState.STANDING) active.add(balanceA);
//        if (stateB == FighterState.STANDING && !healthB.isDead()) active.add(balanceB);
//        java.util.List<MovementController> activeMovement = new java.util.ArrayList<>();
//        if (stateA == FighterState.STANDING) activeMovement.add(moverA);
//
////        java.util.List<BalanceController> active = new java.util.ArrayList<>();
////        if (stateA == FighterState.STANDING) active.add(balanceA);
////        if (stateB == FighterState.STANDING) active.add(balanceB);
//        world.step(1f / 60f, activeBalance, activeMovement);
       // world.step(1f / 60f, active);
        //world.step(1f / 60f);
//        float dt = 1f / 60f;
//        testPoint.integrate(dt, gravity);
//        testPoint.constrainToBounds(0, 0, getWidth(), getHeight(), 0.6f);
    }


//    public void render(Canvas canvas) {
//        canvas.drawColor(Color.rgb(139, 69, 19)); // temp: dirt-brown akhada floor
//    }
public void render(Canvas canvas) {
    canvas.drawColor(Color.rgb(139, 69, 19));
    drawRagdoll(canvas, fighterA, Color.CYAN);
    drawRagdoll(canvas, fighterB, Color.MAGENTA);
    drawHealthBar(canvas, healthA, 50, 50, Color.CYAN);
    drawHealthBar(canvas, healthB, getWidth() - 350, 50, Color.MAGENTA);

    Paint buttonPaint = new Paint();
    buttonPaint.setColor(Color.argb(120, 255, 255, 255));
    canvas.drawRoundRect(leftButtonRect, 20f, 20f, buttonPaint);
    canvas.drawRoundRect(rightButtonRect, 20f, 20f, buttonPaint);
//    canvas.drawColor(Color.rgb(139, 69, 19));
//
//    Paint bonePaint = new Paint();
//    bonePaint.setColor(Color.WHITE);
//    bonePaint.setStrokeWidth(8f);
//
//    Paint jointPaint = new Paint();
//    jointPaint.setColor(Color.YELLOW);
//
//    // draw every bone
//    drawBone(canvas, bonePaint, ragdoll.head, ragdoll.chest);
//    drawBone(canvas, bonePaint, ragdoll.chest, ragdoll.hips);
//    drawBone(canvas, bonePaint, ragdoll.chest, ragdoll.leftShoulder);
//    drawBone(canvas, bonePaint, ragdoll.chest, ragdoll.rightShoulder);
//    drawBone(canvas, bonePaint, ragdoll.leftShoulder, ragdoll.leftElbow);
//    drawBone(canvas, bonePaint, ragdoll.leftElbow, ragdoll.leftHand);
//    drawBone(canvas, bonePaint, ragdoll.rightShoulder, ragdoll.rightElbow);
//    drawBone(canvas, bonePaint, ragdoll.rightElbow, ragdoll.rightHand);
//    drawBone(canvas, bonePaint, ragdoll.hips, ragdoll.leftHip);
//    drawBone(canvas, bonePaint, ragdoll.hips, ragdoll.rightHip);
//    drawBone(canvas, bonePaint, ragdoll.leftHip, ragdoll.leftKnee);
//    drawBone(canvas, bonePaint, ragdoll.leftKnee, ragdoll.leftFoot);
//    drawBone(canvas, bonePaint, ragdoll.rightHip, ragdoll.rightKnee);
//    drawBone(canvas, bonePaint, ragdoll.rightKnee, ragdoll.rightFoot);
//
//    for (PointMass p : ragdoll.points) {
//        canvas.drawCircle(p.pos.x, p.pos.y, 10f, jointPaint);
//    }
}
    private void drawHealthBar(Canvas canvas, HealthComponent health, float x, float y, int color) {
        Paint bgPaint = new Paint();
        bgPaint.setColor(Color.DKGRAY);
        canvas.drawRect(x, y, x + 300, y + 30, bgPaint);

        Paint fgPaint = new Paint();
        fgPaint.setColor(color);
        canvas.drawRect(x, y, x + 300 * health.getHealthFraction(), y + 30, fgPaint);
    }

    private void drawRagdoll(Canvas canvas, RagdollBody body, int jointColor) {
        Paint bonePaint = new Paint();
        bonePaint.setColor(Color.WHITE);
        bonePaint.setStrokeWidth(8f);

        drawBone(canvas, bonePaint, body.head, body.chest);
        drawBone(canvas, bonePaint, body.chest, body.hips);
        drawBone(canvas, bonePaint, body.chest, body.leftShoulder);
        drawBone(canvas, bonePaint, body.chest, body.rightShoulder);
        drawBone(canvas, bonePaint, body.leftShoulder, body.leftElbow);
        drawBone(canvas, bonePaint, body.leftElbow, body.leftHand);
        drawBone(canvas, bonePaint, body.rightShoulder, body.rightElbow);
        drawBone(canvas, bonePaint, body.rightElbow, body.rightHand);
        drawBone(canvas, bonePaint, body.hips, body.leftHip);
        drawBone(canvas, bonePaint, body.hips, body.rightHip);
        drawBone(canvas, bonePaint, body.leftHip, body.leftKnee);
        drawBone(canvas, bonePaint, body.leftKnee, body.leftFoot);
        drawBone(canvas, bonePaint, body.rightHip, body.rightKnee);
        drawBone(canvas, bonePaint, body.rightKnee, body.rightFoot);

        Paint jointPaint = new Paint();
        jointPaint.setColor(jointColor);
        for (PointMass p : body.points) {
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
