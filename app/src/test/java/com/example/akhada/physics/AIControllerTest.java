package com.example.akhada.physics;

import static org.junit.Assert.assertEquals;

import com.example.akhada.ai.AIController;
import com.example.akhada.ai.AIState;
import com.example.akhada.entity.components.HealthComponent;

import org.junit.Test;

public class AIControllerTest {
    @Test
    public void update_transitionsFromIdleToApproach_whenFarFromTarget() {
        RagdollBody self = new RagdollBody(100, 150, 0, 1.4f);
        RagdollBody target = new RagdollBody(600, 150, 1 , 1.4f); // far away
        MovementController mover = new MovementController(self);
        HealthComponent targetHealth = new HealthComponent(100f);

        AIController ai = new AIController(self, target, mover, targetHealth);
        ai.update(1f / 60f);

        assertEquals(AIState.APPROACH, ai.getState());
    }

    @Test
    public void update_transitionsToAttack_whenCloseToTarget() {
        RagdollBody self = new RagdollBody(300, 150, 0);
        RagdollBody target = new RagdollBody(340, 150, 1); // close, within ATTACK_RANGE
        MovementController mover = new MovementController(self);
        HealthComponent targetHealth = new HealthComponent(100f);

        AIController ai = new AIController(self, target, mover, targetHealth);
        ai.update(1f / 60f);

        assertEquals(AIState.ATTACK, ai.getState());
    }

    @Test
    public void onHitReceived_forcesStunnedState() {
        RagdollBody self = new RagdollBody(300, 150, 0);
        RagdollBody target = new RagdollBody(340, 150, 1);
        MovementController mover = new MovementController(self);
        HealthComponent targetHealth = new HealthComponent(100f);

        AIController ai = new AIController(self, target, mover, targetHealth);
        ai.update(1f / 60f); // now in ATTACK
        ai.onHitReceived();

        assertEquals(AIState.STUNNED, ai.getState());
    }
}
