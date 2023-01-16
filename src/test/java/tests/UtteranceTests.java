/*
 * Copyright 2001 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package tests;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.sun.speech.freetts.Item;
import com.sun.speech.freetts.ProcessException;
import com.sun.speech.freetts.Relation;
import com.sun.speech.freetts.Utterance;
import com.sun.speech.freetts.UtteranceProcessor;
import com.sun.speech.freetts.Voice;
import com.sun.speech.freetts.VoiceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * JUNIT Tests for the Utterance class
 *
 * @version 1.0
 */
public class UtteranceTests {

    Voice voice;
    Utterance utterance;

    /**
     * Common code run before each test
     */
    @BeforeEach
    protected void setUp() {
        voice = VoiceManager.getInstance().getVoice("kevin");
        utterance = new Utterance(voice);
        List<UtteranceProcessor> processors = voice.getUtteranceProcessors();

        processors.add(new TestUtteranceProcessor("tokenizer"));
        processors.add(new TestUtteranceProcessor("tokentowords"));
        processors.add(new TestUtteranceProcessor("pauses"));
        processors.add(new TestUtteranceProcessor("intonation"));

        voice.getFeatures().setString("testFeature1", "testFeatureValue1");
        voice.getFeatures().setString("sillyText", "how now brown cowboy!");
        voice.getFeatures().setString("knock knock", "Who is there?");

        utterance.setString("uttFeature1", "this is utt feature 1");
        utterance.setString("whitespace", "_+_+_+_+_+_+_");
        utterance.setString("blackspace", "####@#@#@@");
        utterance.setString("inputText", "How now brown cow");

        Relation tokens = utterance.createRelation("tokens");
        StringTokenizer tok = new StringTokenizer(
                "January 1st 2001 How now brown cow "
                        + " it's a far far better thing I do now than I've ever"
                        + "done before");

        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            Item newItem = tokens.appendItem();
            newItem.getFeatures().setString("name", token);
            newItem.getFeatures().setString("punc", "");
            newItem.getFeatures().setString("stress", "1");
        }

        Relation words = utterance.createRelation("Words");


        for (Item item = tokens.getHead();
             item != null; item = item.getNext()) {
            if (item.getFeatures().getString("name").equals("2001")) {

                Item word1 = item.createDaughter();
                word1.getFeatures().setString("name", "two");
                words.appendItem(word1);

                Item word2 = item.createDaughter();
                word2.getFeatures().setString("name", "thousand");
                words.appendItem(word2);

                Item word3 = item.createDaughter();
                word3.getFeatures().setString("name", "one");
                words.appendItem(word3);

            } else {
                Item word = item.createDaughter();
                word.getFeatures().setString("name",
                        item.getFeatures().getString("name").toLowerCase());
                words.appendItem(word);
            }
        }

