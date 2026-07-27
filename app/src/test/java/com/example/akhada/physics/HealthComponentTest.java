package com.example.akhada.physics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.example.akhada.entity.components.HealthComponent;

import org.junit.Test;

public class HealthComponentTest {
    @Test
    public void applyDamage_reducesHealth() {
        HealthComponent health = new HealthComponent(100f);
        health.applyDamage(30f);
        assertEquals(70f, health.currentHealth, 0.001f);
        assertFalse(health.isDead());
    }

    @Test
    public void applyDamage_clampsAtZero_andMarksDead() {
        HealthComponent health = new HealthComponent(100f);
        health.applyDamage(150f); // overkill damage
        assertEquals(0f, health.currentHealth, 0.001f);
        assertTrue(health.isDead());
    }

    @Test
    public void applyDamage_afterDeath_doesNothing() {
        HealthComponent health = new HealthComponent(100f);
        health.applyDamage(150f);
        health.applyDamage(50f); // should be a no-op now
        assertEquals(0f, health.currentHealth, 0.001f);
    }
}
