/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts;

/**
 * Provides a means to access the voices that are stored in a jar
 * file.  Every jar file that provides a voice must contain a subclass
 * of VoiceDirectory.  The class must provide a main() function that
 * calls dumpVoices() or performs an equivalent operation.  All
 * subclasses of VoiceDirectory can be assumed to always be created by
 * the default constructor (no arguments).
 * <p>
 * Any jar file that has a subclass of VoiceDirectory must define
 * certain attributes in its Manifest.  "Main-class:" must refer to
 * the subclass of VoiceDirectory. "Class-Path:" lists the other jar
 * files upon which this is dependent.  For example,
 * "cmu_us_kal.jar" may be dependent on "en_us.jar" for its lexicon.
 * The Manifest must also have a "FreeTTSVoiceDefinition: true" entry.
 *
 * @see Voice
 * @see VoiceManager
 */
public abstract class VoiceDirectory {
    /**
     * Default constructor does nothing.  This may be overridden by
     * subclasses, but it is not recommended.  This is the only
     * constructor that will be called.
     */
    public VoiceDirectory() {
    }

    /**
     * Provide a means to access the voices in a voice jar file.  The
     * implementation of this function is up to the subclasses.
     *
     * @return an array of Voice instances provided in the jar file
     */
    public abstract Voice[] getVoices();

    /**
     * Print the information about voices contained in this voice
     * directory to a String.
     *
     * @return a String containing the information
     * @see #main(String[] args)
     */
    public String toString() {
        String newline = System.getProperty("line.separator");
        Voice[] voices = getVoices();
        StringBuilder s = new StringBuilder();
        s.append("VoiceDirectory '");
        s.append(this.getClass().getName());
        s.append("'");
        s.append(newline);

        for (Voice voice : voices) {
            s.append(newline);
            s.append("Name: ");
            s.append(voice.getName());
            s.append(newline);
            s.append("\tDescription: ");
            s.append(voice.getDescription());
            s.append(newline);
            s.append("\tOrganization: ");
            s.append(voice.getOrganization());
            s.append(newline);
            s.append("\tDomain: ");
            s.append(voice.getDomain());
            s.append(newline);
            s.append("\tLocale: ");
            s.append(voice.getLocale().toString());
            s.append(newline);
            s.append("\tStyle: ");
            s.append(voice.getStyle());
            s.append(newline);
            s.append("\tGender: ");
            s.append(voice.getGender().toString());
            s.append(newline);
            s.append("\tAge: ");
            s.append(voice.getAge().toString());
            s.append(newline);
            s.append("\tPitch: ");
            s.append(voice.getPitch());
            s.append(newline);
            s.append("\tPitch Range: ");
            s.append(voice.getPitchRange());
            s.append(newline);
            s.append("\tPitch Shift: ");
            s.append(voice.getPitchShift());
            s.append(newline);
            s.append("\tRate: ");
            s.append(voice.getRate());
            s.append(newline);
            s.append("\tVolume: ");
            s.append(voice.getVolume());
            s.append(newline);
            s.append(newline);
        }
        return s.toString();
    }

    /**
     * The main function must be implemented by subclasses to print
     * out information about provided voices.  For example, they may
     * just call dumpVoices()
     *
     * @see #toString()
     */
    public static void main(String[] args) {
        // subclasses must call dumpVoices()
    }
}
