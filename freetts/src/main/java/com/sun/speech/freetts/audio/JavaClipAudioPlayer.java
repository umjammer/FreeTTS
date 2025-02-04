/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts.audio;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import com.sun.speech.freetts.util.BulkTimer;
import com.sun.speech.freetts.util.Timer;
import com.sun.speech.freetts.util.Utilities;


/**
 * Provides an implementation of <code>AudioPlayer</code> that creates
 * javax.sound.sampled audio clips and outputs them via the
 * javax.sound API.  The interface provides a highly reliable audio
 * output package. Since audio is batched and not sent to the audio
 * layer until an entire utterance has been processed, this player has
 * higher latency (50 msecs for a typical 4 second utterance).
 */
public class JavaClipAudioPlayer implements AudioPlayer {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(JavaClipAudioPlayer.class.getName());

    private volatile boolean paused;
    private volatile boolean cancelled = false;
    private volatile Clip currentClip;

    /** The current volume. */
    private float volume = 1.0f;
    private boolean audioMetrics;
    private final BulkTimer timer = new BulkTimer();
    /** Default format is 8kHz. */
    private AudioFormat defaultFormat =
            new AudioFormat(8000f, 16, 1, true, true);
    private AudioFormat currentFormat = defaultFormat;
    private boolean firstSample = true;
    private boolean firstPlay = true;
    private int curIndex = 0;
    /** Data buffer to write the pure audio data to. */
    private final PipedOutputStream outputData;
    /** Audio input stream that is used to play back the audio. */
    private AudioInputStream audioInput;
    private final LineListener lineListener;

    private long drainDelay;
    private long openFailDelayMs;
    private long totalOpenFailDelayMs;

