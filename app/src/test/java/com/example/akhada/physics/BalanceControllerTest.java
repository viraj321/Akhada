package com.example.akhada.physics;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BalanceControllerTest {
    @Test
    public void applyBalance_pullsHipsTowardTargetHeight() {
        RagdollBody body = new RagdollBody(300, 400, 0); // dropped from mid-air
        float groundY = 800f;
        float standingHipHeight = 90f;
        BalanceController balance = new BalanceController(body, groundY, standingHipHeight);

        float targetY = groundY - standingHipHeight;
        float distBefore = Math.abs(body.hips.pos.y - targetY);

        for (int i = 0; i < 30; i++) balance.applyBalance();

        float distAfter = Math.abs(body.hips.pos.y - targetY);
        assertTrue("hips should move closer to standing height after correction",
                distAfter < distBefore);
    }
}
