/**
 * Portions Copyright 2001-2005 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute,
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;

import com.sun.speech.freetts.audio.AudioPlayer;
import com.sun.speech.freetts.audio.JavaClipAudioPlayer;
import com.sun.speech.freetts.audio.MultiFileAudioPlayer;
import com.sun.speech.freetts.audio.NullAudioPlayer;
import com.sun.speech.freetts.audio.RawFileAudioPlayer;
import com.sun.speech.freetts.audio.SingleFileAudioPlayer;


/**
 * Standalone utility that directly interacts with a CMUDiphoneVoice.
 */
public class FreeTTS {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(FreeTTS.class.getName());

    /** Version number. */
    public final static String VERSION = "FreeTTS 1.2.2";
    private Voice voice;
    private static AudioPlayer audioPlayer = null;
    private boolean silent = false;
    private String audioFile = null;
    private boolean multiAudio = false;
    private boolean streamingAudio = false;
    private InputMode inputMode = InputMode.INTERACTIVE;

    /**
     * Constructs a default FreeTTS with the kevin16 voice.
     */
    public FreeTTS() {
        VoiceManager voiceManager = VoiceManager.getInstance();
        voiceManager.getVoice("kevin16");
    }

    /**
     * Creates a FreeTTS object with the given Voice.
     *
     * @param voice the voice to use
     */
    public FreeTTS(Voice voice) {
        this.voice = voice;
    }

    /**
     * Starts this FreeTTS Synthesizer by loading the void and creating a new
     * AudioPlayer.
     */
    public void startup() {
        voice.allocate();
        if (!getSilentMode()) {
            if (audioFile != null) {
                AudioFileFormat.Type type = getAudioType(audioFile);
                if (type != null) {
                    if (multiAudio) {
                        audioPlayer = new MultiFileAudioPlayer(getBasename(audioFile), type);
                    } else
                        audioPlayer = new SingleFileAudioPlayer(getBasename(audioFile), type);
                } else {
                    try {
                        audioPlayer = new RawFileAudioPlayer(audioFile);
                    } catch (IOException ioe) {
                        System.out.println("Can't open " + audioFile + " " + ioe);
                    }
                }
            } else if (!streamingAudio) {
                audioPlayer = new JavaClipAudioPlayer();
            } else {
                try {
                    audioPlayer = voice.getDefaultAudioPlayer();
                } catch (InstantiationException e) {
                    logger.log(Level.ERROR, e.getMessage(), e);
                }
            }
        }

        if (audioPlayer == null) {
            audioPlayer = new NullAudioPlayer();
        }

        voice.setAudioPlayer(audioPlayer);
    }

    /**
     * Returns the audio type based upon the extension of the given file
     *
     * @param file the file of interest
     * @return the audio type of the file or null if it is a non-supported type
     */
    private static AudioFileFormat.Type getAudioType(String file) {
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();
        String extension = getExtension(file);

        for (AudioFileFormat.Type type : types) {
            if (type.getExtension().equals(extension)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Given a filename returns the extension for the file
     *
     * @param path the path to extract the extension from
     * @return the extension or <code>null</code> if none
     */
    private static String getExtension(String path) {
        int index = path.lastIndexOf(".");
        if (index == -1) {
            return null;
        } else {
            return path.substring(index + 1);
        }
    }

    /**
     * Given a filename returns the basename for the file
     *
     * @param path the path to extract the basename from
     * @return the basename of the file
     */
    private static String getBasename(String path) {
        int index = path.lastIndexOf(".");
        if (index == -1) {
            return path;
        } else {
            return path.substring(0, index);
        }
    }

    /**
     * Shuts down this FreeTTS synthesizer by closing the AudioPlayer and voice.
     */
    public void shutdown() {
        try {
            audioPlayer.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "error closing the audio player: " + e.getMessage(), e);
        }
        voice.deallocate();
    }

    /**
     * Converts the given text to speech based using processing options
     * currently set in FreeTTS.
     *
     * @param text the text to speak
     * @return true if the utterance was played properly
     */
    public boolean textToSpeech(String text) {
        return voice.speak(text);
    }

    /**
     * Converts the given text to speech based using processing options
     * currently set in FreeTTS.
     *
     * @param text the text to speak
     * @return true if the utterance was played properly
     */
    private boolean batchTextToSpeech(String text) {
        boolean ok;
        voice.startBatch();
        ok = textToSpeech(text);
        voice.endBatch();
        return ok;
    }

    /**
     * Reads the file pointed to by the given path and renders each line as
     * speech individually.
     */
    private boolean lineToSpeech(String path) {
        boolean ok = true;
        voice.startBatch();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path));
            String line;

            while ((line = reader.readLine()) != null && ok) {
                ok = textToSpeech(line);
            }
            reader.close();
        } catch (IOException ioe) {
            logger.log(Level.ERROR, "can't read " + path);
            throw new Error(ioe);
        }
        voice.endBatch();

