/**
 * Copyright 2003 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.freetts.emacspeak;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import demo.util.TTSServer;


/**
 * Provides text-to-speech server for Emacspeak.
 */
public class FreeTTSEmacspeakServer extends TTSServer {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(FreeTTSEmacspeakServer.class.getName());

    private Voice emacsVoice;

    /**
     * Constructs a EmacspeakServer.
     */
    public FreeTTSEmacspeakServer(String voiceName) {
        System.setProperty("com.sun.speech.freetts.audio.AudioPlayer.cancelDelay", "0");
        createVoice(voiceName);
    }

    /**
     * Creates and loads the Voice.
     */
    private void createVoice(String voiceName) {
        VoiceManager voiceManager = VoiceManager.getInstance();
        emacsVoice = voiceManager.getVoice(voiceName);
        if (emacsVoice == null) {
            logger.log(Level.INFO, "No such voice with the name: " + voiceName);
            System.exit(1);
        }
        emacsVoice.allocate();
    }

    /**
     * Spawns a ProtocolHandler depending on the current protocol.
     * This method is inherited from TTSServer.
     *
     * @param socket the socket that the spawned protocol handler will use
     */
    @Override
    protected void spawnProtocolHandler(Socket socket) {
        try {
            FreeTTSEmacspeakHandler handler =
                    new FreeTTSEmacspeakHandler(socket, emacsVoice);
            (new Thread(handler)).start();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    /**
     * Sets the speaking rate of the voice.
     *
     * @param wpm the speaking rate (words per minute)
     */
    public void setRate(float wpm) {
        emacsVoice.setRate(wpm);
    }

    /**
     * Starts this TTS Server.
     * <p>
     * Usage: FreeTTSEmacspeakServer [voicename [speaking rate]]
     */
    public static void main(String[] args) {
        String voiceName = (args.length > 0) ? args[0] : "kevin16";

        System.out.println();
        System.out.println("Using voice: " + voiceName);
        System.out.println();

        FreeTTSEmacspeakServer server = new FreeTTSEmacspeakServer(voiceName);

        if (args.length > 1) {
            float wpm = Float.parseFloat(args[1]);
            server.setRate(wpm);
        }

        (new Thread(server)).start();
    }
}
