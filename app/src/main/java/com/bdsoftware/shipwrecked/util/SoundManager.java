package com.bdsoftware.shipwrecked.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Looper;

public class SoundManager {

    private static SoundManager instance;

    private SoundPool soundPool;
    private MediaPlayer ambientPlayer;

    private int soundDiceRoll = -1;
    private int soundSuccess  = -1;
    private int soundFail     = -1;
    private int soundCritical = -1;

    private boolean soundEnabled = true;
    private boolean ambientEnabled = true;

    private final Context context;

    private SoundManager(Context context) {
        this.context = context.getApplicationContext();
        initialiseSoundPool();
        generateSounds();
    }

    public static synchronized SoundManager getInstance(Context context) {
        if (instance == null) {
            instance = new SoundManager(context);
        }
        return instance;
    }

    // -------------------------------------------------------
    // SoundPool setup
    // -------------------------------------------------------

    private void initialiseSoundPool() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        soundPool = new SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(attributes)
                .build();
    }

    // -------------------------------------------------------
    // Procedural sound generation
    // -------------------------------------------------------

    // All sounds are generated in code using AudioTrack so no audio files
    // are needed in the project at all.

    private void generateSounds() {
        new Thread(() -> {
            soundDiceRoll = loadGeneratedSound(generateDiceRollPcm());
            soundSuccess  = loadGeneratedSound(generateSuccessPcm());
            soundFail     = loadGeneratedSound(generateFailPcm());
            soundCritical = loadGeneratedSound(generateCriticalPcm());
        }).start();
    }

    // Dice roll: rapid noise burst simulating rattling dice
    private short[] generateDiceRollPcm() {
        int sampleRate = 44100;
        int duration   = (int)(sampleRate * 0.6); // 0.6 seconds
        short[] samples = new short[duration];
        java.util.Random rand = new java.util.Random();

        for (int i = 0; i < duration; i++) {
            float envelope = 1.0f - ((float) i / duration);
            // Mix white noise with a low rumble
            float noise = (rand.nextFloat() * 2f - 1f) * envelope;
            float rumble = (float) Math.sin(2 * Math.PI * 80 * i / sampleRate)
                    * envelope * 0.3f;
            samples[i] = (short)((noise + rumble) * 0.6f * Short.MAX_VALUE);
        }
        return samples;
    }

    // Success: rising two-note chime
    private short[] generateSuccessPcm() {
        int sampleRate = 44100;
        int duration   = (int)(sampleRate * 0.5);
        short[] samples = new short[duration];
        int half = duration / 2;

        for (int i = 0; i < duration; i++) {
            float envelope = (float) Math.exp(-3.0 * i / duration);
            float freq = (i < half) ? 523.25f : 659.25f; // C5 then E5
            float wave = (float) Math.sin(2 * Math.PI * freq * i / sampleRate);
            samples[i] = (short)(wave * envelope * 0.7f * Short.MAX_VALUE);
        }
        return samples;
    }

    // Fail: descending tone
    private short[] generateFailPcm() {
        int sampleRate = 44100;
        int duration   = (int)(sampleRate * 0.5);
        short[] samples = new short[duration];

        for (int i = 0; i < duration; i++) {
            float envelope = (float) Math.exp(-2.5 * i / duration);
            // Frequency slides down from 400Hz to 200Hz
            float freq = 400f - (200f * i / duration);
            float wave = (float) Math.sin(2 * Math.PI * freq * i / sampleRate);
            samples[i] = (short)(wave * envelope * 0.6f * Short.MAX_VALUE);
        }
        return samples;
    }

    // Critical success: bright three-note fanfare
    private short[] generateCriticalPcm() {
        int sampleRate = 44100;
        int duration   = (int)(sampleRate * 0.8);
        short[] samples = new short[duration];
        int third = duration / 3;

        float[] freqs = {523.25f, 659.25f, 783.99f}; // C5, E5, G5

        for (int i = 0; i < duration; i++) {
            float envelope = (float) Math.exp(-2.0 * i / duration);
            int segment = Math.min(i / third, 2);
            float wave = (float) Math.sin(2 * Math.PI * freqs[segment] * i / sampleRate);
            samples[i] = (short)(wave * envelope * 0.7f * Short.MAX_VALUE);
        }
        return samples;
    }

    // Convert PCM short array into a SoundPool sound ID via AudioTrack trick
    private int loadGeneratedSound(short[] pcm) {
        // Write to a temp AudioTrack to get a sound ID into SoundPool
        // We use a raw PCM approach via a background AudioTrack
        // Since SoundPool can't load from PCM directly, we play via AudioTrack
        // and store the PCM for direct playback instead
        return storePcm(pcm);
    }

    // -------------------------------------------------------
    // PCM storage for direct AudioTrack playback
    // -------------------------------------------------------

    private short[] diceRollPcm;
    private short[] successPcm;
    private short[] failPcm;
    private short[] criticalPcm;
    private boolean pcmReady = false;

    private int storePcm(short[] pcm) {
        // We use AudioTrack for playback instead of SoundPool
        // Return an index we use to identify which sound to play
        if (diceRollPcm == null)      { diceRollPcm = pcm; return 0; }
        else if (successPcm == null)  { successPcm  = pcm; return 1; }
        else if (failPcm == null)     { failPcm     = pcm; return 2; }
        else                          { criticalPcm = pcm; pcmReady = true; return 3; }
    }

    private void playPcm(short[] pcm) {
        if (pcm == null || !soundEnabled) return;

        new Thread(() -> {
            int sampleRate = 44100;
            int bufferSize = AudioTrack.getMinBufferSize(
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT);

            AudioTrack track = new AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    AudioFormat.CHANNEL_OUT_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    Math.max(bufferSize, pcm.length * 2),
                    AudioTrack.MODE_STATIC);

            track.write(pcm, 0, pcm.length);
            track.play();

            // Release after playback
            long durationMs = (long)(pcm.length / 44.1f);
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                track.stop();
                track.release();
            }, durationMs + 100);

        }).start();
    }

    // -------------------------------------------------------
    // Public playback methods
    // -------------------------------------------------------

    public void playDiceRoll() {
        playPcm(diceRollPcm);
    }

    public void playSuccess() {
        playPcm(successPcm);
    }

    public void playFail() {
        playPcm(failPcm);
    }

    public void playCriticalSuccess() {
        playPcm(criticalPcm);
    }

    public void playOutcome(String outcome) {
        switch (outcome) {
            case "CRITICAL_FAIL":   playFail();            break;
            case "FAIL":            playFail();            break;
            case "SUCCESS":         playSuccess();         break;
            case "CRITICAL_SUCCESS":playCriticalSuccess(); break;
        }
    }

    // -------------------------------------------------------
    // Ambient ocean sound
    // -------------------------------------------------------

    public void startAmbient() {
        if (!ambientEnabled) return;

        new Thread(() -> {
            // Generate a looping ocean ambient using layered sine waves
            // and pink noise approximation
            int sampleRate  = 44100;
            int duration    = sampleRate * 8; // 8 second loop
            short[] ambient = generateOceanPcm(sampleRate, duration);

            new Handler(Looper.getMainLooper()).post(() ->
                    playAmbientLoop(ambient, sampleRate));
        }).start();
    }

    private short[] generateOceanPcm(int sampleRate, int duration) {
        short[] samples = new short[duration];
        java.util.Random rand = new java.util.Random();

        for (int i = 0; i < duration; i++) {
            // Wave rhythm: slow oscillation simulating wave crests
            float waveEnvelope = (float)(0.5 + 0.5 * Math.sin(2 * Math.PI * 0.18 * i / sampleRate));
            float waveEnvelope2 = (float)(0.5 + 0.5 * Math.sin(2 * Math.PI * 0.11 * i / sampleRate + 1.2));

            // White noise base (surf)
            float noise = rand.nextFloat() * 2f - 1f;

            // Low frequency rumble (deep ocean)
            float rumble = (float) Math.sin(2 * Math.PI * 40 * i / sampleRate) * 0.15f;

            // Mid frequency wash
            float wash = (float) Math.sin(2 * Math.PI * 120 * i / sampleRate) * 0.08f;

            float sample = (noise * 0.4f * waveEnvelope)
                    + (noise * 0.3f * waveEnvelope2)
                    + rumble + wash;

            // Fade in and fade out for seamless looping
            float fadeSamples = sampleRate * 0.5f;
            if (i < fadeSamples) {
                sample *= i / fadeSamples;
            } else if (i > duration - fadeSamples) {
                sample *= (duration - i) / fadeSamples;
            }

            samples[i] = (short)(Math.max(-1f, Math.min(1f, sample)) * 0.5f * Short.MAX_VALUE);
        }
        return samples;
    }

    private AudioTrack ambientTrack;
    private boolean ambientPlaying = false;

    private void playAmbientLoop(short[] pcm, int sampleRate) {
        if (ambientTrack != null) {
            ambientTrack.stop();
            ambientTrack.release();
        }

        int bufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT);

        ambientTrack = new AudioTrack(
                AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                Math.max(bufferSize, pcm.length * 2),
                AudioTrack.MODE_STATIC);

        ambientTrack.write(pcm, 0, pcm.length);
        ambientTrack.setLoopPoints(0, pcm.length, -1); // Loop indefinitely
        ambientTrack.setVolume(0.35f);
        ambientTrack.play();
        ambientPlaying = true;
    }

    public void stopAmbient() {
        if (ambientTrack != null && ambientPlaying) {
            ambientTrack.stop();
            ambientPlaying = false;
        }
    }

    public void resumeAmbient() {
        if (ambientTrack != null && !ambientPlaying && ambientEnabled) {
            ambientTrack.play();
            ambientPlaying = true;
        }
    }

    // -------------------------------------------------------
    // Settings toggles
    // -------------------------------------------------------

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void setAmbientEnabled(boolean enabled) {
        this.ambientEnabled = enabled;
        if (!enabled) stopAmbient();
    }

    public boolean isSoundEnabled() { return soundEnabled; }
    public boolean isAmbientEnabled() { return ambientEnabled; }

    // -------------------------------------------------------
    // Cleanup
    // -------------------------------------------------------

    public void release() {
        stopAmbient();
        if (ambientTrack != null) {
            ambientTrack.release();
            ambientTrack = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        instance = null;
    }
}