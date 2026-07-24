package com.example.akhada.physics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AngleConstraintTest {


    @Test
    public void satisfy_preservesSegmentLength() {
        PointMass shoulder = new PointMass(0, 0);
        PointMass elbow = new PointMass(50, 0);
        PointMass hand = new PointMass(90, 40); // bent inward past limit

        float originalLength = hand.pos.subtract(elbow.pos).length();

        AngleConstraint ac = new AngleConstraint(shoulder, elbow, hand, 60f);
        ac.satisfy();

        float newLength = hand.pos.subtract(elbow.pos).length();
        assertEquals("length must be preserved, only angle changes", originalLength, newLength, 0.01f);
    }
}
