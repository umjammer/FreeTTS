/*
 * Copyright 2001 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.net.URI;

import com.sun.speech.freetts.lexicon.LetterToSound;
import com.sun.speech.freetts.lexicon.LetterToSoundImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.System.getLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Provides junit tests for the LetterToSound class
 *
 * @version 1.0
 */
public class LetterToSoundTest {

    private static final Logger logger = getLogger(LetterToSoundTest.class.getName());
    
    BufferedReader reader = null;
    LetterToSound lts = null;

    /**
     * Common code run before each test
     */
    @BeforeEach
    protected void setUp() throws Exception {
        lts = new LetterToSoundImpl(LetterToSoundTest.class.getResource("/com/sun/speech/freetts/en/us/cmulex_lts.bin").toURI(), true);
        assertNotNull(lts, "LTS Rules created");
        InputStream in = LetterToSoundTest.class.getResourceAsStream("LTS.txt");
        reader = new BufferedReader(new InputStreamReader(in));
        assertNotNull(reader, "Data File opened");
    }

    /**
     * Tests to see that we succeed
     */
    @Test
    void testSuccess() {
        assertTrue(true, "Should succeed");
    }

    /**
     * Tests that LTS generated match those from the standard results.
     */
    @Test
    void testLTS() {
        String word;
        int i;
        String flite_phones;
        String[] lts_phone_array;
        StringBuilder lts_phones;
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("***")) {
                    continue;
                }
                i = line.indexOf(' ');
                word = line.substring(0, i);
                flite_phones = line.substring(i + 1);
                lts_phone_array = lts.getPhones(word, null);
                assertNotNull(lts_phone_array, "Phones returned for " + word + " is not null: ");
                lts_phones = new StringBuilder("(");
                for (i = 0; i < lts_phone_array.length; i++) {
                    if (i != 0) {
                        lts_phones.append(" ");
                    }
                    lts_phones.append(lts_phone_array[i]);
                }
                lts_phones.append(")");
                assertEquals(flite_phones, lts_phones.toString(), "Phones returned for " + word + " are identical "
                        + "(Our phones: " + lts_phones + ", "
                        + "Flite phones: " + flite_phones + "): ");
            }
        } catch (IOException e) {
            fail("FILE IO problem: ");
        }
    }

    /**
     * Tests to see if the binary version of the database matches
     * that of the text database.
     */
    @Test
    void testBinaryLoad() {
        try {
            LetterToSoundImpl text = new LetterToSoundImpl(
                    URI.create("file:src/main/voices/com/sun/speech/freetts/en/us/cmulex_lts.txt"), false);
            LetterToSoundImpl binary = new LetterToSoundImpl(
                    LetterToSoundTest.class.getResource("/com/sun/speech/freetts/en/us/cmulex_lts.bin").toURI(), true);

            assertTrue(text.compare(binary), "text binary compare");
        } catch (Exception ioe) {
            logger.log(Level.ERROR, ioe.getMessage(), ioe);
            fail("Can't load lts " + ioe);
        }
    }
}