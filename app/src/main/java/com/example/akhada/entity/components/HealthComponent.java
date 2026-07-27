package com.example.akhada.entity.components;

public class HealthComponent {
    public float maxHealth;
    public float currentHealth;
    private boolean isDead = false;

    public HealthComponent(float maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    public void applyDamage(float amount) {
        if (isDead) return;
        currentHealth = Math.max(0f, currentHealth - amount);
        if (currentHealth <= 0f) {
            isDead = true;
        }
    }

    public boolean isDead() {
        return isDead;
    }

    public float getHealthFraction() {
        return currentHealth / maxHealth;
    }
}
