package com.example.akhada.render;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;

import com.example.akhada.physics.PointMass;

public class RagdollRenderer {
    public static void drawSpriteOnBone(Canvas canvas, Bitmap sprite, PointMass a, PointMass b,
                                        float spriteBaseLength, float widthScale, Paint paint) {
        float dx = b.pos.x - a.pos.x;
        float dy = b.pos.y - a.pos.y;
        float boneLength = (float) Math.sqrt(dx * dx + dy * dy);
        float angleDeg = (float) Math.toDegrees(Math.atan2(dy, dx));

        float scaleY = boneLength / spriteBaseLength;
        float scaleX = widthScale;

        Matrix matrix = new Matrix();
        // Assumes sprite's "up/down" axis in the source image runs along its height,
        // with the attachment point (e.g. shoulder) at the top edge (y=0)
        matrix.postScale(scaleX, scaleY);
        matrix.postRotate(angleDeg - 90f); // -90 because sprite is drawn vertically by default, bone angle is measured from horizontal
        matrix.postTranslate(a.pos.x, a.pos.y);

        canvas.drawBitmap(sprite, matrix, paint);
    }

    // Draws a sprite centered on a single point, rotated to face a direction
    // (e.g. turban on head, doesn't need to stretch between two joints)
    public static void drawSpriteOnPoint(Canvas canvas, Bitmap sprite, PointMass point,
                                         float desiredWidthPx, float rotationDeg, Paint paint) {

            float scale = desiredWidthPx / sprite.getWidth();

            Matrix matrix = new Matrix();
            // FIXED: center the bitmap at the origin BEFORE scaling/rotating,
            // so rotation pivots around its center, not its top-left corner
            matrix.postTranslate(-sprite.getWidth() / 2f, -sprite.getHeight() / 2f);
            matrix.postScale(scale, scale);
            matrix.postRotate(rotationDeg);
            matrix.postTranslate(point.pos.x, point.pos.y);

            canvas.drawBitmap(sprite, matrix, paint);
        }

        // NEW: computes how much the torso has tipped over, so garments can
        // rotate along with the body instead of always drawing upright.
        // Returns degrees to pass into drawSpriteOnPoint's rotationDeg.
        public static float getSpineRotationDeg(PointMass chest, PointMass hips) {
            float dx = hips.pos.x - chest.pos.x;
            float dy = hips.pos.y - chest.pos.y;
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));
            // when standing normally, hips are directly below chest, so the
            // "raw" angle is 90° — subtract that so 0° means "standing upright"
            return angle - 90f;
        }
//        float scale = desiredWidthPx / sprite.getWidth();
//
//        Matrix matrix = new Matrix();
//        matrix.postScale(scale, scale);
//        matrix.postRotate(rotationDeg);
//        matrix.postTranslate(
//                point.pos.x - (sprite.getWidth() * scale) / 2f,
//                point.pos.y - (sprite.getHeight() * scale) / 2f
//        );
//        canvas.drawBitmap(sprite, matrix, paint);
////        Matrix matrix = new Matrix();
////        matrix.postScale(scale, scale);
////        matrix.postRotate(rotationDeg);
////        matrix.postTranslate(
////                point.pos.x - (sprite.getWidth() * scale) / 2f,
////                point.pos.y - (sprite.getHeight() * scale) / 2f
////        );
////        canvas.drawBitmap(sprite, matrix, paint);
   }

