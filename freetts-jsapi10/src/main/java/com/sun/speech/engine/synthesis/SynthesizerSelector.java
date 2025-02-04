/**
 * Copyright 1998-2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.engine.synthesis;

import java.awt.Component;
import java.util.Enumeration;
import java.util.List;
import javax.speech.Central;
import javax.speech.EngineList;
import javax.speech.synthesis.SynthesizerModeDesc;
import javax.swing.JOptionPane;


/**
 * GUI that displays a list of <code>Synthesizer</code> names in a
 * dialog box.  When the user makes a selection, the selected
 * <code>SynthesizerModeDesc</code> is returned to the caller.
 */
public class SynthesizerSelector {

    /**
     * Asks <code>Central</code> for a list of available synthesizers.
     * If there are none, returns <code>null</code>.  If there is only
     * one, returns it.  Otherwise, pops up an input dialog that gives
     * the user a choice.
     *
     * @param component the component for JOptionPane.showInputDialog
     * @param appName   the title for the input dialog
     * @return a <code>SynthesizerModeDesc</code> representing the
     * synthesizer to use.
     */
    static public SynthesizerModeDesc getSynthesizerModeDesc(Component component, String appName) {
        List<SynthesizerModeDesc> synths = new java.util.ArrayList<>();
        List<String> synthNames = new java.util.ArrayList<>();
        EngineList list = Central.availableSynthesizers(null);
        @SuppressWarnings("unchecked")
        Enumeration<SynthesizerModeDesc> e = (Enumeration<SynthesizerModeDesc>) list.elements();
        while (e.hasMoreElements()) {
            synths.add(e.nextElement());
            synthNames.add(synths.get(synths.size() - 1).getEngineName());
        }
        Object[] synthNamesArray = synthNames.toArray();

        if (synths.isEmpty()) {
            return null;
        } else if (synths.size() == 1) {
            return synths.get(0);
        }

        String synthName = (String) JOptionPane.showInputDialog(
                component,
                "Select the Synthesizer to use:",
                appName + ": Select Synthesizer",
                JOptionPane.QUESTION_MESSAGE,
                null,
                synthNamesArray,
                synthNamesArray[0]);

        int index = synthNames.indexOf(synthName);
        if (index == -1) {
            return null;
        }
        return synths.get(index);
    }
}
