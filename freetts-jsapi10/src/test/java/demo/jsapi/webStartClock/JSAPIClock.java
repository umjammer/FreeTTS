/**
 * Copyright 2003 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.jsapi.webStartClock;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.Locale;
import javax.speech.EngineCreate;
import javax.speech.EngineList;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerModeDesc;

import com.sun.speech.engine.freetts.jsapi.FreeTTSEngineCentral;


/**
 * A talking clock powered by FreeTTS.
 */
public class JSAPIClock extends Clock {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(JSAPIClock.class.getName());

    protected Synthesizer synthesizer;

    /**
     * Creates the synthesizer, called by the constructor.
     */
    @Override
    public void createSynthesizer() {

        try {
            SynthesizerModeDesc desc = new SynthesizerModeDesc(null,
                    "time",
                    Locale.US,
                    Boolean.FALSE,
                    null);

            FreeTTSEngineCentral central = new FreeTTSEngineCentral();
            EngineList list = central.createEngineList(desc);

            if (!list.isEmpty()) {
                EngineCreate creator = (EngineCreate) list.get(0);
                synthesizer = (Synthesizer) creator.createEngine();
            }
            if (synthesizer == null) {
                logger.log(Level.INFO, "Cannot create synthesizer");
                System.exit(1);
            }
            synthesizer.allocate();
            synthesizer.resume();

        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    /**
     * Speaks the given time in full text.
     *
     * @param time time in full text
     */
    @Override
    protected void speak(String time) {
        synthesizer.speakPlainText(time, null);
    }

    /**
     * main() method to run the JSAPIClock.
     */
    public static void main(String[] args) {
        Clock frame = new JSAPIClock();
        frame.pack();
        frame.setVisible(true);
        frame.createSynthesizer();
        frame.startClock();
    }
}
