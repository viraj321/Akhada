package com.example.akhada.physics;

public class MovementController {
    private final RagdollBody body;
    private float moveSpeed = 4f; // pixels per fixed step — tune to taste
    private float currentDirection = 0f; // -1 = left, 0 = still, +1 = right

    public MovementController(RagdollBody body) {
        this.body = body;
    }

    public void setDirection(float direction) {
        // clamp so bad input can't sneak in something like 5.0
        currentDirection = Math.max(-1f, Math.min(1f, direction));
    }

    // Called once per physics substep, only while STANDING —
    // same pattern as BalanceController, so it converges smoothly
    // alongside the balance correction instead of fighting it.
    public void applyMovement() {
        if (currentDirection == 0f) return;

        Vec2 push = new Vec2(currentDirection * moveSpeed, 0);

        // Push the hips (center of mass) — balance correction handles
        // keeping everything else attached and upright as it moves
        pushPoint(body.hips, push);

        // A little push on the leading foot too, so it looks like it's
        // stepping rather than just sliding on ice
        if (currentDirection > 0) {
            pushPoint(body.rightFoot, push.scale(1.3f));
        } else {
            pushPoint(body.leftFoot, push.scale(1.3f));
        }
    }

    private void pushPoint(PointMass p, Vec2 correction) {
        if (p.pinned) return;
        p.pos = p.pos.add(correction);
        p.prevPos = p.prevPos.add(correction.scale(0.3f));
    }
}
