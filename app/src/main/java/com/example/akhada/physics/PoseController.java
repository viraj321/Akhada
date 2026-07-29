package com.example.akhada.physics;

public class PoseController {
    private final RagdollBody body;
    private float strength = 0.08f;
    private float damping = 0.15f;
    private static final float MAX_PULL = 1.2f;
    private float walkPhase = 0f; // 0 to 1, loops
    private static final float WALK_CYCLE_SPEED = 3.5f;

    public PoseController(RagdollBody body) {
        this.body = body;
    }

    // Pulls a joint toward a target position relative to an anchor
    // (e.g. "elbow should be roughly here relative to the shoulder")
    private void pullToward(PointMass joint, PointMass anchor, Vec2 offsetFromAnchor) {
        Vec2 targetPos = anchor.pos.add(offsetFromAnchor);
        Vec2 error = targetPos.subtract(joint.pos);

        Vec2 jointVel = joint.pos.subtract(joint.prevPos);
        Vec2 anchorVel = anchor.pos.subtract(anchor.prevPos);
        Vec2 relativeVel = jointVel.subtract(anchorVel);

        float correctionX = error.x * strength - relativeVel.x * damping;
        float correctionY = error.y * strength - relativeVel.y * damping;

        correctionX = clamp(correctionX, -MAX_PULL, MAX_PULL);
        correctionY = clamp(correctionY, -MAX_PULL, MAX_PULL);

        if (!joint.pinned) {
            joint.pos = joint.pos.add(new Vec2(correctionX, correctionY));
            joint.prevPos = joint.prevPos.add(new Vec2(correctionX, correctionY).scale(0.5f));
        }
    }

    // A relaxed guard stance: elbows bent, hands up near chest level,
    // knees slightly bent, feet under hips
    public void applyIdleStance() {
        pullToward(body.leftElbow, body.leftShoulder, new Vec2(-8, 20));
        pullToward(body.leftHand, body.leftShoulder, new Vec2(-5, 35));
        pullToward(body.rightElbow, body.rightShoulder, new Vec2(8, 20));
        pullToward(body.rightHand, body.rightShoulder, new Vec2(5, 35));

        pullToward(body.leftKnee, body.leftHip, new Vec2(-2, 38));  // slight bend, not locked straight
        pullToward(body.rightKnee, body.rightHip, new Vec2(2, 38));
    }

    public void applyWalkCycle(float moveDirection) {
        if (moveDirection == 0f) {
            walkPhase = 0f; // reset to neutral when stopped, avoids frozen mid-stride pose
            applyIdleStance(); // fall back to idle arms/stance when not moving
            return;
        }

        walkPhase += WALK_CYCLE_SPEED * (1f / 60f);
        if (walkPhase > 1f) walkPhase -= 1f;

        // sine wave gives a smooth forward/back oscillation per leg, offset by half a cycle
        float leftSwing = (float) Math.sin(walkPhase * 2 * Math.PI);
        float rightSwing = (float) Math.sin((walkPhase + 0.5f) * 2 * Math.PI);

        float strideLength = 18f; // how far forward/back each foot swings

        // knees: lift slightly more when leg is swinging forward (positive swing)
        pullToward(body.leftKnee, body.leftHip, new Vec2(leftSwing * strideLength * 0.4f, 34f - Math.max(0, leftSwing) * 8f));
        pullToward(body.rightKnee, body.rightHip, new Vec2(rightSwing * strideLength * 0.4f, 34f - Math.max(0, rightSwing) * 8f));

        // feet: swing forward/back opposite to the knee lift timing, planting when swing crosses zero going down
        pullToward(body.leftFoot, body.leftHip, new Vec2(leftSwing * strideLength, 68f));
        pullToward(body.rightFoot, body.rightHip, new Vec2(rightSwing * strideLength, 68f));

        // arms swing opposite to same-side leg — natural counter-swing
        pullToward(body.leftElbow, body.leftShoulder, new Vec2(-rightSwing * 6f - 8f, 20));
        pullToward(body.leftHand, body.leftShoulder, new Vec2(-rightSwing * 10f - 5f, 35));
        pullToward(body.rightElbow, body.rightShoulder, new Vec2(-leftSwing * 6f + 8f, 20));
        pullToward(body.rightHand, body.rightShoulder, new Vec2(-leftSwing * 10f + 5f, 35));
    }

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
