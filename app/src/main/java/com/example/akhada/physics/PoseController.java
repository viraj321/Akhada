package com.example.akhada.physics;

public class PoseController {
    private final RagdollBody body;
    private float strength = 0.08f;
    private float damping = 0.15f;
    private static final float MAX_PULL = 1.2f;

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

    private float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
}
