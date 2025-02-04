/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts.audio;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.sun.speech.freetts.util.Utilities;


/**
 * Streams audio to multiple files as 8-bit samples, one per utterance.
 * Currently, FreeTTS always outputs 16-bit samples, and this
 * MultiFile8BitAudioPlayer will convert them to 8-bit before outputting
 * them.
 */
public class MultiFile8BitAudioPlayer implements AudioPlayer {

    // 8-bit unsigned little-endian mono audio
    private AudioFormat currentFormat = new AudioFormat
            (8000, 8, 1, false, false);

    private int fileCount = 0;
    private String baseName;
    private byte[] outputData;
    private int curIndex = 0;
    private AudioFileFormat.Type outputType;

    /**
     * Creates a default audio player for an AudioFileFormat of type
     * WAVE.  Reads the "com.sun.speech.freetts.AudioPlayer.baseName"
     * property for the base filename to use, and will produce files
     * of the form &lt;baseName>1.wav.  The default value for the
     * base name is "freetts".
     */
    public MultiFile8BitAudioPlayer() {
        this(Utilities.getProperty("com.sun.speech.freetts.AudioPlayer.baseName", "freetts"),
                AudioFileFormat.Type.WAVE);
    }

    /**
     * Constructs a MultiFile8BitAudioPlayer
     *
     * @param baseName the base name of the audio file
     * @param type     the type of audio output
     */
    public MultiFile8BitAudioPlayer(String baseName, AudioFileFormat.Type type) {
        this.baseName = baseName;
        this.outputType = type;
    }

    /**
     * Sets the audio format for this player
     *
     * @param format the audio format
     * @throws UnsupportedOperationException if the line cannot be opened with
     *                                       the given format
     */
    @Override
    public synchronized void setAudioFormat(AudioFormat format) {
    }

    /**
     * Gets the audio format for this player
     *
     * @return format the audio format
     */
    @Override
    public AudioFormat getAudioFormat() {
        return currentFormat;
    }

    /**
     * Pauses audio output
     */
    @Override
    public void pause() {
    }

    /**
     * Resumes audio output
     */
    @Override
    public synchronized void resume() {
    }

    /**
     * Starts the first sample timer
     */
    @Override
    public void startFirstSampleTimer() {
    }

    /**
     * Cancels currently playing audio
     */
    @Override
    public synchronized void cancel() {
    }

    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    @Override
    public synchronized void reset() {
    }

    /**
     * Closes this audio player
     */
    @Override
    public synchronized void close() {
    }

    /**
     * Returns the current volume.
     *
     * @return the current volume (between 0 and 1)
     */
    @Override
    public float getVolume() {
        return 1.0f;
    }

    /**
     * Sets the current volume.
     *
     * @param volume the current volume (between 0 and 1)
     */
    @Override
    public void setVolume(float volume) {
    }

    /**
     * Starts the output of a set of data. Audio data for a single
     * utterance should be grouped between begin/end pairs.
     *
     * @param size the size of data between now and the end
     */
    @Override
    public void begin(int size) {
        outputData = new byte[size / 2];
        curIndex = 0;
    }

    @Override
    public boolean end() throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(outputData);
        AudioInputStream ais = new AudioInputStream(bais, currentFormat,
                outputData.length / currentFormat.getFrameSize());
        String name = baseName;
        name = name + fileCount;
        name = name + "." + outputType.getExtension();
        File file = new File(name);
        try {
            AudioSystem.write(ais, outputType, file);
            System.out.println("Wrote synthesized speech to " + name);
        } catch (IllegalArgumentException iae) {
            throw new IOException("Can't write audio type " + outputType, iae);
        }
        fileCount++;
        return true;
    }

    /**
     * Waits for all queued audio to be played
     *
     * @return true if the audio played to completion, false if
     * the audio was stopped
     */
    @Override
    public boolean drain() {
        return true;
    }

    /**
     * Gets the amount of played since the last mark
     *
     * @return the amount of audio in milliseconds
     */
    @Override
    public synchronized long getTime() {
        return -1L;
    }

    /**
     * Resets the audio clock
     */
    @Override
    public synchronized void resetTime() {
    }

    /**
     * Writes the given bytes to the audio stream
     *
     * @param audioData audio data to write to the device
     * @return <code>true</code> of the write completed successfully,
     * <code> false </code>if the write was cancelled.
     */
    @Override
    public boolean write(byte[] audioData) {
        return write(audioData, 0, audioData.length);
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
        bytes = convert16To8Bits(bytes);
        size /= 2;
        System.arraycopy(bytes, offset, outputData, curIndex, size);
        curIndex += size;
        return true;
    }

    /**
     * Converts an array of signed 16-bit audio data to unsigned 8-bit
     * audio data.
     *
     * @param samples16Bit the signed 16-bit audio data to convert
     * @return unsigned 8-bit audio data
     */
    private static byte[] convert16To8Bits(byte[] samples16Bit) {
        byte[] samples8Bit = new byte[samples16Bit.length / 2];
        for (int i = 0, j = 0; i < samples16Bit.length; i += 2, j++) {
            int sample = (0x000000FF & samples16Bit[i]);
            samples8Bit[j] = (byte) (sample + 128);
        }
        return samples8Bit;
    }

    /**
     * Returns the name of this audioplayer
     *
     * @return the name of the audio player
     */
    public String toString() {
        return "MultiFile8BitAudioPlayer";
    }

    /**
     * Shows metrics for this audio player
     */
    @Override
    public void showMetrics() {
    }
}
