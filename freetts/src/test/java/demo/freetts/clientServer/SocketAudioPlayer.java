/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.freetts.clientServer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.lang.System.Logger;
import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.audio.AudioPlayer;


/**
 * Implements the AudioPlayer for the freetts Client/Server demo.
 * This SocketAudioPlayer basically sends synthesized wave bytes to the
 * client.
 */
public class SocketAudioPlayer implements AudioPlayer {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(SocketAudioPlayer.class.getName());

    private AudioFormat audioFormat;
    private Socket socket;
    private DataOutputStream dataOutputStream;
    private int bytesToPlay = 0;
    private int bytesPlayed = 0;
    private boolean firstByteSent = false;
    private long firstByteTime = -1;

    /**
     * Constructs a SocketAudioPlayer that will send wave bytes to the
     * given Socket.
     *
     * @param socket the Socket to which synthesized wave bytes will be sent
     */
    public SocketAudioPlayer(Socket socket) {
        this.socket = socket;
        try {
            this.dataOutputStream = new DataOutputStream(socket.getOutputStream());
        } catch (IOException ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
        }
    }

    /**
     * Sets the audio format to use for the next set of outputs. Since
     * an audio player can be shared by a number of voices, and since
     * voices can have different AudioFormats (sample rates for
     * example), it is necessary to allow clients to dynamically set
     * the audio format for the player.
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
     * @return the current audio format
     */
    @Override
    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    /**
     * Pauses all audio output on this player. Play can be resumed
     * with a call to resume. Not implemented in this Player.
     */
    @Override
    public void pause() {
    }

    /**
     * Resumes audio output on this player. Not implemented in this Player.
     */
    @Override
    public void resume() {
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
     * Flushes all the audio data to the Socket.
     *
     * @return <code>true</code> all the time
     */
    @Override
    public boolean drain() {
        try {
            dataOutputStream.flush();
        } catch (IOException ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
        }
        return true;
    }

    /**
     * Starts the output of a set of data. Audio data for a single
     * utterance should be grouped between begin/end pairs.
     *
     * @param size the size of data in bytes to be output before
     *             <code>end</code> is called.
     */
    @Override
    public void begin(int size) {
        try {
            bytesToPlay = size;
            firstByteSent = false;
            dataOutputStream.writeBytes(size + "\n");
            dataOutputStream.flush();
            logger.log(Level.DEBUG, "begin: " + size);
        } catch (IOException ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
        }
    }

    /**
     * Starts the first sample timer (none in this player)
     */
    @Override
    public void startFirstSampleTimer() {
    }

    /**
     * Signals the end of a set of data. Audio data for a single
     * utterance should be grouped between <code> begin/end </code> pairs.
     *
     * @return <code>true</code> if the audio was output properly,
     * <code> false</code> if the output was cancelled
     * or interrupted.
     */
    @Override
    public boolean end() {
        logger.log(Level.DEBUG, "end");
        if (bytesPlayed < bytesToPlay) {
            int bytesNotPlayed = bytesToPlay - bytesPlayed;
            write(new byte[bytesNotPlayed], 0, bytesNotPlayed);
        }

        bytesToPlay = 0;
        bytesPlayed = 0;
        return true;
    }

    /**
     * Cancels all queued output. All 'write' calls until the next
     * reset will return false. Not implemented in this Player.
     */
    @Override
    public void cancel() {
    }

    /**
     * Waits for all audio playback to stop, and closes this AudioPlayer.
     * Not implemented in this Player.
     */
    @Override
    public void close() {
    }

    /**
     * Returns the current volume. The volume is specified as a number
     * between 0.0 and 1.0, where 1.0 is the maximum volume and 0.0 is
     * the minimum volume. Not implemented in this Player.
     *
     * @return the current volume (between 0 and 1)
     */
    @Override
    public float getVolume() {
        return -1;
    }

    /**
     * Sets the current volume. The volume is specified as a number
     * between 0.0 and 1.0, where 1.0 is the maximum volume and 0.0 is
     * the minimum volume. Not implemented in this Player.
     *
     * @param volume the new volume (between 0 and 1)
     */
    @Override
    public void setVolume(float volume) {
    }

    /**
     * Gets the amount of audio played since the last resetTime.
     * Not implemented in this Player.
     *
     * @return the amount of audio in milliseconds
     */
    @Override
    public long getTime() {
        return -1;
    }

    /**
     * Resets the audio clock. Not implemented in this Player.
     */
    @Override
    public void resetTime() {
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
     * @param audioData audio data to write to the device
     * @param offset    the offset into the buffer
     * @param size      the number of bytes to write.
     * @return <code>true</code> of the write completed successfully,
     * <code> false </code>if the write was cancelled.
     */
    @Override
    public boolean write(byte[] audioData, int offset, int size) {
        try {
            if (!firstByteSent) {
                firstByteTime = System.currentTimeMillis();
                firstByteSent = true;
            }

            bytesPlayed += size;
            dataOutputStream.write(audioData, offset, size);
            dataOutputStream.flush();

            logger.log(Level.DEBUG, "sent " + size + " bytes " + audioData[0] + " " + audioData[size / 2]);
            return true;
        } catch (IOException ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
            return false;
        }
    }

    /**
     * Shows metrics for this audio player. Not implemented in this Player.
     */
    @Override
    public void showMetrics() {
    }

    /**
     * Returns the first byte sent time in milliseconds, the last time it
     * was recorded.
     *
     * @return the last first byte sent time in milliseconds
     */
    public long getFirstByteSentTime() {
        return firstByteTime;
    }
}
