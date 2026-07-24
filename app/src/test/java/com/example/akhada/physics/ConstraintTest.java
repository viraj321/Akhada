package com.example.akhada.physics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ConstraintTest {
    @Test
    public void satisfy_pullsPointsToRestLength() {
        PointMass a = new PointMass(0, 0);
        PointMass b = new PointMass(100, 0); // starts 100 apart
        Constraint c = new Constraint(a, b, 50f); // should be 50 apart

        // one pass won't fully converge; run several like PhysicsWorld does
        for (int i = 0; i < 20; i++) c.satisfy();

        float finalDist = a.pos.subtract(b.pos).length();
        assertEquals(50f, finalDist, 0.01f);
    }

    @Test
    public void satisfy_respectsPinnedPoint() {
        PointMass a = new PointMass(0, 0);
        a.pinned = true;
        PointMass b = new PointMass(100, 0);
        Constraint c = new Constraint(a, b, 50f);

        for (int i = 0; i < 20; i++) c.satisfy();

        // pinned point must not have moved
        assertEquals(0f, a.pos.x, 0.001f);
        assertEquals(0f, a.pos.y, 0.001f);
    }
}
