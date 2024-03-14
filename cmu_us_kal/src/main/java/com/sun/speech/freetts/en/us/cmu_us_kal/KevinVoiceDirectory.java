package com.sun.speech.freetts.en.us.cmu_us_kal;

import java.lang.System.Logger;
import java.net.URISyntaxException;
import java.util.Locale;

import com.sun.speech.freetts.Age;
import com.sun.speech.freetts.Gender;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceDirectory;
import com.sun.speech.freetts.en.us.CMUDiphoneVoice;
import com.sun.speech.freetts.en.us.CMULexicon;

import static java.lang.System.getLogger;


/**
 * This voice directory provides default US/English Diphone voices
 * imported from CMU Flite
 */
public class KevinVoiceDirectory extends VoiceDirectory {

    private static final Logger logger = getLogger(KevinVoiceDirectory.class.getName());
    
    /**
     * Gets the voices provided by this voice.
     *
     * @return an array of new Voice instances
     */
    @Override
    public Voice[] getVoices() {
        try {
            CMULexicon lexicon = new CMULexicon("cmulex");
            Voice kevin = new CMUDiphoneVoice("kevin", Gender.MALE,
                    Age.YOUNGER_ADULT, "default 8-bit diphone voice",
                    Locale.US, "general", "cmu", lexicon,
                    this.getClass().getResource("cmu_us_kal.bin").toURI());
            Voice kevin16 = new CMUDiphoneVoice("kevin16", Gender.MALE,
                    Age.YOUNGER_ADULT, "default 16-bit diphone voice",
                    Locale.US, "general", "cmu", lexicon,
                    this.getClass().getResource("cmu_us_kal16.bin").toURI());

            Voice[] voices = {kevin, kevin16};
            return voices;
        } catch (NullPointerException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Print out information about this voice jarfile.
     *
     * @see "mvn -P demo antrun:run@KevinVoiceDirectory"
     */
    public static void main(String[] args) {
        System.out.println((new KevinVoiceDirectory()));
    }
}
