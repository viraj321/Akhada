package com.example.akhada.physics;

public class PointMass {
    public Vec2 pos;
    public Vec2 prevPos;
    public boolean pinned = false;

    public PointMass(float x, float y) {
        this.pos = new Vec2(x, y);
        this.prevPos = new Vec2(x, y);
    }

    // Verlet integration: velocity is implicit as (pos - prevPos),
    // so we never store velocity directly.
    public void integrate(float dt, Vec2 gravity) {
        if (pinned) return;

        Vec2 velocity = pos.subtract(prevPos);
        Vec2 nextPos = pos.add(velocity).add(gravity.scale(dt * dt));

        prevPos = pos;
        pos = nextPos;
    }

    // crude floor/wall collision — push back inside bounds and
    // kill velocity a bit (simulates energy loss on bounce)
    public void constrainToBounds(float minX, float minY, float maxX, float maxY, float bounce) {
        Vec2 velocity = pos.subtract(prevPos);

        if (pos.y > maxY) {
            pos.y = maxY;
            prevPos.y = pos.y + velocity.y * bounce;
        }
        if (pos.x < minX) {
            pos.x = minX;
            prevPos.x = pos.x + velocity.x * bounce;
        }
        if (pos.x > maxX) {
            pos.x = maxX;
            prevPos.x = pos.x + velocity.x * bounce;
        }
    }
}
