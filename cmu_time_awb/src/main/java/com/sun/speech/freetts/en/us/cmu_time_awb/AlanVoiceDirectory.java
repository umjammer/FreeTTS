package com.sun.speech.freetts.en.us.cmu_time_awb;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URISyntaxException;
import java.util.Locale;

import com.sun.speech.freetts.Age;
import com.sun.speech.freetts.Gender;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceDirectory;
import com.sun.speech.freetts.en.us.CMUClusterUnitVoice;
import com.sun.speech.freetts.en.us.CMULexicon;

import static java.lang.System.getLogger;


/**
 * This voice directory provides a default US/English Cluster Unit
 * voice imported from CMU Flite.
 */
public class AlanVoiceDirectory extends VoiceDirectory {

    private static final Logger logger = getLogger(AlanVoiceDirectory.class.getName());

    /**
     * Gets the voices provided by this voice.
     *
     * @return an array of new Voice instances
     */
    @Override
    public Voice[] getVoices() {
        try {
            CMULexicon lexicon = new CMULexicon("cmutimelex");
            Voice alan = new CMUClusterUnitVoice("alan", Gender.MALE,
                    Age.YOUNGER_ADULT, "default time-domain cluster unit voice",
                    Locale.US, "time", "cmu", lexicon,
                    this.getClass().getResource("cmu_time_awb.bin").toURI());
            Voice[] voices = {alan};
            return voices;
        } catch (NullPointerException | URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Print out information about this voice jarfile.
     *
     * @see "mvn -P demo antrun:run@AlanVoiceDirectory"
     */
    public static void main(String[] args) {
        System.out.println((new AlanVoiceDirectory()));
    }
}
