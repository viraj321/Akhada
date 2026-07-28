package com.example.akhada.ai;

public enum AIState {
    IDLE,       // standing still, watching
    APPROACH,   // moving toward the player
    ATTACK,     // in punch range, throwing a hit
    RETREAT,    // backing off after attacking (avoids spamming)
    STUNNED     // just got hit, brief pause before acting again
}