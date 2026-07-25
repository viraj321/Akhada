package com.example.akhada.physics;

import java.util.ArrayList;
import java.util.List;

public class RagdollBody {
    public PointMass head, chest, hips;
    public PointMass leftShoulder, leftElbow, leftHand;
    public PointMass rightShoulder, rightElbow, rightHand;
    public PointMass leftHip, leftKnee, leftFoot;
    public PointMass rightHip, rightKnee, rightFoot;

    public List<PointMass> points = new ArrayList<>();
    public List<Constraint> constraints = new ArrayList<>();
    public List<AngleConstraint> angleConstraints = new ArrayList<>();

    // originX/originY = roughly where the hips/center of mass starts
    public RagdollBody(float originX, float originY,  int ownerId) {
        for (PointMass p : points) {
            p.ownerId = ownerId;
        }
        // Segment lengths (px) — tune these to your sprite scale later
        float headLen = 22f, neckLen = 16f, torsoLen = 46f;
        float upperArmLen = 34f, forearmLen = 30f;
        float thighLen = 42f, shinLen = 40f;

        // Build top-down: head -> chest -> hips
        head  = new PointMass(originX, originY - torsoLen - neckLen - headLen);
        chest = new PointMass(originX, originY - torsoLen);
        hips  = new PointMass(originX, originY);

        // Arms hang from chest, slightly out to each side
        leftShoulder  = new PointMass(originX - 12, originY - torsoLen);
        leftElbow     = new PointMass(originX - 12, originY - torsoLen + upperArmLen);
        leftHand      = new PointMass(originX - 12, originY - torsoLen + upperArmLen + forearmLen);

        rightShoulder = new PointMass(originX + 12, originY - torsoLen);
        rightElbow    = new PointMass(originX + 12, originY - torsoLen + upperArmLen);
        rightHand     = new PointMass(originX + 12, originY - torsoLen + upperArmLen + forearmLen);

        // Legs hang from hips
        leftHip   = new PointMass(originX - 10, originY);
        leftKnee  = new PointMass(originX - 10, originY + thighLen);
        leftFoot  = new PointMass(originX - 10, originY + thighLen + shinLen);

        rightHip  = new PointMass(originX + 10, originY);
        rightKnee = new PointMass(originX + 10, originY + thighLen);
        rightFoot = new PointMass(originX + 10, originY + thighLen + shinLen);

        points.add(head); points.add(chest); points.add(hips);
        points.add(leftShoulder); points.add(leftElbow); points.add(leftHand);
        points.add(rightShoulder); points.add(rightElbow); points.add(rightHand);
        points.add(leftHip); points.add(leftKnee); points.add(leftFoot);
        points.add(rightHip); points.add(rightKnee); points.add(rightFoot);

        // --- Core skeleton bones ---
        constraints.add(new Constraint(head, chest, neckLen + headLen * 0.5f));
        constraints.add(new Constraint(chest, hips, torsoLen));

        // shoulders/hips pinned to chest/hips as rigid width, not just floppy attach points
        constraints.add(new Constraint(chest, leftShoulder, 12f));
        constraints.add(new Constraint(chest, rightShoulder, 12f));
        constraints.add(new Constraint(hips, leftHip, 10f));
        constraints.add(new Constraint(hips, rightHip, 10f));

        // --- Arms ---
        constraints.add(new Constraint(leftShoulder, leftElbow, upperArmLen));
        constraints.add(new Constraint(leftElbow, leftHand, forearmLen));
        constraints.add(new Constraint(rightShoulder, rightElbow, upperArmLen));
        constraints.add(new Constraint(rightElbow, rightHand, forearmLen));

        // --- Legs ---
        constraints.add(new Constraint(leftHip, leftKnee, thighLen));
        constraints.add(new Constraint(leftKnee, leftFoot, shinLen));
        constraints.add(new Constraint(rightHip, rightKnee, thighLen));
        constraints.add(new Constraint(rightKnee, rightFoot, shinLen));

        // --- Diagonal stabilizers ---
        // Pure hinge chains (chest-hips-shoulders-hips) can collapse sideways
        // under stress since nothing resists shearing. A couple of diagonal
        // constraints across the torso box fix this cheaply, same trick used
        // in cloth/soft-body sims.
        constraints.add(new Constraint(leftShoulder, hips,
                leftShoulder.pos.subtract(hips.pos).length()));
        constraints.add(new Constraint(rightShoulder, hips,
                rightShoulder.pos.subtract(hips.pos).length()));

        // --- Angle limits (this is what stops it looking like cooked spaghetti) ---
        // Elbows: can fold to ~40°, can't hyperextend past straight
        angleConstraints.add(new AngleConstraint(leftShoulder, leftElbow, leftHand, 40f));
        angleConstraints.add(new AngleConstraint(rightShoulder, rightElbow, rightHand, 40f));

        // Knees: same idea, folding the other direction is handled by which
        // side of the joint you measure from — tune sign/range once you see it move
        angleConstraints.add(new AngleConstraint(leftHip, leftKnee, leftFoot, 40f));
        angleConstraints.add(new AngleConstraint(rightHip, rightKnee, rightFoot, 40f));

        // Neck: keep head roughly upright relative to chest/hips, allow some lean
       // angleConstraints.add(new AngleConstraint(chest, head, hips, 140f, 220f));
        angleConstraints.add(new AngleConstraint(head, chest, hips, 140f));
    }

    public void addTo(PhysicsWorld world) {
        world.points.addAll(points);
        world.constraints.addAll(constraints);
        world.angleConstraints.addAll(angleConstraints);
    }
}