        wordSylSeg(utterance);
    }

    /**
     * Populates an utterance with the word/syl/seg relations
     *
     * @param u the utterance
     */
    private void wordSylSeg(Utterance u) {
        Relation syl = u.createRelation("Syllable");
        Relation sylStructure = u.createRelation("SylStructure");
        Relation seg = u.createRelation("Segment");

        for (Item word = u.getRelation("Words").getHead();
             word != null; word = word.getNext()) {
            Item ssword = sylStructure.appendItem(word);
            List<String> phones = lookup(word.getFeatures().getString("name"));
            for (String phone : phones) {
                Item segitem = seg.appendItem();
                segitem.getFeatures().setString("name", phone);
                ssword.addDaughter(segitem);
            }
        }
    }

    /**
     * Breaks a word into characters
     *
     * @param word the word
     * @return list of single character strings
     */
    private List<String> lookup(String word) {
        List<String> l = new ArrayList<>();

        for (int i = 0; i < word.length(); i++) {
            String ph = word.substring(i, i + 1);
            l.add(ph);
        }
        return l;
    }

    /**
     * Test feature traversal behavior
     */
    @Test
    void testFeature1() {
        assertNotNull(utterance.getRelation("Syllable"), "syl exist");
        assertNotNull(utterance.getRelation("SylStructure"), "syls exist");
        assertNotNull(utterance.getRelation("Segment"), "segment exists");
        Item item = utterance.getRelation("Segment").getHead();
        item = item.getNext();
        item = item.getNext();
        item = item.getNext();

        // we should be at the 'u' in january
        assertEquals("u", item.findFeature("name").toString(), "name");
        assertEquals("a", item.findFeature("n.name").toString(), "n.name");
        assertEquals("n", item.findFeature("p.name").toString(), "p.name");
        assertEquals("u", item.findItem("R:SylStructure").toString(), "R:SylStructure.parent");
        assertEquals("january", item.findItem("R:SylStructure.parent").
                toString(), "R:SylStructure.parent");
        assertEquals("january", item.findFeature("R:SylStructure.parent.name").toString(), "R:SylStructure.parent.name");
        assertEquals("1st", item.findItem("R:SylStructure.parent.n").toString(), "R:SylStructure.parent.n");

        Item token = utterance.getRelation("tokens").getHead();
        assertEquals("January", token.toString(), "token head");
        assertEquals("2001", token.findFeature("n.n.name").toString(), "n.n.name");
        assertEquals("two", token.findFeature("n.n.daughter.name").
                toString(), "n.n.daughter.name");
        assertEquals("thousand", token.findFeature("n.n.daughter.n.name").
                toString(), "n.n.daughter.n.name");
        assertEquals("one", token.findFeature("n.n.daughtern.name").
                toString(), "n.n.daughtern.name");
    }

    /**
     * Tests to see that we succeed
     */
    @Test
    void testSuccess() {
        assertTrue(true, "Should succeed");
    }

    /**
     * Tests to ensure that an utterance is created properly
     */
    @Test
    void testUtteranceCreation() {
        assertNotNull(utterance, "Utterance Created");
        assertSame(utterance.getVoice(), voice, "retrieved proper voice");
    }

    /**
     * Tests the utterance relations capabilities
     */
    @Test
    void testUtteranceRelations() {
        assertNotNull(utterance, "Utterance Created");
        Relation tokens = utterance.createRelation("tokens");
        assertSame(utterance.getRelation("tokens"), tokens, "retrieved token relation missing");
        assertTrue(utterance.hasRelation("tokens"), "token relation missing");
        assertFalse(utterance.hasRelation("missing_relation"), "missing relation found");
        assertNull(utterance.getRelation("missing_relation"), "retrieved missing token relation found");
    }

    /**
     * Tests the utterance features capabilities
     */
    @Test
    void testUtteranceFeatures() {
        assertNotNull(utterance, "Utterance Created");
        assertFalse(utterance.isPresent("not_present"), "Missing feature found");
        utterance.setString("is_present", "here I am");
        assertTrue(utterance.isPresent("is_present"), "presnt feature found");
        assertEquals("here I am", utterance.getString("is_present"), "present feature retreived");
        assertFalse(utterance.isPresent("voice_feature"), "missing voice feature found");
        voice.getFeatures().setString("voice_feature", "is_set");
        for (boolean b : new boolean[] {voice.getFeatures().isPresent("voice_feature"), utterance.isPresent("voice_feature")}) {
            assertTrue(b,
                    "voice feature missing");
        }
        assertEquals("is_set", utterance.getString("voice_feature"), "voice present feature retreived");

        utterance.setFloat("pi", (float) Math.PI);
        assertEquals(utterance.getFloat("pi"), (float) Math.PI, "float get");

        utterance.setInt("one", 1);
        assertEquals(1, utterance.getInt("one"), "int get");
        Object o = new Object();
        utterance.setObject("object", o);
        assertSame(utterance.getObject("object"), o, "object get");

        try {
            utterance.getFloat("one");
            fail("Cast exception mission");
        } catch (ClassCastException e) {
            assertTrue(true, "cast error OK");
        }
        utterance.remove("one");
        assertFalse(utterance.isPresent("one"), "removed  feature found");
    }


    /**
     * Tests the detailed relations capabilities
     */
    @Test
    void testRelations() {
        int testSize = 10;
        Item[] items = new Item[testSize];
        Relation r = utterance.createRelation("itemTests");
        for (int i = 0; i < testSize; i++) {
            items[i] = r.appendItem();
        }

        assertSame(r.getUtterance(), utterance, "utterance OK");
        assertEquals("itemTests", r.getName(), "Name ok");

        int index = 0;
        for (Item item = r.getHead(); item != null;
             item = item.getNext(), index++) {
            assertSame(items[index], item, "Proper items");
        }

        assertNotEquals(items[0].getSharedContents(), items[1].getSharedContents(), "Items not equal");
        Item dup = r.appendItem();
        Item dup2 = r.appendItem(dup);
        assertEquals(dup.getSharedContents(), dup2.getSharedContents(), "Items should be equal");
    }

    /**
     * Tests the Item class capabilities
     */
    @Test
    void testItems() {
        Relation r = utterance.createRelation("tokens");
        Relation r2 = utterance.createRelation("words");
        Item parent = r.appendItem();
        Item i2 = r.appendItem();
        Item dup2 = r.appendItem(i2);
        Item d1 = parent.createDaughter();
        Item d2 = parent.createDaughter();
        Item d3 = parent.createDaughter();

        assertEquals(dup2.getSharedContents(), i2.getSharedContents(), "dup equals");
        assertSame(d1.getParent(), parent, "parent check 1");
        assertSame(d2.getParent(), parent, "parent check 2");
        assertSame(d3.getParent(), parent, "parent check 3");

        //assertTrue("daugher size", parent.getDaughters().size() == 3);
        assertSame(parent.getDaughter(), d1, "first daughter");
        assertSame(parent.getNthDaughter(1), d2, "second daughter");
        assertSame(parent.getLastDaughter(), d3, "Last daughter");

        assertSame(parent.getOwnerRelation(), r, "owner");

        Item r2i1 = r2.appendItem();
        assertSame(r2i1.getOwnerRelation(), r2, "owner r2i1");

        Item r1i1 = r.appendItem(r2i1);
        assertEquals(r1i1.getSharedContents(), r2i1.getSharedContents(), "r1,r2 equal");
        assertSame(r2i1.getOwnerRelation(), r2, "owner r2i1 reprise");
        assertSame(r1i1.getOwnerRelation(), r, "owner r1i1 ");
        assertNull(r2i1.getParent(), "no parent");
        assertFalse(r1i1.hasDaughters(), "r1i1 no daughters");
        assertSame(r1i1.getUtterance(), utterance, "r1i1 utterance");

        // test the feature capability
        assertFalse(r1i1.getFeatures().isPresent("not_present"), "Missing feature found");
        r1i1.getFeatures().setString("is_present", "here I am");
        assertTrue(r1i1.getFeatures().isPresent("is_present"), "presnt feature found");
        assertEquals("here I am", r1i1.getFeatures().getString("is_present"), "present feature retreived");
        assertFalse(r1i1.getFeatures().isPresent("voice_feature"), "missing voice feature found");

        r1i1.getFeatures().setFloat("pi", (float) Math.PI);
        assertEquals(r1i1.getFeatures().getFloat("pi"), (float) Math.PI, "float get");
        assertEquals(r2i1.getFeatures().getFloat("pi"), (float) Math.PI, "float get r2");

        r1i1.getFeatures().setInt("one", 1);
        assertEquals(1, r1i1.getFeatures().getInt("one"), "int get");
        Object o = new Object();
        r1i1.getFeatures().setObject("object", o);
        assertSame(r1i1.getFeatures().getObject("object"), o, "object get");
        assertSame(r2i1.getFeatures().getObject("object"), o, "object get r2");

        try {
            r1i1.getFeatures().getFloat("one");
            fail("Cast exception mission");
        } catch (ClassCastException e) {
            assertTrue(true, "cast error OK");
        }
        r1i1.getFeatures().remove("one");
        assertFalse(r1i1.getFeatures().isPresent("one"), "removed  feature found");
    }
}

/**
 * A test utterance processor
 */
class TestUtteranceProcessor implements UtteranceProcessor {
    String name;

    public TestUtteranceProcessor(String name) {
        this.name = name;
    }

    public void processUtterance(Utterance u) throws ProcessException {
        System.out.println("Processing " + name);
    }

    public String toString() {
        return name;
    }
}

  
