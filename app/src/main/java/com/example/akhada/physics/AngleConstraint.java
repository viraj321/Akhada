package com.example.akhada.physics;

public class AngleConstraint {
    public PointMass a; // e.g. shoulder
    public PointMass b; // e.g. elbow — the joint vertex
    public PointMass c; // e.g. hand

    // Only a minimum matters — the angle between two vectors can never
    // exceed 180° anyway, so there's no wraparound and nothing to clamp on that end.
    public float minAngleDeg;

    public AngleConstraint(PointMass a, PointMass b, PointMass c, float minAngleDeg) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.minAngleDeg = minAngleDeg;
    }

    public void satisfy() {
        Vec2 baVec = a.pos.subtract(b.pos).normalized();
        Vec2 bcVec = c.pos.subtract(b.pos).normalized();

        float dot = clampFloat(baVec.x * bcVec.x + baVec.y * bcVec.y, -1f, 1f);
        float currentAngleRad = (float) Math.acos(dot); // always in [0, PI], no wraparound
        float minAngleRad = (float) Math.toRadians(minAngleDeg);

        if (currentAngleRad >= minAngleRad) return; // within allowed bend, nothing to do

        // Need to push bcVec outward until angle == minAngleRad.
        // Cross product z-sign tells us which rotational direction is "outward".
        float cross = baVec.x * bcVec.y - baVec.y * bcVec.x;
        float rotationDir = (cross >= 0) ? 1f : -1f;

        float baseAngle = (float) Math.atan2(baVec.y, baVec.x);
        float targetAngle = baseAngle + rotationDir * minAngleRad;

        float length = c.pos.subtract(b.pos).length();
        Vec2 newBC = new Vec2(
                (float) Math.cos(targetAngle) * length,
                (float) Math.sin(targetAngle) * length
        );

        if (!c.pinned) {
            c.pos = b.pos.add(newBC);
        }
    }

    private float clampFloat(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }
//    public PointMass a; // e.g. shoulder
//    public PointMass b; // e.g. elbow — the joint vertex being limited
//    public PointMass c; // e.g. hand
//
//    // Allowed angle range (degrees) between segment B->A and segment B->C,
//    // measured as a signed angle. 180° = fully straight arm.
//    // Smaller values = more bend allowed toward one side.
//    public float minAngleDeg;
//    public float maxAngleDeg;
//
//    public AngleConstraint(PointMass a, PointMass b, PointMass c, float minAngleDeg, float maxAngleDeg) {
//        this.a = a;
//        this.b = b;
//        this.c = c;
//        this.minAngleDeg = minAngleDeg;
//        this.maxAngleDeg = maxAngleDeg;
//    }
//
//    public void satisfy() {
//        Vec2 baVec = a.pos.subtract(b.pos); // b -> a
//        Vec2 bcVec = c.pos.subtract(b.pos); // b -> c
//
//        float angleBA = (float) Math.atan2(baVec.y, baVec.x);
//        float angleBC = (float) Math.atan2(bcVec.y, bcVec.x);
//
//        float relativeAngle = normalizeAngle(angleBC - angleBA);
//        float relativeDeg = (float) Math.toDegrees(relativeAngle);
//
//        float minRad = (float) Math.toRadians(minAngleDeg);
//        float maxRad = (float) Math.toRadians(maxAngleDeg);
//
//        float clampedRelative = relativeAngle;
//        boolean needsClamp = false;
//
//        if (relativeDeg < minAngleDeg) {
//            clampedRelative = minRad;
//            needsClamp = true;
//        } else if (relativeDeg > maxAngleDeg) {
//            clampedRelative = maxRad;
//            needsClamp = true;
//        }
//
//        if (!needsClamp) return;
//
//        // Rotate BC vector to the clamped angle, keeping its length
//        // (the distance Constraint between b and c handles length separately)
//        float targetAngle = angleBA + clampedRelative;
//        float length = bcVec.length();
//
//        Vec2 clampedBC = new Vec2(
//                (float) Math.cos(targetAngle) * length,
//                (float) Math.sin(targetAngle) * length
//        );
//
//        if (!c.pinned) {
//            c.pos = b.pos.add(clampedBC);
//        }
//    }
//
//    // keep angle in range [-PI, PI]
//    private float normalizeAngle(float angle) {
//        while (angle > Math.PI) angle -= 2 * Math.PI;
//        while (angle < -Math.PI) angle += 2 * Math.PI;
//        return angle;
//    }
}
