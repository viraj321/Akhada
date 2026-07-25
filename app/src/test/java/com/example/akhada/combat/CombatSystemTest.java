package com.example.akhada.combat;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.example.akhada.physics.PointMass;
import com.example.akhada.physics.Vec2;

import org.junit.Test;

public class CombatSystemTest {
    @Test
    public void tryPunch_landsWhenWithinRadius_andAppliesImpulse() {
        PointMass attackerHand = new PointMass(0, 0);
        PointMass target = new PointMass(30, 0); // 30px away, inside 60px hitRadius

        Vec2 prevPosBefore = target.prevPos;
        float impulseLanded = CombatSystem.tryPunch(attackerHand, new PointMass[]{target}, 60f, 30f);

        assertTrue("punch should land and return a positive impulse", impulseLanded > 0f);
        assertNotEquals("impulse should have shifted prevPos, creating implicit velocity",
                prevPosBefore.x, target.prevPos.x, 0.001f);
    }

    @Test
    public void tryPunch_missesWhenOutsideRadius() {
        PointMass attackerHand = new PointMass(0, 0);
        PointMass target = new PointMass(200, 0); // far outside 60px hitRadius

        float impulseLanded = CombatSystem.tryPunch(attackerHand, new PointMass[]{target}, 60f, 30f);

        assertEquals("punch should not land outside hit radius, returning 0", 0f, impulseLanded, 0.001f);
    }

    @Test
    public void tryPunch_returnsImpulseAboveKnockdownThreshold() {
        PointMass attackerHand = new PointMass(0, 0);
        PointMass target = new PointMass(30, 0);

        float impulseLanded = CombatSystem.tryPunch(attackerHand, new PointMass[]{target}, 60f, 50f);

        assertTrue("a strong punch should return an impulse exceeding the knockdown threshold",
                impulseLanded >= CombatSystem.KNOCKDOWN_THRESHOLD);
    }
//    @Test
//    public void tryPunch_landsWhenWithinRadius_andAppliesImpulse() {
//        PointMass attackerHand = new PointMass(0, 0);
//        PointMass target = new PointMass(30, 0); // 30px away, inside 60px hitRadius
//
//        Vec2 prevPosBefore = target.prevPos;
//        boolean landed = CombatSystem.tryPunch(attackerHand, new PointMass[]{target}, 60f, 30f);
//
//        assertTrue("punch should land within hit radius", landed);
//        assertNotEquals("impulse should have shifted prevPos, creating implicit velocity",
//                prevPosBefore.x, target.prevPos.x, 0.001f);
//    }
//
//    @Test
//    public void tryPunch_missesWhenOutsideRadius() {
//        PointMass attackerHand = new PointMass(0, 0);
//        PointMass target = new PointMass(200, 0); // far outside 60px hitRadius
//
//        boolean landed = CombatSystem.tryPunch(attackerHand, new PointMass[]{target}, 60f, 30f);
//
//        assertFalse("punch should not land outside hit radius", landed);
//    }
}