    /**
     * Constructs a default JavaClipAudioPlayer
     */
    public JavaClipAudioPlayer() {
        drainDelay = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.drainDelay",
                150L);
        openFailDelayMs = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.openFailDelayMs",
                0);
        totalOpenFailDelayMs = Utilities.getLong("com.sun.speech.freetts.audio.AudioPlayer.totalOpenFailDelayMs",
                0);
        audioMetrics = Utilities.getBoolean("com.sun.speech.freetts.audio.AudioPlayer.showAudioMetrics");
        setPaused(false);
        outputData = new PipedOutputStream();
        lineListener = new JavaClipLineListener();
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
        if (currentFormat.matches(format)) {
            return;
        }
        currentFormat = format;
        // Force the clip to be recreated if the format changed.
        if (currentClip != null) {
            currentClip = null;
        }
    }

    /**
     * Retrieves the audio format for this player
     *
     * @return format the audio format
     */
    @Override
    public AudioFormat getAudioFormat() {
        return currentFormat;
    }

    /**
     * Pauses audio output.   All audio output is
     * stopped. Output can be resumed at the
     * current point by calling <code>resume</code>. Output can be
     * aborted by calling <code> cancel </code>
     */
    @Override
    public void pause() {
        if (!paused) {
            setPaused(true);
            if (currentClip != null) {
                currentClip.stop();
            }
            synchronized (this) {
                notifyAll();
            }
        }
    }

    /**
     * Resumes playing audio after a pause.
     */
    @Override
    public synchronized void resume() {
        if (paused) {
            setPaused(false);
            if (currentClip != null) {
                currentClip.start();
            }
            notifyAll();
        }
    }

    /**
     * Cancels all queued audio. Any 'write' in process will return
     * immediately false.
     */
    @Override
    public void cancel() {
        if (audioMetrics) {
            timer.start("audioCancel");
        }
        if (currentClip != null) {
            currentClip.stop();
            currentClip.close();
        }
        synchronized (this) {
            cancelled = true;
            paused = false;
            notifyAll();
        }
        if (audioMetrics) {
            timer.stop("audioCancel");
            Timer.showTimesShortTitle("");
            timer.getTimer("audioCancel").showTimesShort(0);
        }
    }

    /**
     * Prepares for another batch of output. Larger groups of output
     * (such as all output associated with a single FreeTTSSpeakable)
     * should be grouped between a reset/drain pair.
     */
    @Override
    public synchronized void reset() {
        timer.start("speakableOut");
    }

    /**
     * Waits for all queued audio to be played
     *
     * @return <code>true</code> if the write completed successfully,
     * <code> false </code>if the write was cancelled.
     */
    @Override
    public boolean drain() {
        timer.stop("speakableOut");
        return true;
    }

    /**
     * Closes this audio player
     * <p>
     * [[[ WORKAROUND TODO
     * The javax.sound.sampled drain is almost working properly.  On
     * linux, there is still a little bit of sound that needs to go
     * out, even after drain is called. Thus, the drainDelay. We
     * wait for a few hundred milliseconds while the data is really
     * drained out of the system
     * ]]]
     */
    @Override
    public synchronized void close() {
        if (currentClip != null) {
            currentClip.drain();
            if (drainDelay > 0L) {
                try {
                    Thread.sleep(drainDelay);
                } catch (InterruptedException e) {
                }
            }
            currentClip.close();
        }
        notifyAll();
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
        if (volume > 1.0f) {
            volume = 1.0f;
        }
        if (volume < 0.0f) {
            volume = 0.0f;
        }
        this.volume = volume;
        if (currentClip != null) {
            setVolume(currentClip, volume);
        }
    }

    /**
     * Sets pause mode
     *
     * @param state true if we are paused
     */
    private void setPaused(boolean state) {
        paused = state;
    }

    /**
     * Sets the volume on the given clip
     *
     * @param clip the line to set the volume on
     * @param vol  the volume (range 0 to 1)
     */
    private static void setVolume(Clip clip, float vol) {
        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float range = volumeControl.getMaximum() - volumeControl.getMinimum();
            volumeControl.setValue(vol * range + volumeControl.getMinimum());
        }
    }

    /**
     * Returns the current position in the output stream since the
     * last <code>resetTime</code>
     * <p>
     * Currently not supported.
     *
     * @return the position in the audio stream in milliseconds
     */
    @Override
    public synchronized long getTime() {
        return -1L;
    }

    /**
     * Resets the time for this audio stream to zero
     */
    @Override
    public synchronized void resetTime() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void begin(int size) throws IOException {
        timer.start("utteranceOutput");
        cancelled = false;
        curIndex = 0;
        PipedInputStream in;
        try {
            in = new PipedInputStream(outputData);
            audioInput = new AudioInputStream(in, currentFormat, size);
        } catch (IOException e) {
            logger.log(Level.WARNING, e.getLocalizedMessage());
        }
        while (paused && !cancelled) {
            try {
                wait();
            } catch (InterruptedException ie) {
                return;
            }
        }

        timer.start("clipGeneration");

        boolean opened = false;
        long totalDelayMs = 0;
        do {
            // keep trying to open the clip until the specified
            // delay is exceeded
            try {
                currentClip = getClip();
                currentClip.open(audioInput);
                opened = true;
            } catch (LineUnavailableException lue) {
                logger.log(Level.INFO, "LINE UNAVAILABLE: " + "Format is " + currentFormat);
                try {
                    Thread.sleep(openFailDelayMs);
                    totalDelayMs += openFailDelayMs;
                } catch (InterruptedException ie) {
                    return;
                }
            }
        } while (!opened && totalDelayMs < totalOpenFailDelayMs);

        if (!opened) {
            close();
        } else {
            setVolume(currentClip, volume);
            if (audioMetrics && firstPlay) {
                firstPlay = false;
                timer.stop("firstPlay");
                timer.getTimer("firstPlay");
                Timer.showTimesShortTitle("");
                timer.getTimer("firstPlay").showTimesShort(0);
            }
            currentClip.start();
        }
    }

    /**
     * Lazy instantiation of the clip.
     *
     * @return the clip to use.
     * @throws LineUnavailableException if the target line is not available.
     */
    private Clip getClip() throws LineUnavailableException {
        if (currentClip == null) {
            if (logger.isLoggable(Level.DEBUG)) {
                logger.log(Level.DEBUG, "creating new clip");
            }
            DataLine.Info info = new DataLine.Info(Clip.class, currentFormat);
            try {
                currentClip = (Clip) AudioSystem.getLine(info);
                currentClip.addLineListener(lineListener);
            } catch (SecurityException | IllegalArgumentException e) {
                throw new LineUnavailableException(e.getLocalizedMessage());
            }
        }
        return currentClip;
    }

    /**
     * Marks the end a set of data. Audio data for a single utterance should be
     * grouped between begin/end pairs.
     *
     * @return <code>true</code> if the audio was output properly,
     * <code>false </code> if the output was canceled or interrupted.
     */
    @Override
    public synchronized boolean end() {
        boolean ok = true;

        if (cancelled) {
            return false;
        }

        if ((currentClip == null) || !currentClip.isOpen()) {
            close();
            ok = false;
        } else {
            setVolume(currentClip, volume);
            if (audioMetrics && firstPlay) {
                firstPlay = false;
                timer.stop("firstPlay");
                timer.getTimer("firstPlay");
                Timer.showTimesShortTitle("");
                timer.getTimer("firstPlay").showTimesShort(0);
            }
            try {
                // wait for audio to complete
                while (currentClip != null &&
                        (currentClip.isRunning() || paused) && !cancelled) {
                    wait();
                }
            } catch (InterruptedException ie) {
                ok = false;
            }
            close();
        }

        timer.stop("clipGeneration");
        timer.stop("utteranceOutput");
        ok &= !cancelled;
        return ok;
    }

    @Override
    public boolean write(byte[] audioData) throws IOException {
        return write(audioData, 0, audioData.length);
    }

    @Override
    public boolean write(byte[] bytes, int offset, int size) throws IOException {
        if (firstSample) {
            firstSample = false;
            timer.stop("firstAudio");
            if (audioMetrics) {
                Timer.showTimesShortTitle("");
                timer.getTimer("firstAudio").showTimesShort(0);
            }
        }
        outputData.write(bytes, offset, size);
        curIndex += size;
        return true;
    }

    /**
     * Returns the name of this audio player
     *
     * @return the name of the audio player
     */
    public String toString() {
        return "JavaClipAudioPlayer";
    }

    /**
     * Shows metrics for this audio player
     */
    @Override
    public void showMetrics() {
        timer.show(toString());
    }

    /**
     * Starts the first sample timer
     */
    @Override
    public void startFirstSampleTimer() {
        timer.start("firstAudio");
        firstSample = true;
        if (audioMetrics) {
            timer.start("firstPlay");
            firstPlay = true;
        }
    }

    /**
     * Provides a LineListener for this clas.
     */
    private class JavaClipLineListener implements LineListener {

        /**
         * Implements update() method of LineListener interface. Responds to the
         * line events as appropriate.
         *
         * @param event the LineEvent to handle
         */
        @Override
        public void update(LineEvent event) {
            if (event.getType().equals(LineEvent.Type.START)) {
                if (logger.isLoggable(Level.DEBUG)) {
                    logger.log(Level.DEBUG, this + ": EVENT START");
                }
            } else if (event.getType().equals(LineEvent.Type.STOP)) {
                if (logger.isLoggable(Level.DEBUG)) {
                    logger.log(Level.DEBUG, this + ": EVENT STOP");
                }
                synchronized (JavaClipAudioPlayer.this) {
                    JavaClipAudioPlayer.this.notifyAll();
                }
            } else if (event.getType().equals(LineEvent.Type.OPEN)) {
                if (logger.isLoggable(Level.DEBUG)) {
                    logger.log(Level.DEBUG, this + ": EVENT OPEN");
                }
            } else if (event.getType().equals(LineEvent.Type.CLOSE)) {
                // When a clip is closed we no longer need it, so
                // set currentClip to null and notify anyone who may
                // be waiting on it.
                if (logger.isLoggable(Level.DEBUG)) {
                    logger.log(Level.DEBUG, this + ": EVENT CLOSE");
                }
                synchronized (JavaClipAudioPlayer.this) {
                    JavaClipAudioPlayer.this.notifyAll();
                }
            }
        }
    }
}
