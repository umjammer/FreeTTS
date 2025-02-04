/*
 * RTP demo for FreeTTS.
 *
 * Copyright (C) 2007 JVoiceXML group - http://jvoicexml.sourceforge.net
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 *
 */

package org.jvoicexml.rtp.freetts;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.media.protocol.PullSourceStream;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import com.sun.speech.freetts.audio.AudioPlayer;


/**
 * FreeTTS {@link AudioPlayer} for the RTP protocol.
 *
 * @author Dirk Schnelle
 */
public final class RtpAudioPlayer implements AudioPlayer {

    /** The audio format to use. */
    private AudioFormat currentFormat;

    /** Type of the audio format to send over RTP. */
    private AudioFileFormat.Type outputType;

    /** RTP source stream to send the data. */
    private FreeTTSPullSourceStream stream;

    /** Buffer to capture the FreeTTS output. */
    private ByteArrayOutputStream out;

    /** Number of received bytes. */
    private int numBytes = 0;

    /**
     * Constructs a new object.
     */
    public RtpAudioPlayer(FreeTTSDataSource ds) {
        PullSourceStream[] streams = ds.getStreams();
        stream = (FreeTTSPullSourceStream) streams[0];
        outputType = AudioFileFormat.Type.WAVE;
    }

    @Override
    public void begin(int num) {
        out = new ByteArrayOutputStream();
    }

    @Override
    public void cancel() {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean drain() {
        return true;
    }

    @Override
    public boolean end() {
        // This algorithm is not very efficient. Needs some cleanup.
        byte[] bytes = out.toByteArray();
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);

        AudioInputStream ais = new AudioInputStream(in,
                currentFormat, numBytes / currentFormat.getFrameSize());

        out = new ByteArrayOutputStream();
        try {
            AudioSystem.write(ais, outputType, out);
        } catch (IOException e) {
            return false;
        }
        byte[] waveBytes = out.toByteArray();
        in = new ByteArrayInputStream(waveBytes);
        stream.setInstream(in);

        return true;
    }

    @Override
    public AudioFormat getAudioFormat() {
        return currentFormat;
    }

    @Override
    public long getTime() {
        return 0;
    }

    @Override
    public float getVolume() {
        return 0;
    }

    @Override
    public void pause() {
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetTime() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void setAudioFormat(AudioFormat format) {
        currentFormat = format;
    }

    @Override
    public void setVolume(float level) {
    }

    @Override
    public void showMetrics() {
    }

    @Override
    public void startFirstSampleTimer() {
    }

    @Override
    public boolean write(byte[] bytes) {
        write(bytes, 0, bytes.length);
        return true;
    }

    @Override
    public boolean write(byte[] bytes, int offset, int length) {
        out.write(bytes, offset, length);
        numBytes += length;
        return true;
    }
}
