/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts.audio;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.util.BulkTimer;


/**
 * Provides an implementation of <code>AudioPlayer</code> that sends
 * all audio data to the bit bucket. The <code>NullAudioPlayer</code>
 * is instrumented to provide timing metrics.
 */
public class NullAudioPlayer implements AudioPlayer {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(NullAudioPlayer.class.getName());

    private float volume = 1.0f;
    private AudioFormat audioFormat;
    private boolean firstSound = true;
    private int totalBytes = 0;
    private int totalWrites = 0;
    private BulkTimer timer = new BulkTimer();

    /**
     * Constructs a NullAudioPlayer
     */
    public NullAudioPlayer() {
    }

    /**
     * Sets the audio format for this player
     *
     * @param format the audio format
     */
    @Override
    public void setAudioFormat(AudioFormat format) {
        this.audioFormat = format;
    }

    /**
     * Retrieves the audio format for this player
     *
     * @return the current audio format.
     */
    @Override
    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    /**
     * Cancels all queued output. Current 'write' call will return
     * false
     */
    @Override
    public void cancel() {
    }

    /**
     * Pauses the audio output
     */
    @Override
    public void pause() {
    }

    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    @Override
    public void reset() {
        timer.start("AudioOutput");
    }

    /**
     * Resumes audio output
     */
    @Override
    public void resume() {
    }

    /**
     * Waits for all audio playback to stop, and closes this AudioPlayer.
     */
    @Override
    public void close() {
    }

    /**
     * Returns the current volume.
     *
     * @return the current volume (between 0 and 1)
     */
    @Override
    public float getVolume() {
        return volume;
    }

    /**
     * Sets the current volume.
     *
     * @param volume the current volume (between 0 and 1)
     */
    @Override
    public void setVolume(float volume) {
        this.volume = volume;
    }

    /**
     * Writes the given bytes to the audio stream
     *
     * @param audioData array of audio data
     * @return <code>true</code> of the write completed successfully,
     * <code> false </code>if the write was cancelled.
     */
    @Override
    public boolean write(byte[] audioData) {
        return write(audioData, 0, audioData.length);
    }

    /**
     * Starts the output of a set of data
     *
     * @param size the size of data between now and the end
     */
    @Override
    public void begin(int size) {
    }

    /**
     * Marks the end of a set of data
     */
    @Override
    public boolean end() {
        return true;
    }

    /**
     * Writes the given bytes to the audio stream
     *
     * @param bytes  audio data to write to the device
     * @param offset the offset into the buffer
     * @param size   the size into the buffer
     * @return <code>true</code> of the write completed successfully,
     * <code> false </code>if the write was cancelled.
     */
    @Override
    public boolean write(byte[] bytes, int offset, int size) {
        totalBytes += size;
        totalWrites++;
        if (firstSound) {
            timer.stop("AudioFirstSound");
            firstSound = false;
            if (logger.isLoggable(Level.TRACE)) {
                timer.show("Null Trace");
            }
        }
        if (logger.isLoggable(Level.TRACE)) {
            logger.log(Level.DEBUG, "NullAudio: write " + size + " bytes.");
        }
        return true;
    }

    /**
     * Starts the first sample timer
     */
    @Override
    public void startFirstSampleTimer() {
        firstSound = true;
        timer.start("AudioFirstSound");
    }

    /**
     * Waits for all queued audio to be played
     *
     * @return <code>true</code> if the audio played to completion,
     * <code> false </code>if the audio was stopped
     */
    @Override
    public boolean drain() {
        timer.stop("AudioOutput");
        return true;
    }

    /**
     * Gets the amount of played since the last resetTime
     * Currently not supported.
     *
     * @return the amount of audio in milliseconds
     */
    @Override
    public long getTime() {
        return -1L;
    }

    /**
     * Resets the audio clock
     */
    @Override
    public void resetTime() {
    }

    /**
     * Shows metrics for this audio player
     */
    @Override
    public void showMetrics() {
        timer.show("NullAudioPlayer");
    }
}
