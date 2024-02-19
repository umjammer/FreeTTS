/**
 * Copyright 2003 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.freetts.clientServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System.Logger.Level;
import java.net.Socket;
import java.lang.System.Logger;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import com.sun.speech.freetts.util.Utilities;
import demo.util.TTSServer;


/**
 * Implements a text-to-speech server for the Client/Server demo.
 * It creates two Voices when it starts up, one 8k and one 16k,
 * and then waits for socket connections.
 * After it receives a connection, it waits for TTS requests from the client,
 * does speech synthesis, and then sends the synthesized wave bytes back to
 * the client. For a complete specification of the protocol, please refer
 * to the document <code>Protocol.txt</code>.
 */
public class Server extends TTSServer {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(Server.class.getName());

    // 8k Voice
    private Voice voice8k;
    private String voice8kName = Utilities.getProperty("voice8kName", "kevin");

    // 16k Voice
    private Voice voice16k;
    private String voice16kName = Utilities.getProperty("voice16kName", "kevin16");

    /**
     * Constructs a default Server, which loads a 8k Voice and a 16k Voice
     * by default.
     */
    public Server() {
        port = Utilities.getInteger("port", 5555);
        try {
            VoiceManager voiceManager = VoiceManager.getInstance();
            voice8k = voiceManager.getVoice(voice8kName);
            voice16k = voiceManager.getVoice(voice16kName);
            voice8k.allocate();
            voice16k.allocate();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            System.exit(1);
        }
    }

    /**
     * Returns the 8k diphone voice.
     *
     * @return 8k diphone voice
     */
    public Voice get8kVoice() {
        return voice8k;
    }

    /**
     * Returns the 16k diphone voice.
     *
     * @return 16k diphone voice
     */
    public Voice get16kVoice() {
        return voice16k;
    }

    /**
     * Spawns a ProtocolHandler depending on the current protocol.
     *
     * @param socket the socket that the spawned protocol handler will use
     */
    @Override
    protected void spawnProtocolHandler(Socket socket) {
        try {
            SocketTTSHandler handler = new SocketTTSHandler(socket, this);
            (new Thread(handler)).start();
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    /**
     * Starts this TTS Server.
     */
    public static void main(String[] args) {
        Server server = new Server();
        (new Thread(server)).start();
    }
}


/**
 * A simple socket TTS request handler.
 */
class SocketTTSHandler implements Runnable {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(SocketTTSHandler.class.getName());

    /** the Voice to use to speak */
    private Voice voice;

    /** the Server to obtain Voices from */
    private Server server;

    /** the Socket to communicate with */
    private Socket socket;

    /** an AudioPlayer that writes bytes to the socket */
    private SocketAudioPlayer socketAudioPlayer;

    private BufferedReader reader;
    private PrintWriter writer;

    private static final int INVALID_SAMPLE_RATE = 1;

    // metrics variables
    private boolean metrics = Utilities.getBoolean("metrics");
    private long requestReceivedTime;
    private long requestSpeakTime;

    /**
     * Constructs a SocketTTSHandler with the given <code>Socket</code>
     * and <code>Server</code>.
     *
     * @param socket the Socket to read from and write to
     * @param server the Server to obtain Voices from
     */
    public SocketTTSHandler(Socket socket, Server server) {
        setSocket(socket);
        this.server = server;
        this.socketAudioPlayer = new SocketAudioPlayer(socket);
    }

    /**
     * Sets the Socket to be used by this ProtocolHandler.
     *
     * @param socket the Socket to be used
     */
    private void setSocket(Socket socket) {
        this.socket = socket;
        if (socket != null) {
            try {
                reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                writer = new PrintWriter(socket.getOutputStream(), true);
            } catch (IOException ioe) {
                logger.log(Level.ERROR, ioe.getMessage(), ioe);
                logger.log(Level.DEBUG, "Socket reader/writer not instantiated");
                throw new Error();
            }
        }
    }

    /**
     * Sends the given line of text over the Socket.
     *
     * @param line the line of text to send
     */
    private void sendLine(String line) {
        writer.print(line);
        writer.print('\n');
        writer.flush();
    }

    /**
     * Implements the run() method of Runnable
     */
    @Override
    public void run() {
        try {
            sendLine("READY");

            String command;
            int status;

            while ((command = reader.readLine()) != null && command.equals("TTS")) {

                requestReceivedTime = System.currentTimeMillis();

                status = handleSynthesisRequest();

                if (status == INVALID_SAMPLE_RATE) {
                    logger.log(Level.DEBUG, "Invalid sample rate\nexit.");
                    return;
                } else if (metrics) {
                    System.out.println("Time To Sending First Byte: " +
                                    (socketAudioPlayer.getFirstByteSentTime() - requestReceivedTime) + " ms");
                }
            }
            if (command != null) {
                if (command.equals("DONE")) {
                    socket.close();
                    logger.log(Level.DEBUG, "... closed socket connection");
                } else {
                    logger.log(Level.DEBUG, "invalid command: " + command);
                }
            }
        } catch (IOException ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
        }
    }

    /**
     * Handles a single speech synthesis request.
     */
    private int handleSynthesisRequest() {
        try {
            String sampleRateLine = reader.readLine();
            int sampleRate = Integer.parseInt(sampleRateLine);

            if (sampleRate == 8000) {
                voice = server.get8kVoice();
            } else if (sampleRate == 16000) {
                voice = server.get16kVoice();
            } else {
                // invalid sample rate
                sendLine("-2");
                return INVALID_SAMPLE_RATE;
            }

            String text = reader.readLine();

            voice.setAudioPlayer(socketAudioPlayer);
            voice.speak(text);

            // tell the client that there is no more data for this request
            sendLine("-1");

        } catch (IOException ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
        }
        return 0;
    }
}
