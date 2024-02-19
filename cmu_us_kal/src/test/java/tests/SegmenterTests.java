/*
 * Copyright 2001 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package tests;

import java.io.PrintWriter;
import java.util.StringTokenizer;

import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Segmenter;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * Tests for the Utterance class
 *
 * @version 1.0
 */
public class SegmenterTests {

    Voice voice;
    Utterance utterance;
    UtteranceProcessor wordSylSeg = new Segmenter();

    /**
     * given some text, create a word relation
     * and the syllable relations that go with it.
     *
     * @param text the text to process
     * @return the utterance
     */
    public Utterance getSyllables(String text) {
        VoiceManager voiceManager = VoiceManager.getInstance();
        voice = voiceManager.getVoice("kevin");
        voice.allocate();
        utterance = new Utterance(voice);
        Relation words = utterance.createRelation("Word");
        StringTokenizer tok = new StringTokenizer(text);

        while (tok.hasMoreTokens()) {
            Item word = words.appendItem();
            word.getFeatures().setString("name", tok.nextToken().toLowerCase());
        }
        try {
            wordSylSeg.processUtterance(utterance);
        } catch (ProcessException pe) {
            System.out.println("Error processing " + text);
        }
        return utterance;
    }

    /**
     * Tests simple syllable and segment behavior
     */
    @Test
    void testHowNowBrownCow() {
        Utterance u = getSyllables("how now brown cowboy");
        Relation segment = u.getRelation("Segment");
        assertNotNull(segment, "segment");
        assertNotNull(u.getRelation("Syllable"), "Syllable");
        assertNotNull(u.getRelation("SylStructure"), "SylStructure");
    }

    /**
     * Tests to see if the segment names are created properly, as well
     * as the syllable structure is created properly.
     */
    @Test
    void testJanuary() {
        Utterance u = getSyllables("january first two thousand and one");
        Relation segment = u.getRelation("Segment");
        assertNotNull(segment, "segment");
        assertNotNull(u.getRelation("Syllable"), "Syllable");
        assertNotNull(u.getRelation("SylStructure"), "SylStructure");

        // tests the segment

        //assertTrue("segment size", segment.getItems().size() == 26);

        // spot check some segments
        Item i = segment.getHead();
        assertEquals("jh", i.toString(), "seg jh");
        i = i.getNext();
        assertEquals("ae", i.toString(), "seg ae");
        i = i.getNext();
        assertEquals("n", i.toString(), "seg n");
        i = i.getNext();
        assertEquals("y", i.toString(), "seg y");
        i = i.getNext();
        assertEquals("uw", i.toString(), "seg uw");
        i = i.getNext();
        assertEquals("eh", i.toString(), "seg eh");
        i = i.getNext();
        assertEquals("r", i.toString(), "seg r");
        i = i.getNext();
        assertEquals("iy", i.toString(), "seg iy");
        i = i.getNext();
        assertEquals("f", i.toString(), "seg f");
        i = i.getNext();
        assertEquals("er", i.toString(), "seg er");
        i = i.getNext();
        assertEquals("s", i.toString(), "seg s");
        i = i.getNext();
        assertEquals("t", i.toString(), "seg t");
        i = i.getNext();

        // spot check the SylStructure
        Relation sylStructure = u.getRelation("SylStructure");
        //assertTrue("sylStructure size", sylStructure.getItems().size() == 6);

        Item si = sylStructure.getHead();
        assertEquals("january", si.toString(), "january");
        si = si.getNext();
        assertEquals("first", si.toString(), "first");
        si = si.getNext();
        assertEquals("two", si.toString(), "two");
        si = si.getNext();
        assertEquals("thousand", si.toString(), "thousand");
        si = si.getNext();
        assertEquals("and", si.toString(), "and");
        si = si.getNext();
        assertEquals("one", si.toString(), "one");
        si = si.getNext();

        Item january = sylStructure.getHead();

        assertEquals("january", january.findItem("R:Word").toString(), "findItem");

        assertEquals("first", january.findItem("R:Word.n").toString(), "findItem");
        assertEquals("two", january.findItem("R:Word.n.n").toString(), "findItem");
        assertEquals("first", january.findItem("R:Word.n.n.p").toString(), "findItem");

        PrintWriter pw = new PrintWriter(System.out);
        january.findItem("daughter.daughter").dump(pw, 4, "dd");

        assertEquals("jh", january.findItem("daughter.daughter").toString(), "findItem");

        assertEquals("ae", january.findItem("daughter.daughter.n").toString(), "findItem");

        assertEquals("jh", january.findItem("daughter.daughter.n.p").toString(), "findItem");
        assertEquals("n", january.findItem("daughter.daughtern").toString(), "findItem");
        assertEquals("january", january.findItem("daughter.daughtern.parent.parent").toString(), "findItem");
        assertEquals("ae", january.findItem("daughter.daughtern.parent.parent.R:Word.R:SylStructure" + ".daughter.daughter.n").toString(), "findItem");

        assertEquals("n", january.findItem("daughter.daughtern").toString(), "findItem");

        assertEquals("1", january.findFeature("daughter.stress").toString(), "findFeature");

        Item firstSyllable = january.getDaughter();
//        assertTrue("num seg in syl", firstSyllable.getDaughters().size() == 3);
        Item l = firstSyllable.getDaughter();
        assertEquals("jh", l.toString(), "syl jh");
        l = l.getNext();
        assertEquals("ae", l.toString(), "syl ae");
        l = l.getNext();
        assertEquals("n", l.toString(), "syl n");
        l = l.getNext();
    }

//    /**
//     * Main entry point for this test suite.
//     *
//     * @param args the command line arguments.
//     */
//    public static void main(String[] args) {
//        // String inputText = "for score and seven years ago";
//        String inputText = "january first two thousand and one";
//
//        if (args.length > 0) {
//            inputText = args[0];
//        }
//
//        SegmenterTests wsst = new SegmenterTests("tests");
//        Utterance t1 = wsst.getSyllables(inputText);
//        t1.dump("t1");
//    }
}


