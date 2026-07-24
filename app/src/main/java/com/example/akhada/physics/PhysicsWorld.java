package com.example.akhada.physics;

import java.util.ArrayList;
import java.util.List;

public class PhysicsWorld {
    public List<PointMass> points = new ArrayList<>();
    public List<Constraint> constraints = new ArrayList<>();
    public List<AngleConstraint> angleConstraints = new ArrayList<>(); // NEW
    public Vec2 gravity = new Vec2(0, 800f);
    public int constraintIterations = 6;

    private float minX, minY, maxX, maxY;

    public void setBounds(float minX, float minY, float maxX, float maxY) {
        this.minX = minX; this.minY = minY;
        this.maxX = maxX; this.maxY = maxY;
    }

    public void step(float dt) {
        for (PointMass p : points) {
            p.integrate(dt, gravity);
        }

        for (int i = 0; i < constraintIterations; i++) {
            for (Constraint c : constraints) {
                c.satisfy();
            }
            for (AngleConstraint ac : angleConstraints) { // NEW
                ac.satisfy();
            }
            for (PointMass p : points) {
                p.constrainToBounds(minX, minY, maxX, maxY, 0.6f);
            }
        }
    }
}
