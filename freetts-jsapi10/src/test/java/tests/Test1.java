/*
 * Copyright (c) 2023 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package tests;

import java.util.Arrays;
import java.util.Locale;

import javax.speech.Central;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import org.junit.jupiter.api.Test;
import vavi.util.Debug;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2023-01-28 nsano initial version <br>
 */
public class Test1 {

    /**
     * Example of how to list all the known voices.
     */
    static void listAllVoices() {
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice[] voices = voiceManager.getVoices();
System.err.println("---- voices ----");
Arrays.stream(voices).forEach(v -> System.err.println(v.getName()));
System.err.println("---");
    }

    @Test
    void test1() throws Exception {
listAllVoices();

        String voiceName = "kevin16";

        // The VoiceManager manages all the voices for FreeTTS.
        //
        VoiceManager voiceManager = VoiceManager.getInstance();
        Voice helloVoice = voiceManager.getVoice(voiceName);
        helloVoice.setVolume(0.5f); // under 0.8f is too silent
Debug.println(helloVoice);

        helloVoice.allocate();
        helloVoice.speak("Hello world");

        helloVoice.deallocate();
    }

    @Test
    void test2() throws Exception {
        speak("She sells seashells by the seashore.");
    }

    /** */
    static void speak(String text) throws Exception {
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        SynthesizerModeDesc desc = new SynthesizerModeDesc(Locale.US);
        Central.registerEngineCentral("com.sun.speech.engine.freetts.jsapi.FreeTTSEngineCentral");
        Synthesizer synthesizer = Central.createSynthesizer(desc);

        synthesizer.allocate();
        synthesizer.resume();

        SynthesizerModeDesc smd = (SynthesizerModeDesc) synthesizer.getEngineModeDesc();
        Arrays.stream(smd.getVoices()).forEach(v -> System.err.println(v.getName()));
listAllVoices();
        String name = "kevin16";
//        String name = "kevin";
        javax.speech.synthesis.Voice voice = Arrays.stream(smd.getVoices())
                .filter(v -> v.getName().equals(name))
                .findFirst().get();
        synthesizer.getSynthesizerProperties().setVoice(voice);
        synthesizer.getSynthesizerProperties().setVolume(0.5f);

        synthesizer.speakPlainText(text, null);

        synthesizer.waitEngineState(Synthesizer.QUEUE_EMPTY);
        synthesizer.deallocate();
    }
}
