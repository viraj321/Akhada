package com.example.akhada.physics;

public class Constraint {
    public PointMass a, b;
    public float restLength;
    public boolean isRigid = true; // rigid bone vs. stretchy (e.g. cloth-like) — keep true for limbs

    public Constraint(PointMass a, PointMass b) {
        this.a = a;
        this.b = b;
        // rest length is whatever distance they started at
        this.restLength = a.pos.subtract(b.pos).length();
    }

    public Constraint(PointMass a, PointMass b, float restLength) {
        this.a = a;
        this.b = b;
        this.restLength = restLength;
    }

    // Pulls/pushes both points so the distance between them equals restLength.
    // Called multiple times per frame (relaxation) — one pass rarely fully
    // satisfies every constraint when several bones share a joint, so we
    // iterate until it converges close enough.
    public void satisfy() {
        Vec2 delta = b.pos.subtract(a.pos);
        float dist = delta.length();
        if (dist == 0) return; // avoid divide-by-zero if points overlap exactly

        float difference = (dist - restLength) / dist;
        Vec2 correction = delta.scale(0.5f * difference);

        if (!a.pinned) a.pos = a.pos.add(correction);
        if (!b.pinned) b.pos = b.pos.subtract(correction);
    }
}
