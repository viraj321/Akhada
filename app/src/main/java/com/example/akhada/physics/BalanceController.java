package com.example.akhada.physics;

public class BalanceController {
    private final RagdollBody body;
    private final float groundY;       // the Y coordinate of the "floor" for this fighter
    private final float standingHipHeight; // how far above groundY the hips should sit when upright

    // Tuning knobs — start here, adjust by feel
    private float hipCorrectionStrength = 0.05f;   // 0 = no correction, 1 = instant snap (too rigid)
    private float uprightCorrectionStrength = 0.04f;
    private float footAnchorStrength = 0.1f;

    private float hipDamping = 0.15f;
    private float uprightDamping = 0.2f;
    // pulls feet toward directly under hips

    private static final float HIP_DEADZONE = 2f;
    private static final float UPRIGHT_DEADZONE = 1.5f;
    private static final float FOOT_DEADZONE = 3f;
    private static final float MAX_CORRECTION = 1.5f;

    public BalanceController(RagdollBody body, float groundY, float standingHipHeight) {
        this.body = body;
        this.groundY = groundY;
        this.standingHipHeight = standingHipHeight;
    }

    // Call this once per physics substep, only while the fighter is STANDING.
    public void applyBalance() {
        correctHipHeight();
        correctUpright();
        correctFootPlacement();
        dampenFeet();
    }


    // Pulls hips back toward standing height — like a weak spring fighting gravity
    private void correctHipHeight() {
        float targetY = groundY - standingHipHeight;
        float error = targetY - body.hips.pos.y;
        if (Math.abs(error) < HIP_DEADZONE) return;
        float hipVelocityY = body.hips.pos.y - body.hips.prevPos.y;
        float correction = error * hipCorrectionStrength - hipVelocityY * hipDamping;
        correction = clamp(correction, -MAX_CORRECTION, MAX_CORRECTION);

        pushPoint(body.hips, new Vec2(0, correction));

//        float clampedError = clamp(error * hipCorrectionStrength, -MAX_CORRECTION, MAX_CORRECTION); // NEW
//        pushPoint(body.hips, new Vec2(0, clampedError));
//        float targetY = groundY - standingHipHeight;
//        float error = targetY - body.hips.pos.y;
//        Vec2 correction = new Vec2(0, error * hipCorrectionStrength);
//        pushPoint(body.hips, correction);
    }

    // Keeps chest above hips (resists toppling sideways) by nudging chest
    // horizontally back toward directly above the hips
    private void correctUpright() {
        float error = body.hips.pos.x - body.chest.pos.x;
        if (Math.abs(error) < UPRIGHT_DEADZONE) return;
        float chestVelX = body.chest.pos.x - body.chest.prevPos.x;
        float hipVelX = body.hips.pos.x - body.hips.prevPos.x;
        float relativeVelX = chestVelX - hipVelX;

        float correction = error * uprightCorrectionStrength - relativeVelX * uprightDamping;
        correction = clamp(correction, -MAX_CORRECTION, MAX_CORRECTION);

        pushPoint(body.chest, new Vec2(correction, 0));
        pushPoint(body.head, new Vec2(correction * 0.5f, 0));
//        float clampedError = clamp(error * uprightCorrectionStrength, -MAX_CORRECTION, MAX_CORRECTION); // NEW
//        pushPoint(body.chest, new Vec2(clampedError, 0));
//        pushPoint(body.head, new Vec2(clampedError * 0.5f, 0));
//        float error = body.hips.pos.x - body.chest.pos.x;
//        Vec2 correction = new Vec2(error * uprightCorrectionStrength, 0);
//        pushPoint(body.chest, correction);
//
//        // head follows the same correction, more gently, so it doesn't lag/whip
//        Vec2 headCorrection = new Vec2(error * uprightCorrectionStrength * 0.5f, 0);
//        pushPoint(body.head, headCorrection);
    }

    // Pulls feet toward being roughly under the hips on the X axis,
    // so the fighter doesn't stand with legs splayed at a weird angle
    private void correctFootPlacement() {
        float leftError = body.leftHip.pos.x - body.leftFoot.pos.x;
        if (Math.abs(leftError) >= FOOT_DEADZONE) {
            float clamped = clamp(leftError * footAnchorStrength, -MAX_CORRECTION, MAX_CORRECTION);
            pushPoint(body.leftFoot, new Vec2(clamped, 0));
        }

        float rightError = body.rightHip.pos.x - body.rightFoot.pos.x;
        if (Math.abs(rightError) >= FOOT_DEADZONE) {
            float clamped = clamp(rightError * footAnchorStrength, -MAX_CORRECTION, MAX_CORRECTION);
            pushPoint(body.rightFoot, new Vec2(clamped, 0));
        }
//        float leftTargetX = body.leftHip.pos.x;
//        float leftError = leftTargetX - body.leftFoot.pos.x;
//        pushPoint(body.leftFoot, new Vec2(leftError * footAnchorStrength, 0));
//
//        float rightTargetX = body.rightHip.pos.x;
//        float rightError = rightTargetX - body.rightFoot.pos.x;
//        pushPoint(body.rightFoot, new Vec2(rightError * footAnchorStrength, 0));
    }

    private void dampenFeet() {
        killHorizontalVelocity(body.leftFoot);
        killHorizontalVelocity(body.rightFoot);
    }
    private void killHorizontalVelocity(PointMass p) {
        if (p.pinned) return;
        float vx = p.pos.x - p.prevPos.x;
        p.prevPos.x = p.pos.x - vx * 0.5f; // halve horizontal velocity every frame while standing
    }


    // A "soft push" — moves pos directly rather than going through applyImpulse,
    // since this is a positional correction (like the constraint solver),
    // not a physical force. Keeps it stable regardless of frame timing.
    private void pushPoint(PointMass p, Vec2 correction) {
        if (p.pinned) return;
        p.pos = p.pos.add(correction);
        // nudge prevPos too, by a smaller amount, so this doesn't look like
        // a sudden teleport (velocity partially "catches up" to the correction)
        p.prevPos = p.prevPos.add(correction.scale(0.5f));
    }
    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
