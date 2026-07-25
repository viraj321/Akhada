package com.example.akhada.physics;

public class BalanceController {
    private final RagdollBody body;
    private final float groundY;       // the Y coordinate of the "floor" for this fighter
    private final float standingHipHeight; // how far above groundY the hips should sit when upright

    // Tuning knobs — start here, adjust by feel
    private float hipCorrectionStrength = 0.08f;   // 0 = no correction, 1 = instant snap (too rigid)
    private float uprightCorrectionStrength = 0.06f;
    private float footAnchorStrength = 0.15f;      // pulls feet toward directly under hips

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
    }

    // Pulls hips back toward standing height — like a weak spring fighting gravity
    private void correctHipHeight() {
        float targetY = groundY - standingHipHeight;
        float error = targetY - body.hips.pos.y;
        Vec2 correction = new Vec2(0, error * hipCorrectionStrength);
        pushPoint(body.hips, correction);
    }

    // Keeps chest above hips (resists toppling sideways) by nudging chest
    // horizontally back toward directly above the hips
    private void correctUpright() {
        float error = body.hips.pos.x - body.chest.pos.x;
        Vec2 correction = new Vec2(error * uprightCorrectionStrength, 0);
        pushPoint(body.chest, correction);

        // head follows the same correction, more gently, so it doesn't lag/whip
        Vec2 headCorrection = new Vec2(error * uprightCorrectionStrength * 0.5f, 0);
        pushPoint(body.head, headCorrection);
    }

    // Pulls feet toward being roughly under the hips on the X axis,
    // so the fighter doesn't stand with legs splayed at a weird angle
    private void correctFootPlacement() {
        float leftTargetX = body.leftHip.pos.x;
        float leftError = leftTargetX - body.leftFoot.pos.x;
        pushPoint(body.leftFoot, new Vec2(leftError * footAnchorStrength, 0));

        float rightTargetX = body.rightHip.pos.x;
        float rightError = rightTargetX - body.rightFoot.pos.x;
        pushPoint(body.rightFoot, new Vec2(rightError * footAnchorStrength, 0));
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
}
