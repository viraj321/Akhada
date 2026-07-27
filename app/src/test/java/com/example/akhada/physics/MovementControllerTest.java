package com.example.akhada.physics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class MovementControllerTest {
    @Test
    public void applyMovement_movesHipsInSetDirection() {
        RagdollBody body = new RagdollBody(300, 150, 0);
        MovementController mover = new MovementController(body);
        mover.setDirection(1f); // move right

        float startX = body.hips.pos.x;
        for (int i = 0; i < 10; i++) mover.applyMovement();

        assertTrue("hips should have moved right", body.hips.pos.x > startX);
    }

    @Test
    public void applyMovement_doesNothingWhenDirectionZero() {
        RagdollBody body = new RagdollBody(300, 150, 0);
        MovementController mover = new MovementController(body);
        mover.setDirection(0f);

        float startX = body.hips.pos.x;
        mover.applyMovement();

        assertEquals(startX, body.hips.pos.x, 0.001f);
    }
}
