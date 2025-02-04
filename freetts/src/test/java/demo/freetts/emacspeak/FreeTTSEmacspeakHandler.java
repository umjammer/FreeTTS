/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.freetts.emacspeak;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.util.Vector;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.util.Utilities;
import demo.util.EmacspeakProtocolHandler;

import static java.lang.System.getLogger;


/**
 * Implements a simplified version of the Emacspeak speech server.
 */
public class FreeTTSEmacspeakHandler extends EmacspeakProtocolHandler {

    private static final Logger logger = getLogger(FreeTTSEmacspeakHandler.class.getName());

    private SpeakCommandHandler speakCommandHandler;

    /**
     * Constructs an Emacspeak ProtocolHandler
     *
     * @param voice  the FreeTTS that this FreeTTSEmacspeakHandler belongs
     * @param socket the Socket that holds the TCP connection
     */
    public FreeTTSEmacspeakHandler(Socket socket, Voice voice) {
        setSocket(socket);
        this.speakCommandHandler = new SpeakCommandHandler(voice);
        this.speakCommandHandler.start();
        setDebug(Utilities.getBoolean("debug"));
    }

    /**
     * Speaks the given input text.
     *
     * @param input the input text to speak.
     */
    @Override
    public void speak(String input) {
        // split around "[*]"
        String[] parts = input.split(PARENS_STAR_REGEX);
        for (String part : parts) {
            speakCommandHandler.add(part);
        }
    }

    /**
     * Removes all the queued text.
     */
    @Override
    public void cancelAll() {
        speakCommandHandler.removeAll();
    }

    /**
     * Sets the speaking rate.
     *
     * @param wpm the new speaking rate (words per minute)
     */
    @Override
    public void setRate(float wpm) {
        speakCommandHandler.setRate(wpm);
    }

    /**
     * This thread is used to separate the handling of Voice.speak() from
     * the thread that accepts commands from the client, so that the
     * latter won't be blocked by the former.
     */
    class SpeakCommandHandler extends Thread {

        private Voice voice;
        private boolean done = false;
        private final Vector<String> commandList = new Vector<>();

        /**
         * Constructs a default SpeakCommandHandler object.
         *
         * @param voice the Voice object use to speak
         */
        public SpeakCommandHandler(Voice voice) {
            this.voice = voice;
        }

        /**
         * Implements the run() method of the Thread class.
         */
        @Override
        public void run() {
            while (!getSocket().isClosed() || !commandList.isEmpty()) {
                String firstCommand = null;
                synchronized (commandList) {
                    while (commandList.isEmpty() &&
                            !getSocket().isClosed()) {
                        try {
                            commandList.wait();
                        } catch (InterruptedException ie) {
                            logger.log(Level.ERROR, ie.getMessage(), ie);
                        }
                    }
                    if (!commandList.isEmpty()) {
                        firstCommand = commandList.remove(0);
                    }
                }
                if (firstCommand != null) {
                    voice.speak(firstCommand);
                    debugPrintln("SPEAK: \"" + firstCommand + "\"");
                }
            }
            debugPrintln("SpeakCommandHandler: thread terminated");
        }

        /**
         * Adds the given command to this Handler.
         *
         * @param command the text to be spoken
         */
        public void add(String command) {
            synchronized (commandList) {
                commandList.add(command);
                commandList.notifyAll();
            }
        }

        /**
         * Removes all the commands from this Handler.
         */
        public void removeAll() {
            synchronized (commandList) {
                voice.getAudioPlayer().cancel();
                commandList.removeAllElements();
            }
        }

        /**
         * Sets the speaking rate.
         *
         * @param wpm the new speaking rate (words per minute)
         */
        public void setRate(float wpm) {
            voice.setRate(wpm);
        }

        /**
         * Terminates this SpeakCommandHandler thread.
         *
         * @param done true to terminate this thread
         */
        public synchronized void setDone(boolean done) {
            this.done = done;
        }
    }
}
