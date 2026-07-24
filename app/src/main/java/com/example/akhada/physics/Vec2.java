package com.example.akhada.physics;

public class Vec2 {
    public float x, y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 subtract(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }

    public Vec2 scale(float factor) {
        return new Vec2(x * factor, y * factor);
    }

    public float length() {
        return (float) Math.sqrt(x * x + y * y);
    }

    // useful later for constraints/collision normals
    public Vec2 normalized() {
        float len = length();
        if (len == 0) return new Vec2(0, 0);
        return new Vec2(x / len, y / len);
    }
}
