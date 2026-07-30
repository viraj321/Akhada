package com.example.akhada.entity.components;

public class StaminaComponent {
    public float maxStamina;
    public float currentStamina;
    private float regenRate; // stamina per second
    private float regenDelay = 0.5f; // pause before regen kicks in after spending
    private float regenDelayTimer = 0f;

    public StaminaComponent(float maxStamina, float regenRate) {
        this.maxStamina = maxStamina;
        this.currentStamina = maxStamina;
        this.regenRate = regenRate;
    }

    public boolean canAttack(float cost) {
        return currentStamina >= cost;
    }

    public void spend(float cost) {
        currentStamina = Math.max(0f, currentStamina - cost);
        regenDelayTimer = regenDelay;
    }

    public void update(float dt) {
        if (regenDelayTimer > 0f) {
            regenDelayTimer -= dt;
            return;
        }
        currentStamina = Math.min(maxStamina, currentStamina + regenRate * dt);
    }

    public float getStaminaFraction() {
        return currentStamina / maxStamina;
    }
}
