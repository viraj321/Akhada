package com.example.akhada.ai;

import com.example.akhada.combat.CombatSystem;
import com.example.akhada.entity.components.HealthComponent;
import com.example.akhada.entity.components.StaminaComponent;
import com.example.akhada.physics.MovementController;
import com.example.akhada.physics.PointMass;
import com.example.akhada.physics.RagdollBody;
import com.example.akhada.physics.Vec2;

public class AIController {
    private final RagdollBody self;
    private final RagdollBody target;
    private final MovementController mover;
    private final HealthComponent targetHealth;
    private final float scale; // NEW

    private AIState state = AIState.IDLE;
    private float stateTimer = 0f;
    private float retractTimer = 0f;
    private static final float RETRACT_DURATION = 0.3f;
    private static final float RETRACT_STRENGTH = 0.15f;

    private static final float APPROACH_DISTANCE = 250f;
    private static final float BASE_ATTACK_RANGE = 70f;   // renamed, will be scaled
    private static final float BASE_HIT_RADIUS = 60f;     // renamed, will be scaled
    private static final float ATTACK_COOLDOWN = 1.0f;
    private static final float STUN_DURATION = 0.5f;
    private static final float RETREAT_DURATION = 0.4f;
    private static final float AI_PUNCH_FORCE = 18f;
    private float currentMoveDirection = 0f;
    private final StaminaComponent selfStamina;
    private static final float PUNCH_STAMINA_COST = 30f;

    public interface OnHitLandedListener {
        void onHitLanded(float impulseMagnitude);
    }
    private OnHitLandedListener hitListener;


    public AIController(RagdollBody self, RagdollBody target, MovementController mover, HealthComponent targetHealth,StaminaComponent selfStamina, float scale) {
        this.self = self;
        this.target = target;
        this.mover = mover;
        this.targetHealth = targetHealth;
        this.scale = scale;
        this.selfStamina = selfStamina;
    }

    public void setOnHitLandedListener(OnHitLandedListener listener) {
        this.hitListener = listener;
    }

    public void onHitReceived() {
        state = AIState.STUNNED;
        stateTimer = 0f;
        mover.setDirection(0f);
    }
    private void setMoveDirection(float direction) {
        currentMoveDirection = direction;
        mover.setDirection(direction);
    }

    public float getCurrentMoveDirection() {
        return currentMoveDirection;
    }

    public void update(float dt) {
        retractHandIfNeeded();
        stateTimer += dt;
        float distance = self.hips.pos.x - target.hips.pos.x;
        float absDistance = Math.abs(distance);
        float attackRange = BASE_ATTACK_RANGE * scale; // NEW

        switch (state) {
            case IDLE:
               // mover.setDirection(0f);
                setMoveDirection(0f);
                if (absDistance <= attackRange) {
                    state = AIState.ATTACK;
                    stateTimer = 0f;
                } else {
                    state = AIState.APPROACH;
                }
                break;
//                mover.setDirection(0f);
//                if (absDistance > APPROACH_DISTANCE) {
//                    state = AIState.APPROACH;
//                } else if (absDistance <= attackRange) {
//                    state = AIState.ATTACK;
//                    stateTimer = 0f;
//                }
//                break;

            case APPROACH:
                //mover.setDirection(distance > 0 ? -1f : 1f);
                setMoveDirection(distance > 0 ? -1f : 1f);
                if (absDistance <= attackRange) {
                    state = AIState.ATTACK;
                    stateTimer = 0f;
                    //mover.setDirection(0f);
                    setMoveDirection(0f);
                }
                break;

            case ATTACK:
                //mover.setDirection(0f);
                setMoveDirection(0f);
                if (stateTimer >= ATTACK_COOLDOWN) {
                    throwPunch();
                    state = AIState.RETREAT;
                    stateTimer = 0f;
                } else if (absDistance > attackRange * 1.5f) {
                    state = AIState.APPROACH;
                }
                break;

            case RETREAT:
                //mover.setDirection(distance > 0 ? 1f : -1f);
                setMoveDirection(distance > 0 ? 1f : -1f);
                if (stateTimer >= RETREAT_DURATION) {
                    state = AIState.IDLE;
                    stateTimer = 0f;
                }
                break;

            case STUNNED:
                //mover.setDirection(0f);
                setMoveDirection(0f);
                if (stateTimer >= STUN_DURATION) {
                    state = AIState.IDLE;
                    stateTimer = 0f;
                }
                break;
        }
    }

    private void throwPunch() {
        float punchCost = 30f;
        if (!selfStamina.canAttack(punchCost)) return; // too tired, skip this swing
        selfStamina.spend(punchCost);
        PointMass[] targetPoints = target.points.toArray(new PointMass[0]);
        float hitRadius = BASE_HIT_RADIUS * scale;

        self.rightHand.collisionImmune = true; // NEW — immune for the duration of the swing+retract

        CombatSystem.HitResult result = CombatSystem.tryPunch(self.rightHand, targetPoints, hitRadius, AI_PUNCH_FORCE);

        if (result.landed) {
            targetHealth.applyDamage(result.damage);
            if (hitListener != null) {
                hitListener.onHitLanded(result.impulseMagnitude);
            }
        }

        retractTimer = RETRACT_DURATION;
//        PointMass[] targetPoints = target.points.toArray(new PointMass[0]);
//        float hitRadius = BASE_HIT_RADIUS * scale; // NEW
//        CombatSystem.HitResult result = CombatSystem.tryPunch(self.rightHand, targetPoints, hitRadius, AI_PUNCH_FORCE);
//
//        if (result.landed) {
//            targetHealth.applyDamage(result.damage);
//            if (hitListener != null) {
//                hitListener.onHitLanded(result.impulseMagnitude);
//            }
//        }
    }
    private void retractHandIfNeeded() {
        if (retractTimer <= 0f) {
            self.rightHand.collisionImmune = false; // NEW — turn immunity back off once retract window ends
            return;
        }
        retractTimer -= 1f / 60f;

        Vec2 toShoulder = self.rightShoulder.pos.subtract(self.rightHand.pos);
        Vec2 pull = toShoulder.scale(RETRACT_STRENGTH);
        self.rightHand.pos = self.rightHand.pos.add(pull);
        self.rightHand.prevPos = self.rightHand.prevPos.add(pull.scale(0.3f));
    }

    public AIState getState() {
        return state;
    }
}
