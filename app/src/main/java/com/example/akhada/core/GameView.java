package com.example.akhada.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.akhada.R;
import com.example.akhada.ai.AIController;
import com.example.akhada.combat.CombatSystem;
import com.example.akhada.entity.components.FighterState;
import com.example.akhada.entity.components.HealthComponent;
import com.example.akhada.physics.AngleConstraint;
import com.example.akhada.physics.BalanceController;
import com.example.akhada.physics.Constraint;
import com.example.akhada.physics.MovementController;
import com.example.akhada.physics.PhysicsWorld;
import com.example.akhada.physics.PointMass;
import com.example.akhada.physics.PoseController;
import com.example.akhada.physics.RagdollBody;
import com.example.akhada.physics.Vec2;
import com.example.akhada.render.ParallaxBackground;
import com.example.akhada.render.RagdollRenderer;

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

    private MovementController moverB;
    private AIController aiB;
    private PoseController poseA, poseB;
    private float ragdollTimerA = 0f;
    private ParallaxBackground background = new ParallaxBackground();
    private Bitmap kurtaBitmap, dhotiBitmap, turbanBitmap, moustacheBitmap;
    public GameView(Context context) {
        super(context);
        getHolder().addCallback(this);
        kurtaBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.kurta_shirt);
        dhotiBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.dhoti_skirt);
        turbanBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.turban);
        moustacheBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moustache);
        world = new PhysicsWorld();
        world.pointRadius = 12f * 1.4f;
        float scale = 1.4f;
        fighterA = new RagdollBody(250, 150 , 0, scale);
        fighterB = new RagdollBody(450, 150, 1, scale);

        fighterA.addTo(world);
        fighterB.addTo(world);
        poseA = new PoseController(fighterA);
        poseB = new PoseController(fighterB);

        healthA = new HealthComponent(100f);
        healthB = new HealthComponent(100f);
        float groundY = 800f; // will be reset properly in surfaceChanged
        balanceA = new BalanceController(fighterA, groundY, 115f);
        balanceB = new BalanceController(fighterB, groundY, 115f);
        //moverA = new MovementController(fighterA);
        moverA = new MovementController(fighterA);
        moverB = new MovementController(fighterB);
        aiB = new AIController(fighterB, fighterA, moverB, healthA, scale);

        aiB.setOnHitLandedListener(impulseMagnitude -> {
            if (impulseMagnitude >= CombatSystem.KNOCKDOWN_THRESHOLD) {
                stateA = FighterState.RAGDOLL;
            }
        });
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
    private float retractTimerA = 0f;
    private static final float RETRACT_DURATION = 0.3f;
    private static final float RETRACT_STRENGTH = 0.15f;
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
                // NEW: guard — no punching while ragdolled or dead
                if (stateA == FighterState.STANDING && !healthA.isDead() && !healthB.isDead()) {
                    float scale = 1.4f;
                    PointMass[] targetPoints = fighterB.points.toArray(new PointMass[0]);

                    fighterA.rightHand.collisionImmune = true;
                    CombatSystem.HitResult result = CombatSystem.tryPunch(fighterA.rightHand, targetPoints, 60f * scale, 8f);
                    if (result.landed) {
                        healthB.applyDamage(result.damage);
                        aiB.onHitReceived();
                        if (result.impulseMagnitude >= CombatSystem.KNOCKDOWN_THRESHOLD) {
                            stateB = FighterState.RAGDOLL;
                        }
                    }
                    retractTimerA = RETRACT_DURATION;
                }
            }
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            inputDirection = 0f;
        }

        moverA.setDirection(inputDirection);
        return true;
    }
    private void retractHandA() {
        if (retractTimerA <= 0f) {
            fighterA.rightHand.collisionImmune = false;
            return;
        }
        retractTimerA -= 1f / 60f;

        Vec2 toShoulder = fighterA.rightShoulder.pos.subtract(fighterA.rightHand.pos);
        Vec2 pull = toShoulder.scale(RETRACT_STRENGTH);
        fighterA.rightHand.pos = fighterA.rightHand.pos.add(pull);
        fighterA.rightHand.prevPos = fighterA.rightHand.prevPos.add(pull.scale(0.3f));
    }
