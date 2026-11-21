package com.blacksite.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Procedural low-latency sound synthesizer.
 * Generates crisp sci-fi sound effects dynamically using pure Java sound.
 * Zero external asset dependencies ensures reliable execution on any Windows machine.
 */
public class SoundEffects {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(4, r -> {
        Thread t = new Thread(r, "Audio-Synthesizer");
        t.setDaemon(true);
        return t;
    });

    private static final float SAMPLE_RATE = 44100f;

    public static void playPlayerShoot() {
        POOL.submit(() -> {
            int durationMs = 120;
            int numSamples = (int) (SAMPLE_RATE * durationMs / 1000f);
            byte[] data = new byte[numSamples];
            for (int i = 0; i < numSamples; i++) {
                float t = (float) i / numSamples;
                float env = (1.0f - t) * (1.0f - t);
                float freq = 320f * (1.0f - 0.7f * t);
                float noise = (float) (Math.random() * 2.0 - 1.0);
                float tone = (float) Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE);
                float sample = (0.7f * noise + 0.3f * tone) * env;
                data[i] = (byte) (Math.clamp(sample, -1f, 1f) * 127);
            }
            playPcm(data);
        });
    }

    public static void playEnemyLaser() {
        POOL.submit(() -> {
            int durationMs = 150;
            int numSamples = (int) (SAMPLE_RATE * durationMs / 1000f);
            byte[] data = new byte[numSamples];
            for (int i = 0; i < numSamples; i++) {
                float t = (float) i / numSamples;
                float env = 1.0f - t;
                // High-to-low laser chirp
                float freq = 1200f - 850f * t;
                float tone = (float) Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE);
                float sample = tone * env * 0.8f;
                data[i] = (byte) (Math.clamp(sample, -1f, 1f) * 127);
            }
            playPcm(data);
        });
    }

    public static void playHitMarker() {
        POOL.submit(() -> {
            int durationMs = 45;
            int numSamples = (int) (SAMPLE_RATE * durationMs / 1000f);
            byte[] data = new byte[numSamples];
            for (int i = 0; i < numSamples; i++) {
                float t = (float) i / numSamples;
                float tone = (float) Math.sin(2.0 * Math.PI * 1800f * i / SAMPLE_RATE);
                float sample = tone * (1.0f - t) * 0.9f;
                data[i] = (byte) (Math.clamp(sample, -1f, 1f) * 127);
            }
            playPcm(data);
        });
    }

    public static void playReload() {
        POOL.submit(() -> {
            // Mechanical click-clack
            playTone(800, 40, 0.6f);
            try { Thread.sleep(300); } catch (InterruptedException ignored) {}
            playTone(600, 50, 0.7f);
            try { Thread.sleep(400); } catch (InterruptedException ignored) {}
            playTone(1100, 60, 0.8f);
        });
    }

    public static void playSentryAlert() {
        POOL.submit(() -> {
            playTone(950, 70, 0.8f);
            try { Thread.sleep(90); } catch (InterruptedException ignored) {}
            playTone(1350, 90, 0.9f);
        });
    }

    public static void playSentryExplode() {
        POOL.submit(() -> {
            int durationMs = 450;
            int numSamples = (int) (SAMPLE_RATE * durationMs / 1000f);
            byte[] data = new byte[numSamples];
            for (int i = 0; i < numSamples; i++) {
                float t = (float) i / numSamples;
                float env = (1.0f - t) * (1.0f - t);
                float noise = (float) (Math.random() * 2.0 - 1.0);
                float rumble = (float) Math.sin(2.0 * Math.PI * 65f * (1.0f - 0.5f * t) * i / SAMPLE_RATE);
                float sample = (0.6f * noise + 0.4f * rumble) * env;
                data[i] = (byte) (Math.clamp(sample, -1f, 1f) * 127);
            }
            playPcm(data);
        });
    }

    public static void playTerminalBeep() {
        POOL.submit(() -> playTone(1500, 50, 0.5f));
    }

    public static void playHackComplete() {
        POOL.submit(() -> {
            playTone(1000, 80, 0.7f);
            try { Thread.sleep(90); } catch (InterruptedException ignored) {}
            playTone(1400, 100, 0.8f);
            try { Thread.sleep(110); } catch (InterruptedException ignored) {}
            playTone(1800, 180, 0.9f);
        });
    }

    public static void playVictory() {
        POOL.submit(() -> {
            int[] notes = {523, 659, 784, 1046}; // C5, E5, G5, C6
            for (int note : notes) {
                playTone(note, 150, 0.8f);
                try { Thread.sleep(130); } catch (InterruptedException ignored) {}
            }
        });
    }

    public static void playGameOver() {
        POOL.submit(() -> {
            int[] notes = {400, 360, 320, 260};
            for (int note : notes) {
                playTone(note, 180, 0.8f);
                try { Thread.sleep(170); } catch (InterruptedException ignored) {}
            }
        });
    }

    private static void playTone(float freq, int durationMs, float volume) {
        int numSamples = (int) (SAMPLE_RATE * durationMs / 1000f);
        byte[] data = new byte[numSamples];
        for (int i = 0; i < numSamples; i++) {
            float t = (float) i / numSamples;
            float env = (t < 0.1f) ? (t / 0.1f) : (1.0f - t);
            float tone = (float) Math.sin(2.0 * Math.PI * freq * i / SAMPLE_RATE);
            data[i] = (byte) (tone * env * volume * 127);
        }
        playPcm(data);
    }

    private static void playPcm(byte[] data) {
        try {
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format, data.length);
            line.start();
            line.write(data, 0, data.length);
            line.drain();
            line.close();
        } catch (Exception ignored) {
            // Gracefully ignore audio hardware initialization errors
        }
    }
}