        return ok;
    }

    /**
     * Returns the voice used by FreeTTS.
     *
     * @return the voice used by freetts
     */
    protected Voice getVoice() {
        return voice;
    }

    /**
     * Converts the text contained in the given stream to speech.
     *
     * @param is the stream containing the text to speak
     */
    public boolean streamToSpeech(InputStream is) {
        boolean ok;
        voice.startBatch();
        ok = voice.speak(is);
        voice.endBatch();
        return ok;
    }

    /**
     * Converts the text contained in the given path to speech.
     *
     * @param urlPath the file containing the text to speak
     * @return true if the utterance was played properly
     */
    public boolean urlToSpeech(String urlPath) {
        boolean ok = false;
        try {
            URL url = new URL(urlPath);
            InputStream is = url.openStream();
            ok = streamToSpeech(is);
        } catch (IOException ioe) {
            logger.log(Level.INFO, "Can't read data from " + urlPath);
        }
        return ok;
    }

    /**
     * Converts the text contained in the given path to speech.
     *
     * @param filePath the file containing the text to speak
     * @return true if the utterance was played properly
     */
    public boolean fileToSpeech(String filePath) {
        boolean ok = false;
        try {
            InputStream is = Files.newInputStream(Paths.get(filePath));
            ok = streamToSpeech(is);
        } catch (IOException ioe) {
            logger.log(Level.INFO, "Can't read data from " + filePath);
        }
        return ok;
    }

    /**
     * Turns audio playing on and off.
     *
     * @param silent if true, don't play audio
     */
    public void setSilentMode(boolean silent) {
        this.silent = silent;
    }

    /**
     * Gets silent mode.
     *
     * @return true if in silent mode
     * @see #setSilentMode
     */
    public boolean getSilentMode() {
        return this.silent;
    }

    /**
     * Sets the input mode.
     *
     * @param inputMode the input mode
     */
    public void setInputMode(InputMode inputMode) {
        this.inputMode = inputMode;
    }

    /**
     * Returns the InputMode.
     *
     * @return the input mode
     * @see #setInputMode
     */
    public InputMode getInputMode() {
        return this.inputMode;
    }

    /**
     * Sets the audio file .
     *
     * @param audioFile the audioFile
     */
    public void setAudioFile(String audioFile) {
        this.audioFile = audioFile;
    }

    /**
     * Sets multi audio. If true, and an audio file has been set output will be
     * sent to multiple files
     *
     * @param multiAudio if <code>true</code> send output to multiple files.
     */
    public void setMultiAudio(boolean multiAudio) {
        this.multiAudio = multiAudio;
    }

    /**
     * Sets streaming audio. If true, output will be sent to
     *
     * @param streamingAudio if <code>true</code> stream audio
     */
    public void setStreamingAudio(boolean streamingAudio) {
        this.streamingAudio = streamingAudio;
    }

    /**
     * Prints the usage message for FreeTTS.
     */
    static void usage(String voices) {
        System.out.println(VERSION);
        System.out.println("Usage:");
        System.out.println("    -detailedMetrics: turn on detailed metrics");
        System.out.println("    -dumpAudio file : dump audio to file ");
        System.out.println("    -dumpAudioTypes : dump the possible" + " output types");
        System.out.println("    -dumpMultiAudio file : dump audio to file ");
        System.out.println("    -dumpRelations  : dump the relations ");
        System.out.println("    -dumpUtterance  : dump the final utterance");
        System.out.println("    -dumpASCII file : dump the final wave to file as ASCII");
        System.out.println("    -file file      : speak text from given file");
        System.out.println("    -lines file     : render lines from a file");
        System.out.println("    -help           : shows usage information");
        System.out.println("    -voiceInfo      : print detailed voice info");
        System.out.println("    -metrics        : turn on metrics");
        System.out.println("    -run  name      : sets the name of the run");
        System.out.println("    -silent         : don't say anything");
        System.out.println("    -streaming      : use streaming audio player");
        System.out.println("    -text say me    : speak given text");
        System.out.println("    -url path       : speak text from given URL");
        System.out.println("    -verbose        : verbose output");
        System.out.println("    -version        : shows version number");
        System.out.println("    -voice VOICE    : " + voices);
    }

    /**
     * Starts interactive mode on the given FreeTTS. Reads text from the console
     * and gives it to FreeTTS to speak. terminates on end of file.
     *
     * @param freetts the engine
     */
    private static void interactiveMode(FreeTTS freetts) {
        try {
            while (true) {
                String text;
                BufferedReader reader;
                reader = new BufferedReader(new InputStreamReader(System.in));
                System.out.print("Enter text: ");
                System.out.flush();
                text = reader.readLine();
                if ((text == null) || (text.isEmpty())) {
                    freetts.shutdown();
                    System.exit(0);
                } else {
                    freetts.batchTextToSpeech(text);
                }
            }
        } catch (IOException e) {
        }
    }

    /**
     * Dumps the possible audio output file types
     */
    private static void dumpAudioTypes() {
        AudioFileFormat.Type[] types = AudioSystem.getAudioFileTypes();

        for (AudioFileFormat.Type type : types) {
            System.out.println(type.getExtension());
        }
    }

    /**
     * The main entry point for FreeTTS.
     *
     * @see "mvn -P demo antrun:run@FreeTTS"
     */
    public static void main(String[] args) {

        String text = null;
        String inFile = null;
        boolean dumpAudioTypes = false;
        Voice voice = null;

        VoiceManager voiceManager = VoiceManager.getInstance();
        String voices = voiceManager.toString();

        // find out what Voice to use first
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-voice")) {
                if (++i < args.length) {
                    String voiceName = args[i];
                    if (voiceManager.contains(voiceName)) {
                        voice = voiceManager.getVoice(voiceName);
                    } else {
                        System.out.println("Invalid voice: " + voiceName);
                        System.out.println("  Valid voices are " + voices);
                        System.exit(1);
                    }
                } else {
                    usage(voices);
                    System.exit(1);
                }
                break;
            }
        }

        if (voice == null) { // default Voice is kevin16
            voice = voiceManager.getVoice("kevin16");
        }

        if (voice == null) {
            throw new Error("The specified voice is not defined");
        }
        FreeTTS freetts = new FreeTTS(voice);

        label:
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
            case "-metrics":
                voice.setMetrics(true);
                break;
            case "-detailedMetrics":
                voice.setDetailedMetrics(true);
                break;
            case "-silent":
                freetts.setSilentMode(true);
                break;
            case "-streaming":
                freetts.setStreamingAudio(true);
                break;
            case "-verbose":
                Handler handler = new ConsoleHandler();
                handler.setLevel(java.util.logging.Level.ALL);
                java.util.logging.Logger.getLogger("com.sun").addHandler(handler);
                java.util.logging.Logger.getLogger("com.sun").setLevel(java.util.logging.Level.ALL);
                break;
            case "-dumpUtterance":
                voice.setDumpUtterance(true);
                break;
            case "-dumpAudioTypes":
                dumpAudioTypes = true;
                break;
            case "-dumpRelations":
                voice.setDumpRelations(true);
                break;
            case "-dumpASCII":
                if (++i < args.length) {
                    voice.setWaveDumpFile(args[i]);
                } else {
                    usage(voices);
                }
                break;
            case "-dumpAudio":
                if (++i < args.length) {
                    freetts.setAudioFile(args[i]);
                } else {
                    usage(voices);
                }
                break;
            case "-dumpMultiAudio":
                if (++i < args.length) {
                    freetts.setAudioFile(args[i]);
                    freetts.setMultiAudio(true);
                } else {
                    usage(voices);
                }
                break;
            case "-version":
                System.out.println(VERSION);
                break;
            case "-voice":
                // do nothing here, just skip the voice name
                i++;
                break;
            case "-help":
                usage(voices);
                System.exit(0);
            case "-voiceInfo":
                System.out.println(VoiceManager.getInstance().getVoiceInfo());
                System.exit(0);
            case "-text":
                freetts.setInputMode(InputMode.TEXT);
                // add the rest of the args as text
                StringBuilder sb = new StringBuilder();
                for (int j = i + 1; j < args.length; j++) {
                    sb.append(args[j]);
                    sb.append(" ");
                }
                text = sb.toString();
                break label;
            case "-file":
                if (++i < args.length) {
                    inFile = args[i];
                    freetts.setInputMode(InputMode.FILE);
                } else {
                    usage(voices);
                }
                break;
            case "-lines":
                if (++i < args.length) {
                    inFile = args[i];
                    freetts.setInputMode(InputMode.LINES);
                } else {
                    usage(voices);
                }
                break;
            case "-url":
                if (++i < args.length) {
                    inFile = args[i];
                    freetts.setInputMode(InputMode.URL);
                } else {
                    usage(voices);
                }
                break;
            case "-run":
                if (++i < args.length) {
                    voice.setRunTitle(args[i]);
                } else {
                    usage(voices);
                }
                break;
            default:
                System.out.println("Unknown option:" + args[i]);
                break;
            }
        }

        if (dumpAudioTypes) {
            dumpAudioTypes();
        }

        freetts.startup();

        if (freetts.getInputMode() == InputMode.TEXT) {
            freetts.batchTextToSpeech(text);
        } else if (freetts.getInputMode() == InputMode.FILE) {
            freetts.fileToSpeech(inFile);
        } else if (freetts.getInputMode() == InputMode.URL) {
            freetts.urlToSpeech(inFile);
        } else if (freetts.getInputMode() == InputMode.LINES) {
            freetts.lineToSpeech(inFile);
        } else {
            interactiveMode(freetts);
        }

        if (freetts.getVoice().isMetrics() && !freetts.getSilentMode()) {
            // TODO get first byte timer times back in
//            freetts.getFirstByteTimer().showTimes();
//            freetts.getFirstSoundTimer().showTimes();
        }

        freetts.shutdown();
        System.exit(0);
    }
}
