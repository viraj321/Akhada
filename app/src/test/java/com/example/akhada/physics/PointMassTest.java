package com.example.akhada.physics;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class PointMassTest {
    @Test
    public void integrate_appliesGravity_increasesDownwardVelocity() {
        PointMass p = new PointMass(0, 0);
        Vec2 gravity = new Vec2(0, 800f);
        float dt = 1f / 60f;

        p.integrate(dt, gravity);
        float firstStepY = p.pos.y;

        p.integrate(dt, gravity);
        float secondStepDelta = p.pos.y - firstStepY;

        // second step should fall farther than first step (accelerating)
        assertEquals(true, secondStepDelta > firstStepY);
    }

    @Test
    public void pinnedPoint_doesNotMove() {
        PointMass p = new PointMass(50, 50);
        p.pinned = true;
        p.integrate(1f / 60f, new Vec2(0, 800f));

        assertEquals(50, p.pos.x, 0.001f);
        assertEquals(50, p.pos.y, 0.001f);
    }
}