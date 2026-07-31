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

//    public void step(float dt) {
//        for (PointMass p : points) {
//            p.integrate(dt, gravity);
//        }
//
//        for (int i = 0; i < constraintIterations; i++) {
//            for (Constraint c : constraints) {
//                c.satisfy();
//            }
//            for (AngleConstraint ac : angleConstraints) { // NEW
//                ac.satisfy();
//            }
//            for (PointMass p : points) {
//                p.constrainToBounds(minX, minY, maxX, maxY, 0.6f);
//            }
//        }
//    }
    public float pointRadius = 12f; // how "thick" a joint is for collision purposes

    public void step(float dt) {
        for (PointMass p : points) {
            p.integrate(dt, gravity);
        }


        for (int i = 0; i < constraintIterations; i++) {
            for (Constraint c : constraints) {
                c.satisfy();
            }
            for (AngleConstraint ac : angleConstraints) {
                ac.satisfy();
            }
            resolvePointCollisions(); // NEW
            for (PointMass p : points) {
                p.constrainToBounds(minX, minY, maxX, maxY, 0.6f);
            }
        }
    }

    // Naive O(n^2) point-vs-point push-apart. Fine for two ragdolls (~28 points),
    // would need a spatial grid if you ever had many fighters on screen at once.
    private void resolvePointCollisions() {
        for (int i = 0; i < points.size(); i++) {
            for (int j = i + 1; j < points.size(); j++) {
                PointMass p1 = points.get(i);
                PointMass p2 = points.get(j);

                if (p1.ownerId == p2.ownerId) continue; // NEW: never collide with your own body

                Vec2 delta = p2.pos.subtract(p1.pos);
                float dist = delta.length();
                float minDist = pointRadius * 2f;

                if (dist == 0 || dist >= minDist) continue;

                float overlap = minDist - dist;
                Vec2 pushDir = delta.scale(1f / dist);
                if (p1.collisionImmune && !p2.collisionImmune) {
                    if (!p2.pinned) p2.pos = p2.pos.add(pushDir.scale(overlap));
                } else if (p2.collisionImmune && !p1.collisionImmune) {
                    if (!p1.pinned) p1.pos = p1.pos.subtract(pushDir.scale(overlap));
                } else {
                    Vec2 correction = pushDir.scale(overlap * 0.5f);
                    if (!p1.pinned) p1.pos = p1.pos.subtract(correction);
                    if (!p2.pinned) p2.pos = p2.pos.add(correction);
                }
//                Vec2 correction = pushDir.scale(overlap * 0.5f);
//                if (!p1.pinned) p1.pos = p1.pos.subtract(correction);
//                if (!p2.pinned) p2.pos = p2.pos.add(correction);
            }
        }
    }
    public void step(float dt, java.util.List<BalanceController> activeBalancers , java.util.List<MovementController> activeMovers) {
        for (PointMass p : points) {
            p.integrate(dt, gravity);
        }
        for (MovementController mc : activeMovers) {
            mc.applyMovement();
        }

        for (int i = 0; i < constraintIterations; i++) {
            for (Constraint c : constraints) {
                c.satisfy();
            }
            for (AngleConstraint ac : angleConstraints) {
                ac.satisfy();
            }
            resolvePointCollisions();

            // NEW: apply balance correction each iteration, same as constraints,
            // so it converges smoothly instead of fighting the solver
            for (BalanceController bc : activeBalancers) {
                bc.applyBalance();
            }
           // for (MovementController mc : activeMovers) mc.applyMovement();

            for (PointMass p : points) {
                p.constrainToBounds(minX, minY, maxX, maxY, 0.2f);
            }
        }
    }

}
