package com.example.akhada.physics;

public class BalanceController {
    private float currentMoveDirection = 0f;
    private static final float RIGHTING_STRENGTH = 4f;
    private final RagdollBody body;
    private final float groundY;       // the Y coordinate of the "floor" for this fighter
    private final float standingHipHeight; // how far above groundY the hips should sit when upright

    // Tuning knobs — start here, adjust by feel
    private float hipCorrectionStrength = 0.05f;   // 0 = no correction, 1 = instant snap (too rigid)
    private float uprightCorrectionStrength = 0.04f;
    private float footAnchorStrength = 0.12f;

    private float hipDamping = 0.15f;
    private float uprightDamping = 0.2f;
    // pulls feet toward directly under hips

    private static final float HIP_DEADZONE = 2f;
    private static final float UPRIGHT_DEADZONE = 1.5f;
    private static final float FOOT_DEADZONE = 3f;
    private static final float MAX_CORRECTION = 1.5f;

    private float getUpBoostTimer = 0f;
    private static final float GET_UP_BOOST_DURATION = 0.8f;
    private static final float GET_UP_BOOST_MULTIPLIER = 3.5f;

    public BalanceController(RagdollBody body, float groundY, float standingHipHeight) {

        this.body = body;
        this.groundY = groundY;
        this.standingHipHeight = standingHipHeight;
    }
    public void beginGetUpBoost() {
        getUpBoostTimer = GET_UP_BOOST_DURATION;
    }
    public void setMoveDirection(float direction) {
        this.currentMoveDirection = direction;
    }

    // Call this once per physics substep, only while the fighter is STANDING.
    public void applyBalance() {
        float boost = 1f;
        if (getUpBoostTimer > 0f) {
            float progress = getUpBoostTimer / GET_UP_BOOST_DURATION; // 1 → 0 over the duration
            boost = 1f + (GET_UP_BOOST_MULTIPLIER - 1f) * progress; // eases from 3.5x down to 1x smoothly
            getUpBoostTimer -= 1f / 60f;
        }
        if (isUpsideDown()) {
            correctOrientation();
            return;
        }
//        float boost = getUpBoostTimer > 0f ? GET_UP_BOOST_MULTIPLIER : 1f;
//        if (getUpBoostTimer > 0f) getUpBoostTimer -= 1f / 60f;
       // boolean isMoving = moveDirection != 0f;
        boolean isMoving = currentMoveDirection != 0f;
        correctHipHeight(boost);
        correctUpright(boost);
        correctFootPlacement(isMoving ? boost * 0.15f : boost);
        if (!isMoving) {
            settleAllJoints();
        }
        //settleAllJoints();
       // dampenFeet();
    }
    private boolean isUpsideDown() {
        return body.head.pos.y > body.hips.pos.y - 10f;
    }
    private void correctOrientation() {
        // Strong push: head goes up, hips go down — over several frames this
        // rotates the whole skeleton back to right-side-up, since the
        // constraints/collision naturally drag the rest of the body along
        pushPoint(body.head, new Vec2(0, -RIGHTING_STRENGTH));
        pushPoint(body.chest, new Vec2(0, -RIGHTING_STRENGTH * 0.6f));
        pushPoint(body.hips, new Vec2(0, RIGHTING_STRENGTH * 0.4f));

        // also nudge feet toward the ground to help anchor the rotation
        // around a stable base rather than letting the whole body drift
        pushPoint(body.leftFoot, new Vec2(0, RIGHTING_STRENGTH * 0.3f));
        pushPoint(body.rightFoot, new Vec2(0, RIGHTING_STRENGTH * 0.3f));
    }
    private static final float GET_UP_BOOST_MAX_CORRECTION = 2.5f;

    // Pulls hips back toward standing height — like a weak spring fighting gravity
    private void correctHipHeight(float boost) {
        float targetY = groundY - standingHipHeight;
        float error = targetY - body.hips.pos.y;
        if (Math.abs(error) < HIP_DEADZONE) return;
        float hipVelocityY = body.hips.pos.y - body.hips.prevPos.y;
        float correction = error * hipCorrectionStrength * boost - hipVelocityY * hipDamping;
        correction = clamp(correction, -GET_UP_BOOST_MAX_CORRECTION, GET_UP_BOOST_MAX_CORRECTION);
        pushPoint(body.hips, new Vec2(0, correction));
//        correction = clamp(correction, -MAX_CORRECTION * boost, MAX_CORRECTION * boost);
//        pushPoint(body.hips, new Vec2(0, correction));
//        float targetY = groundY - standingHipHeight;
//        float error = targetY - body.hips.pos.y;
//        if (Math.abs(error) < HIP_DEADZONE) return;
//        float hipVelocityY = body.hips.pos.y - body.hips.prevPos.y;
//        float correction = error * hipCorrectionStrength - hipVelocityY * hipDamping;
//        correction = clamp(correction, -MAX_CORRECTION, MAX_CORRECTION);
//
//        pushPoint(body.hips, new Vec2(0, correction));

//        float clampedError = clamp(error * hipCorrectionStrength, -MAX_CORRECTION, MAX_CORRECTION); // NEW
//        pushPoint(body.hips, new Vec2(0, clampedError));
//        float targetY = groundY - standingHipHeight;
//        float error = targetY - body.hips.pos.y;
//        Vec2 correction = new Vec2(0, error * hipCorrectionStrength);
//        pushPoint(body.hips, correction);
    }

