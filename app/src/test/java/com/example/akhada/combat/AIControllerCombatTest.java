package com.example.akhada.combat;

import static org.junit.Assert.assertTrue;

import com.example.akhada.ai.AIController;
import com.example.akhada.entity.components.HealthComponent;
import com.example.akhada.physics.MovementController;
import com.example.akhada.physics.RagdollBody;

import org.junit.Test;

public class AIControllerCombatTest {
    @Test
    public void throwPunch_damagesTargetHealth_whenInRange() {
        RagdollBody self = new RagdollBody(300, 150, 0 , 1.4f);
        RagdollBody target = new RagdollBody(340, 150, 1 , 1.4f); // close enough to hit
        MovementController mover = new MovementController(self);



        HealthComponent targetHealth = new HealthComponent(100f);

        AIController ai = new AIController(self, target, mover, targetHealth);

        // drive it through IDLE -> ATTACK -> cooldown elapsed -> punch thrown
        ai.update(1f / 60f); // enters ATTACK
        for (int i = 0; i < 61; i++) ai.update(1f / 60f); // let ATTACK_COOLDOWN (1.0s) elapse

        assertTrue("target should have taken damage from AI's punch",
                targetHealth.currentHealth < 100f);
    }
}
