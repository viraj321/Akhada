package com.example.akhada.audio;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.example.akhada.R;

public class SoundManager {
    private SoundPool soundPool;
    private int hitLightId, hitHeavyId, knockdownId;
    private boolean loaded = false;

    public SoundManager(Context context) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4) // a few hits can overlap without cutting each other off
                .setAudioAttributes(attributes)
                .build();

        soundPool.setOnLoadCompleteListener((pool, sampleId, status) -> loaded = true);

        hitLightId = soundPool.load(context, R.raw.hit_light, 1);
        hitHeavyId = soundPool.load(context, R.raw.hit_heavy, 1);
        knockdownId = soundPool.load(context, R.raw.knockdown, 1);
    }

    public void playHit(boolean isHeavy) {
        if (!loaded) return; // avoid playing before sounds finish loading
        soundPool.play(isHeavy ? hitHeavyId : hitLightId, 1f, 1f, 1, 0, 1f);
    }

    public void playKnockdown() {
        if (!loaded) return;
        soundPool.play(knockdownId, 1f, 1f, 1, 0, 1f);
    }

    public void release() {
        soundPool.release();
    }
}
