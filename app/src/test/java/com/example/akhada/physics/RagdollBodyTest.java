package com.example.akhada.physics;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class RagdollBodyTest {
    @Test
    public void construction_createsAllFourteenJoints() {
        RagdollBody body = new RagdollBody(300, 150);
        assertEquals(15, body.points.size());
    }

    @Test
    public void addTo_registersAllPointsAndConstraints() {
        RagdollBody body = new RagdollBody(300, 150);
        PhysicsWorld world = new PhysicsWorld();
        body.addTo(world);

        assertEquals(15, world.points.size());
        assertEquals(body.constraints.size(), world.constraints.size());
        assertEquals(body.angleConstraints.size(), world.angleConstraints.size());
    }

    @Test
    public void step_keepsAllBoneLengthsWithinTolerance() {
        RagdollBody body = new RagdollBody(300, 150);
        PhysicsWorld world = new PhysicsWorld();
        world.setBounds(0, 0, 1000, 1000);
        body.addTo(world);

        for (int i = 0; i < 60; i++) world.step(1f / 60f); // simulate 1 second of falling

        for (Constraint c : body.constraints) {
            float actualLength = c.a.pos.subtract(c.b.pos).length();
            assertEquals("bone should stay near rest length after settling",
                    c.restLength, actualLength, 3f); // small tolerance for iterative solver
        }
    }
}
