/**
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute,
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts.en;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;


/**
 * Tags the words in the utterance with their part-of-speech.
 * Currently this does nothing.
 */
public class PartOfSpeechTagger implements UtteranceProcessor {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(PartOfSpeechTagger.class.getName());

    /**
     * Constructs a PartOfSpeechTagger
     */
    public PartOfSpeechTagger() {
    }

    /**
     * Tags the utterance with part-of-speech information. Currently
     * this processor does nothing.
     *
     * @param utterance the utterance to process/tokenize
     * @throws ProcessException if an error occurs while
     *                          processing of the utterance
     */
    @Override
    public void processUtterance(Utterance utterance) throws ProcessException {
        if (logger.isLoggable(Level.DEBUG)) {
            logger.log(Level.DEBUG, "PartOfSpeechTagger does nothing!");
        }
    }

    /**
     * Returns the string representation of the object
     *
     * @return the string representation of the object
     */
    public String toString() {
        return "PartOfSpeechTagger";
    }
}

