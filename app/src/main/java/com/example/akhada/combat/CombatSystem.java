package com.example.akhada.combat;

import com.example.akhada.physics.PointMass;
import com.example.akhada.physics.Vec2;

public class CombatSystem {
    public static final float KNOCKDOWN_THRESHOLD = 30f;
    public static float tryPunch(PointMass attackerHand, PointMass[] targetPoints, float hitRadius, float punchForce) {
        for (PointMass target : targetPoints) {
            float dist = attackerHand.pos.subtract(target.pos).length();
            if (dist < hitRadius) {
                Vec2 direction = target.pos.subtract(attackerHand.pos).normalized();
                target.applyImpulse(direction.scale(punchForce));
                return punchForce;
            }
        }
        return 0f;
    }
//    public static boolean tryPunch(PointMass attackerHand, PointMass[] targetPoints, float hitRadius, float punchForce) {
//        for (PointMass target : targetPoints) {
//            float dist = attackerHand.pos.subtract(target.pos).length();
//            if (dist < hitRadius) {
//                Vec2 direction = target.pos.subtract(attackerHand.pos).normalized();
//                target.applyImpulse(direction.scale(punchForce));
//                return true; // only land on the first point found this call
//            }
//        }
//        return false;
//    }
}
