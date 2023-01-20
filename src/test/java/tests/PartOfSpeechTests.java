/*
 * Copyright 2001 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package tests;

import java.net.URL;

import com.sun.speech.freetts.PartOfSpeech;
import com.sun.speech.freetts.PartOfSpeechImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


/**
 * JUnit Tests for the PartOfSpeech class
 *
 * @version 1.0
 */
public class PartOfSpeechTests {

    PartOfSpeech pos;

    /**
     * Common code run before each test
     */
    @BeforeEach
    protected void setUp() {
        try {
            URL url = PartOfSpeechTests.class.getResource("part_of_speech.txt");
            pos = new PartOfSpeechImpl(url, "content");
        } catch (Exception ioe) {
            System.out.println("Can't open part_of_speech.txt");
        }
    }

    /**
     * test that checks for proer determination of part-of-speech
     */
    @Test
    void testPartOfSpeech() {
        assertEquals("in", pos.getPartOfSpeech("of"));
        assertEquals("in", pos.getPartOfSpeech("from"));
        assertEquals("in", pos.getPartOfSpeech("about"));
        assertEquals("in", pos.getPartOfSpeech("up"));
        assertEquals("in", pos.getPartOfSpeech("down"));

        assertEquals("det", pos.getPartOfSpeech("each"));
        assertEquals("det", pos.getPartOfSpeech("both"));
        assertEquals("det", pos.getPartOfSpeech("no"));
        assertEquals("det", pos.getPartOfSpeech("this"));

        assertEquals("md", pos.getPartOfSpeech("will"));
        assertEquals("md", pos.getPartOfSpeech("can"));
        assertEquals("md", pos.getPartOfSpeech("ought"));
        assertEquals("md", pos.getPartOfSpeech("might"));

        assertEquals("cc", pos.getPartOfSpeech("and"));
        assertEquals("cc", pos.getPartOfSpeech("but"));
        assertEquals("cc", pos.getPartOfSpeech("or"));
        assertEquals("cc", pos.getPartOfSpeech("yet"));

        assertEquals("wp", pos.getPartOfSpeech("who"));
        assertEquals("wp", pos.getPartOfSpeech("what"));
        assertEquals("wp", pos.getPartOfSpeech("where"));
        assertEquals("wp", pos.getPartOfSpeech("when"));

        assertEquals("pps", pos.getPartOfSpeech("her"));
        assertEquals("pps", pos.getPartOfSpeech("his"));
        assertEquals("pps", pos.getPartOfSpeech("our"));
        assertEquals("pps", pos.getPartOfSpeech("mine"));

        assertEquals("aux", pos.getPartOfSpeech("is"));
        assertEquals("aux", pos.getPartOfSpeech("am"));
        assertEquals("aux", pos.getPartOfSpeech("are"));
        assertEquals("aux", pos.getPartOfSpeech("was"));
        assertEquals("aux", pos.getPartOfSpeech("were"));
        assertEquals("aux", pos.getPartOfSpeech("be"));

        assertEquals("punc", pos.getPartOfSpeech("."));
        assertEquals("punc", pos.getPartOfSpeech(","));
        assertEquals("punc", pos.getPartOfSpeech(":"));
        assertEquals("punc", pos.getPartOfSpeech(";"));
        assertEquals("punc", pos.getPartOfSpeech("'"));
        assertEquals("punc", pos.getPartOfSpeech("("));
        assertEquals("punc", pos.getPartOfSpeech("?"));
        assertEquals("punc", pos.getPartOfSpeech(")"));

        assertEquals("content", pos.getPartOfSpeech("bear"));
        assertEquals("content", pos.getPartOfSpeech("lamere"));
        assertEquals("content", pos.getPartOfSpeech("walker"));
        assertEquals("content", pos.getPartOfSpeech("kwok"));
        assertEquals("content", pos.getPartOfSpeech("cumquat"));
        assertEquals("content", pos.getPartOfSpeech("marshmellow"));
        assertEquals("content", pos.getPartOfSpeech("tryptich"));
    }
}