    // Keeps chest above hips (resists toppling sideways) by nudging chest
    // horizontally back toward directly above the hips
    private void correctUpright(float boost) {
        float error = body.hips.pos.x - body.chest.pos.x;
        if (Math.abs(error) < UPRIGHT_DEADZONE) return;
        float chestVelX = body.chest.pos.x - body.chest.prevPos.x;
        float hipVelX = body.hips.pos.x - body.hips.prevPos.x;
        float relativeVelX = chestVelX - hipVelX;
        float correction = error * uprightCorrectionStrength * boost - relativeVelX * uprightDamping;
        correction = clamp(correction, -MAX_CORRECTION * boost, MAX_CORRECTION * boost);
        pushPoint(body.chest, new Vec2(correction, 0));
        pushPoint(body.head, new Vec2(correction * 0.5f, 0));
//        float error = body.hips.pos.x - body.chest.pos.x;
//        if (Math.abs(error) < UPRIGHT_DEADZONE) return;
//        float chestVelX = body.chest.pos.x - body.chest.prevPos.x;
//        float hipVelX = body.hips.pos.x - body.hips.prevPos.x;
//        float relativeVelX = chestVelX - hipVelX;
//
//        float correction = error * uprightCorrectionStrength - relativeVelX * uprightDamping;
//        correction = clamp(correction, -MAX_CORRECTION, MAX_CORRECTION);
//
//        pushPoint(body.chest, new Vec2(correction, 0));
//        pushPoint(body.head, new Vec2(correction * 0.5f, 0));
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
    private void correctFootPlacement(float boost) {
        float footTargetY = groundY - 4f;
        correctFootTowards(body.leftFoot, body.leftHip.pos.x, footTargetY, boost);
        correctFootTowards(body.rightFoot, body.rightHip.pos.x, footTargetY, boost);
//        float leftError = body.leftHip.pos.x - body.leftFoot.pos.x;
//        if (Math.abs(leftError) >= FOOT_DEADZONE) {
//            float clamped = clamp(leftError * footAnchorStrength, -MAX_CORRECTION, MAX_CORRECTION);
//            pushPoint(body.leftFoot, new Vec2(clamped, 0));
//        }
//
//        float rightError = body.rightHip.pos.x - body.rightFoot.pos.x;
//        if (Math.abs(rightError) >= FOOT_DEADZONE) {
//            float clamped = clamp(rightError * footAnchorStrength, -MAX_CORRECTION, MAX_CORRECTION);
//            pushPoint(body.rightFoot, new Vec2(clamped, 0));
//        }
//        float leftTargetX = body.leftHip.pos.x;
//        float leftError = leftTargetX - body.leftFoot.pos.x;
//        pushPoint(body.leftFoot, new Vec2(leftError * footAnchorStrength, 0));
//
//        float rightTargetX = body.rightHip.pos.x;
//        float rightError = rightTargetX - body.rightFoot.pos.x;
//        pushPoint(body.rightFoot, new Vec2(rightError * footAnchorStrength, 0));
    }
    private void correctFootTowards(PointMass foot, float targetX, float targetY, float boost) {
        float errX = targetX - foot.pos.x;
        float errY = targetY - foot.pos.y;
        float velX = foot.pos.x - foot.prevPos.x;
        float velY = foot.pos.y - foot.prevPos.y;
        float corrX = clamp(errX * footAnchorStrength * boost - velX * 0.2f, -MAX_CORRECTION * boost, MAX_CORRECTION * boost);
        float corrY = clamp(errY * footAnchorStrength * boost - velY * 0.2f, -MAX_CORRECTION * boost, MAX_CORRECTION * boost);
        pushPoint(foot, new Vec2(corrX, corrY));
    }
    private void settleAllJoints() {
        for (PointMass p : body.points) {
            if (p.pinned) continue;
            float vx = p.pos.x - p.prevPos.x;
            float vy = p.pos.y - p.prevPos.y;
            p.prevPos.x = p.pos.x - vx * 0.9f;
            p.prevPos.y = p.pos.y - vy * 0.9f;
        }
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