//        int action = event.getActionMasked();
//
//        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
//            float x = event.getX();
//            float y = event.getY();
//
//            if (leftButtonRect.contains(x, y)) {
//                inputDirection = -1f;
//            } else if (rightButtonRect.contains(x, y)) {
//                inputDirection = 1f;
//            } else if (action == MotionEvent.ACTION_DOWN) {
//                // tap elsewhere on screen = punch, same as before
//                if (!healthB.isDead()) {
//                    PointMass[] targetPoints = fighterB.points.toArray(new PointMass[0]);
//                    CombatSystem.HitResult result = CombatSystem.tryPunch(fighterA.rightHand, targetPoints, 60f, 18f);
//                    if (result.landed) {
//                        healthB.applyDamage(result.damage);
//                        aiB.onHitReceived();
//                        if (result.impulseMagnitude >= CombatSystem.KNOCKDOWN_THRESHOLD || healthB.isDead()) {
//                            stateB = FighterState.RAGDOLL;
//                        }
//                    }
//                }
//            }
//        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
//            inputDirection = 0f; // stop moving when finger lifts
//        }
//
//        moverA.setDirection(inputDirection);
//        return true;
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

    private float ragdollTimerB = 0f;
    private static final float RECOVERY_TIME = 2.0f;
    //public void update() { /* engine.update() goes here later */ }

    private float startupTimer = 0f;
    private static final float BALANCE_STARTUP_DELAY = 0.6f;
    public void update() {

        float dt = 1f / 60f;
        startupTimer += dt;
        retractHandA();
        // NEW: recovery logic
        if (stateB == FighterState.RAGDOLL) {
            ragdollTimerB += dt;
            if (ragdollTimerB >= RECOVERY_TIME && !healthB.isDead()) {
                stateB = FighterState.STANDING;
                ragdollTimerB = 0f;

                // IMPORTANT: re-anchor hips near current position before balance
                // kicks back in — otherwise it'll try to snap violently from
                // wherever it landed back to standing height in one frame
                recenterBalanceTarget(balanceB, fighterB);
            }
        } else {
            ragdollTimerB = 0f;
        }

        if (stateA == FighterState.RAGDOLL) {
            ragdollTimerA += dt;
            if (ragdollTimerA >= RECOVERY_TIME && !healthA.isDead()) {
                stateA = FighterState.STANDING;
                ragdollTimerA = 0f;
            }
        } else {
            ragdollTimerA = 0f;
        }


        java.util.List<BalanceController> activeBalance = new java.util.ArrayList<>();
        boolean balanceReady = startupTimer >= BALANCE_STARTUP_DELAY;
        if (stateA == FighterState.STANDING && !healthA.isDead()){ activeBalance.add(balanceA);
            //poseA.applyIdleStance();
            poseA.applyWalkCycle(inputDirection);

        }
        if (stateB == FighterState.STANDING && !healthB.isDead()) {
            aiB.update(1f / 60f);
        }
        if (stateB == FighterState.STANDING && !healthB.isDead()) {activeBalance.add(balanceB);
            //poseB.applyIdleStance();
            poseB.applyWalkCycle(aiB.getCurrentMoveDirection());
        }

        java.util.List<MovementController> activeMovement = new java.util.ArrayList<>();
        if (stateA == FighterState.STANDING && !healthA.isDead()) activeMovement.add(moverA);
        if (stateB == FighterState.STANDING && !healthB.isDead()) activeMovement.add(moverB);

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
    private void recenterBalanceTarget(BalanceController balance, RagdollBody body) {
        // no-op for now — placeholder in case you want a "getting up" animation
        // or a temporary invincibility window here later
    }


//    public void render(Canvas canvas) {
//        canvas.drawColor(Color.rgb(139, 69, 19)); // temp: dirt-brown akhada floor
//    }

   // private ParallaxBackground background = new ParallaxBackground();
public void render(Canvas canvas) {
    background.draw(canvas, getWidth(), getHeight());

    drawRagdoll(canvas, fighterA, Color.rgb(198, 134, 89));
    drawRagdoll(canvas, fighterB, Color.rgb(198, 134, 89));

    drawHealthBar(canvas, healthA, 50, 50, Color.rgb(255, 153, 51));
    drawHealthBar(canvas, healthB, getWidth() - 350, 50, Color.WHITE);

    // movement buttons (from earlier input step)
    Paint buttonPaint = new Paint();
    buttonPaint.setColor(Color.argb(120, 255, 255, 255));
    canvas.drawRoundRect(leftButtonRect, 20f, 20f, buttonPaint);
    canvas.drawRoundRect(rightButtonRect, 20f, 20f, buttonPaint);
//    background.draw(canvas, getWidth(), getHeight());
////    canvas.drawColor(Color.rgb(139, 69, 19));
//    drawRagdoll(canvas, fighterA, Color.rgb(198, 134, 89), Color.rgb(255, 153, 51));  // saffron dhoti
//    drawRagdoll(canvas, fighterB, Color.rgb(198, 134, 89), Color.rgb(255, 255, 255)); // white dhoti
////    drawRagdoll(canvas, fighterA, Color.CYAN);
////    drawRagdoll(canvas, fighterB, Color.MAGENTA);
//    drawHealthBar(canvas, healthA, 50, 50, Color.CYAN);
//    drawHealthBar(canvas, healthB, getWidth() - 350, 50, Color.MAGENTA);
//
//    Paint buttonPaint = new Paint();
//    buttonPaint.setColor(Color.argb(120, 255, 255, 255));
//    canvas.drawRoundRect(leftButtonRect, 20f, 20f, buttonPaint);
//    canvas.drawRoundRect(rightButtonRect, 20f, 20f, buttonPaint);
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

    private void drawRagdoll(Canvas canvas, RagdollBody body, int skinColor) {
        Paint limbPaint = new Paint();
        limbPaint.setColor(skinColor);
        limbPaint.setAntiAlias(true);

        Paint spritePaint = new Paint();
        spritePaint.setAntiAlias(true);
        spritePaint.setFilterBitmap(true);

        // legs first (drawn behind torso)
        drawBone(canvas, limbPaint, body.leftHip, body.leftKnee, 20f);
        drawBone(canvas, limbPaint, body.leftKnee, body.leftFoot, 16f);
        drawBone(canvas, limbPaint, body.rightHip, body.rightKnee, 20f);
        drawBone(canvas, limbPaint, body.rightKnee, body.rightFoot, 16f);

        // torso capsule (mostly hidden under kurta, but keeps shape consistent
        // if the sprite doesn't fully cover, e.g. during odd ragdoll poses)
        drawBone(canvas, limbPaint, body.chest, body.hips, 34f);
        drawBone(canvas, limbPaint, body.chest, body.leftShoulder, 22f);
        drawBone(canvas, limbPaint, body.chest, body.rightShoulder, 22f);
        drawBone(canvas, limbPaint, body.hips, body.leftHip, 18f);
        drawBone(canvas, limbPaint, body.hips, body.rightHip, 18f);

        float spineRotation = RagdollRenderer.getSpineRotationDeg(body.chest, body.hips);
        RagdollRenderer.drawSpriteOnPoint(canvas, dhotiBitmap, body.hips, 55f, spineRotation, spritePaint);
        RagdollRenderer.drawSpriteOnPoint(canvas, kurtaBitmap, body.chest, 60f, spineRotation, spritePaint);


        // garments layered on top of torso/hips
//        RagdollRenderer.drawSpriteOnPoint(canvas, dhotiBitmap, body.hips, 55f, 0f, spritePaint);
//        RagdollRenderer.drawSpriteOnPoint(canvas, kurtaBitmap, body.chest, 60f, 0f, spritePaint);

        // arms drawn on top of garments (so hands/forearms aren't hidden under the kurta)
        drawBone(canvas, limbPaint, body.leftShoulder, body.leftElbow, 16f);
        drawBone(canvas, limbPaint, body.leftElbow, body.leftHand, 14f);
        drawBone(canvas, limbPaint, body.rightShoulder, body.rightElbow, 16f);
        drawBone(canvas, limbPaint, body.rightElbow, body.rightHand, 14f);

        // neck + head
        drawBone(canvas, limbPaint, body.chest, body.head, 12f);
        canvas.drawCircle(body.head.pos.x, body.head.pos.y, 24f, limbPaint);

        // face details on top of head
        RagdollRenderer.drawSpriteOnPoint(canvas, turbanBitmap, body.head, 45f, spineRotation, spritePaint);
        RagdollRenderer.drawSpriteOnPoint(canvas, moustacheBitmap, body.head, 20f, spineRotation, spritePaint);
//        Paint torsoPaint = new Paint();
//        torsoPaint.setColor(clothColor);
//        torsoPaint.setAntiAlias(true);
//
//        Paint limbPaint = new Paint();
//        limbPaint.setColor(skinColor);
//        limbPaint.setAntiAlias(true);
//
//
//        // legs first (furthest back)
//        drawBone(canvas, torsoPaint, body.leftHip, body.leftKnee, 20f);
//        drawBone(canvas, torsoPaint, body.leftKnee, body.leftFoot, 16f);
//        drawBone(canvas, torsoPaint, body.rightHip, body.rightKnee, 20f);
//        drawBone(canvas, torsoPaint, body.rightKnee, body.rightFoot, 16f);
//
//        // torso + connectors
//        drawBone(canvas, torsoPaint, body.chest, body.hips, 34f);
//        drawBone(canvas, torsoPaint, body.chest, body.leftShoulder, 22f);
//        drawBone(canvas, torsoPaint, body.chest, body.rightShoulder, 22f);
//        drawBone(canvas, torsoPaint, body.hips, body.leftHip, 18f);
//        drawBone(canvas, torsoPaint, body.hips, body.rightHip, 18f);
//
//        // arms on top
//        drawBone(canvas, limbPaint, body.leftShoulder, body.leftElbow, 16f);
//        drawBone(canvas, limbPaint, body.leftElbow, body.leftHand, 14f);
//        drawBone(canvas, limbPaint, body.rightShoulder, body.rightElbow, 16f);
//        drawBone(canvas, limbPaint, body.rightElbow, body.rightHand, 14f);
//
//        // neck + head last (frontmost)
//        drawBone(canvas, limbPaint, body.chest, body.head, 12f);
//        canvas.drawCircle(body.head.pos.x, body.head.pos.y, 24f, limbPaint);
        // Torso — thickest segment
//        drawBone(canvas, torsoPaint, body.chest, body.hips, 34f);
//
//        // Arms — medium thickness, skin tone
//        drawBone(canvas, limbPaint, body.leftShoulder, body.leftElbow, 16f);
//        drawBone(canvas, limbPaint, body.leftElbow, body.leftHand, 14f);
//        drawBone(canvas, limbPaint, body.rightShoulder, body.rightElbow, 16f);
//        drawBone(canvas, limbPaint, body.rightElbow, body.rightHand, 14f);
//
//        // Legs — thicker than arms, clothed color (dhoti-ish)
//        drawBone(canvas, torsoPaint, body.leftHip, body.leftKnee, 20f);
//        drawBone(canvas, torsoPaint, body.leftKnee, body.leftFoot, 16f);
//        drawBone(canvas, torsoPaint, body.rightHip, body.rightKnee, 20f);
//        drawBone(canvas, torsoPaint, body.rightKnee, body.rightFoot, 16f);
//
//        // Shoulder/hip connectors — thin, just structural
//        Paint connectorPaint = new Paint();
//        connectorPaint.setColor(clothColor);
//        drawBone(canvas, connectorPaint, body.chest, body.leftShoulder, 22f);
//        drawBone(canvas, connectorPaint, body.chest, body.rightShoulder, 22f);
//        drawBone(canvas, connectorPaint, body.hips, body.leftHip, 18f);
//        drawBone(canvas, connectorPaint, body.hips, body.rightHip, 18f);
//
//        // Head — a circle instead of a bone
//        Paint headPaint = new Paint();
//        headPaint.setColor(skinColor);
//        headPaint.setAntiAlias(true);
//        canvas.drawCircle(body.head.pos.x, body.head.pos.y, 24f, headPaint);
//
//        // Neck connector
//        drawBone(canvas, limbPaint, body.chest, body.head, 12f);
    }
//    private void drawRagdoll(Canvas canvas, RagdollBody body, int jointColor) {
//        Paint bonePaint = new Paint();
//        bonePaint.setColor(Color.WHITE);
//        bonePaint.setStrokeWidth(8f);
//
//        drawBone(canvas, bonePaint, body.head, body.chest);
//        drawBone(canvas, bonePaint, body.chest, body.hips);
//        drawBone(canvas, bonePaint, body.chest, body.leftShoulder);
//        drawBone(canvas, bonePaint, body.chest, body.rightShoulder);
//        drawBone(canvas, bonePaint, body.leftShoulder, body.leftElbow);
//        drawBone(canvas, bonePaint, body.leftElbow, body.leftHand);
//        drawBone(canvas, bonePaint, body.rightShoulder, body.rightElbow);
//        drawBone(canvas, bonePaint, body.rightElbow, body.rightHand);
//        drawBone(canvas, bonePaint, body.hips, body.leftHip);
//        drawBone(canvas, bonePaint, body.hips, body.rightHip);
//        drawBone(canvas, bonePaint, body.leftHip, body.leftKnee);
//        drawBone(canvas, bonePaint, body.leftKnee, body.leftFoot);
//        drawBone(canvas, bonePaint, body.rightHip, body.rightKnee);
//        drawBone(canvas, bonePaint, body.rightKnee, body.rightFoot);
//
//        Paint jointPaint = new Paint();
//        jointPaint.setColor(jointColor);
//        for (PointMass p : body.points) {
//            canvas.drawCircle(p.pos.x, p.pos.y, 10f * 1.4f, jointPaint);
//        }
//    }
    private void drawBone(Canvas canvas, Paint paint, PointMass a, PointMass b, float thickness) {
        // Draw as a rounded-rectangle "capsule" instead of a thin line —
        // reads as a limb/body segment rather than a wireframe stick
        paint.setStrokeWidth(thickness);
        paint.setStrokeCap(Paint.Cap.ROUND); // rounded ends = joints look like actual joints, not sharp corners
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
