/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts.audio;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.util.Utilities;


/**
 * Provides an implementation of <code>AudioPlayer</code> that sends
 * all audio data to the given file.
 */
public class RawFileAudioPlayer implements AudioPlayer {

    private AudioFormat audioFormat;
    private float volume;
    private BufferedOutputStream os;
    private String path;

    /**
     * Creates a default audio player for an AudioFileFormat of type
     * WAVE.  Reads the "com.sun.speech.freetts.AudioPlayer.baseName"
     * property for the base filename to use, and will produce files
     * of the form &lt;baseName>.raw.  The default value for the
     * base name is "freetts".
     */
    public RawFileAudioPlayer() throws IOException {
        this(Utilities.getProperty("com.sun.speech.freetts.AudioPlayer.baseName", "freetts")
                + ".raw");
    }

    /**
     * Constructs a NullAudioPlayer
     */
    public RawFileAudioPlayer(String path) throws IOException {
        this.path = path;
        os = new BufferedOutputStream(Files.newOutputStream(Paths.get(path)));
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
    public void close() throws IOException {
        os.flush();
        os.close();
        System.out.println("Wrote synthesized speech to " + path);
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

    @Override
    public boolean write(byte[] audioData) throws IOException {
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

    @Override
    public boolean write(byte[] bytes, int offset, int size) throws IOException {
        os.write(bytes, offset, size);
        return true;
    }

    /**
     * Starts the first sample timer
     */
    @Override
    public void startFirstSampleTimer() {
    }

    /**
     * Waits for all queued audio to be played
     *
     * @return <code>true</code> if the audio played to completion,
     * <code> false </code>if the audio was stopped
     */
    @Override
    public boolean drain() {
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
    }
}
