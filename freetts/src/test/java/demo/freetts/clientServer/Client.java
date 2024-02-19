/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.freetts.clientServer;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.Socket;
import javax.sound.sampled.AudioFormat;

import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaStreamingAudioPlayer;
import com.sun.speech.freetts.util.Utilities;


/**
 * Implements a Java Client for the Client/Server demo. For details about
 * the protocol between client and server, consult the file
 * <code>Protocol.txt</code>.
 */
public class Client {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(Client.class.getName());

    private String serverAddress = Utilities.getProperty("server", "localhost");
    private int serverPort = Utilities.getInteger("port", 5555);

    private static final int AUDIO_BUFFER_SIZE = 256;

    private BufferedReader systemInReader;  // for reading user input text
    private BufferedReader reader;
    private DataInputStream dataReader;     // for reading raw bytes
    private PrintWriter writer;
    private AudioPlayer audioPlayer;
    private int sampleRate = Utilities.getInteger("sampleRate", 16000);
    private int sampleSize = 16;            // in bits
    private byte[] socketBuffer = new byte[AUDIO_BUFFER_SIZE];

    private boolean metrics = Boolean.getBoolean("metrics");
    private long sendTime;             // time the text is sent to server
    private long receiveTime;          // time the first byte is received
    private long firstSoundTime;       // time the first play to audio
    private boolean firstByteReceived = false;

    private static final String FIRST_SENTENCE = "Type in what you want me to say.";

    /**
     * Constructs a default Client. It connects to the speech server, and
     * constructs an AudioPlayer.
     */
    public Client() {
        if (!connect()) {
            System.out.println("Error connecting to " + serverAddress + " at " + serverPort);
            System.exit(1);
        }
        this.audioPlayer = new JavaStreamingAudioPlayer();
        this.audioPlayer.setAudioFormat(new AudioFormat(sampleRate, sampleSize, 1, true, true));
    }

    /**
     * Connects this client to the server.
     *
     * @return <code>true</code>  if successfully connected
     * <code>false</code>  if failed to connect
     */
    private boolean connect() {
        Socket socket = null;
        try {
            socket = new Socket(serverAddress, serverPort);
            dataReader = new DataInputStream(socket.getInputStream());
            systemInReader = new BufferedReader(new InputStreamReader(System.in));
            writer = new PrintWriter(socket.getOutputStream(), true);
            return true;
        } catch (IOException ioe) {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
            return false;
        }
    }

    /**
     * Reads a line of text from the Socket.
     *
     * @return a line of text without the end of line character
     */
    private String readLine() throws IOException {
        StringBuilder buffer = new StringBuilder();

        char c;
        while ((c = (char) dataReader.readByte()) != '\n') {
            logger.log(Level.TRACE, String.valueOf(c));
            buffer.append(c);
        }

        int lastCharIndex = buffer.length() - 1;

        // remove trailing ^M for Windows-based machines
        byte lastByte = (byte) buffer.charAt(lastCharIndex);
        if (lastByte == 13) {
            return buffer.substring(0, lastCharIndex);
        } else {
            return buffer.toString();
        }
    }

    /**
     * Sends the given line of text to the Socket, appending an end of
     * line character to the end.
     *
     * @param line the line of text to send
     */
    private void sendLine(String line) {
        logger.log(Level.DEBUG, line);
        line = line.trim();
        if (!line.isEmpty()) {
            writer.print(line);
            writer.print('\n');
            writer.flush();
        }
    }

    /**
     * Run the TTS protocol.
     */
    public void runTTSProtocol() {
        try {
            String readyLine = readLine();
            if (readyLine.equals("READY")) {
                if (!sendTTSRequest(FIRST_SENTENCE)) {
                    return;
                }
                System.out.print("Say       : ");
                String input;
                while ((input = systemInReader.readLine()) != null) {
                    if (!input.isEmpty() && !sendTTSRequest(input)) {
                        return;
                    }
                    System.out.print("Say       : ");
                }
            }
            sendLine("DONE");

            audioPlayer.drain();
            audioPlayer.close();

            logger.log(Level.DEBUG, "ALL DONE");

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    /**
     * Sends a TTS request on the given text.
     *
     * @param text the text to do TTS on
     * @return <code>true</code> if the TTS transaction was successful
     * <code>false</code> if an error occurred
     */
    private boolean sendTTSRequest(String text) {

        if (metrics) {
            sendTime = System.currentTimeMillis();
            firstByteReceived = false;
        }

        // send TTS request to server
        sendLine("TTS\n" + sampleRate + "\n" + text + "\n");

        // get response
        String numberSamplesStr;
        int numberSamples = 0;

        do {
            try {
                numberSamplesStr = readLine();
                numberSamples = Integer.parseInt(numberSamplesStr);

                if (numberSamples == -2) { // error
                    System.err.println("Client.sendTTSRequest(): error!");
                    return false;
                }
                if (numberSamples > 0) {
                    logger.log(Level.DEBUG, "Receiving : " + numberSamples + " samples");
                    receiveAndPlay(numberSamples);
                }
            } catch (IOException ioe) {
                logger.log(Level.ERROR, ioe.getMessage(), ioe);
            }
        }
        while (numberSamples > 0);

        if (metrics) {
            logger.log(Level.DEBUG, "FirstByte : " + (receiveTime - sendTime) + " ms");
        }

        return true;
    }

    /**
     * Reads the given number of bytes from the socket, and plays them
     * with the AudioPlayer.
     *
     * @param numberSamples the number of bytes to read from the socket
     */
    private void receiveAndPlay(int numberSamples) throws IOException {

        int bytesToRead;
        int bytesRemaining;

        bytesRemaining = numberSamples;

        audioPlayer.begin(0);

        while (bytesRemaining > 0) {

            // how many more bytes do we have to read?
            bytesToRead = Math.min(bytesRemaining, AUDIO_BUFFER_SIZE);

            try {
                // we want to fill the socketBuffer completely before playing
                int nRead = 0;
                do {
                    int read = dataReader.read(socketBuffer, nRead, bytesToRead);

                    if (metrics && !firstByteReceived) {
                        receiveTime = System.currentTimeMillis();
                    }
                    nRead += read;
                    bytesToRead -= read;
                }
                while (bytesToRead > 0);

                if (nRead < 0) {
                    System.err.println("error reading samples");
                } else {
                    bytesRemaining -= nRead;

                    if (metrics && !firstByteReceived) {
                        firstSoundTime = System.currentTimeMillis();
                        firstByteReceived = true;
                    }
                    audioPlayer.write(socketBuffer, 0, nRead);
                }
            } catch (IOException ioe) {
                logger.log(Level.ERROR, ioe.getMessage(), ioe);
            }

            logger.log(Level.DEBUG, "BytesRemaining: " + bytesRemaining);
        }

        audioPlayer.end();

        logger.log(Level.DEBUG, "finished");
    }

    /**
     * Main program to run the client.
     */
    public static void main(String[] argv) {
        Client client = new Client();
        client.runTTSProtocol();
        System.exit(0);
    }
}
