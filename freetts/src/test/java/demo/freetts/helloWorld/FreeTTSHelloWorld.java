/**
 * Copyright 2003 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.freetts.helloWorld;

import java.util.logging.Logger;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;


/**
 * Simple program to demonstrate the use of the FreeTTS speech
 * synthesizer.  This simple program shows how to use FreeTTS
 * without requiring the Java Speech API (JSAPI).
 */
public class FreeTTSHelloWorld {

    /** Logger instance. */
    private static final Logger logger = Logger.getLogger(FreeTTSHelloWorld.class.getName());

    /**
     * Example of how to list all the known voices.
     */
    public static void listAllVoices() {
        System.out.println();
        System.out.println("All voices available:");
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[] voices = voiceManager.getVoices();
        for (Voice voice : voices) {
            System.out.println("    " + voice.getName() + " (" + voice.getDomain() + " domain)");
        }
    }

    /**
     * @see "mvn -P demo antrun:run@hello-world"
     */
    public static void main(String[] args) {

        listAllVoices();

        String voiceName = (args.length > 0) ? args[0] : "kevin16";

        System.out.println();
        System.out.println("Using voice: " + voiceName);

        // The VoiceManager manages all the voices for FreeTTS.
        //
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice helloVoice = voiceManager.getVoice(voiceName);

        if (helloVoice == null) {
            logger.info("Cannot find a voice named " + voiceName + ".  Please specify a different voice.");
            System.exit(1);
        }

        // Allocates the resources for the voice.
        //
        helloVoice.allocate();

        // Synthesize speech.
        //
        helloVoice.speak("Thank you for giving me a voice. " + "I'm so glad to say hello to this world.");

        // Clean up and leave.
        //
        helloVoice.deallocate();
        System.exit(0);
    }
}
