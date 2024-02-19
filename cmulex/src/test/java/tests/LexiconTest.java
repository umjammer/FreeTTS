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
import java.io.Reader;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import com.sun.speech.freetts.en.us.CMULexicon;
import com.sun.speech.freetts.lexicon.Lexicon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.lang.System.getLogger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


/**
 * Tests for the LexiconTest class
 *
 * @version 1.0
 */
public class LexiconTest {

    private static final Logger logger = getLogger(LexiconTest.class.getName());

    BufferedReader reader = null;
    Lexicon lex = null;

    /**
     * Common code run before each test
     */
    @BeforeEach
    protected void setUp() {
        try {
            lex = CMULexicon.getInstance(true);
            assertNotNull(lex, "Lexicon Created");
            InputStream in =
                    LexiconTest.class.getResourceAsStream("LEX.txt");
            Reader inputReader = new InputStreamReader(in);
            reader = new BufferedReader(inputReader);
            assertNotNull(reader, "Data File opened");
        } catch (Exception e) {
            logger.log(Level.ERROR, e.getMessage(), e);
        }
    }

    /**
     * Tests to see that we succeed
     */
    @Test
    void testSuccess() {
        assertTrue(true, "Should succeed");
    }

    /**
     * Tests that Lexicon matches those from the standard results.
     */
    @Test
    void testLexicon() {
        String word;
        String pos;
        int i;
        String flite_phones;
        String[] lex_phone_array;
        StringBuilder lex_phones;
        String line;
        lex_phone_array = lex.getPhones("dirk", null);
        for (String s : lex_phone_array) {
            System.out.println(s);
        }
        try {
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("***")) {
                    continue;
                }
                i = line.indexOf(' ');
                word = line.substring(0, i);
                line = line.substring(i + 1);
                i = line.indexOf(' ');
                pos = line.substring(0, i);
                flite_phones = line.substring(i + 1);
                lex_phone_array = lex.getPhones(word, pos);
                assertNotNull(lex_phone_array, "Phones returned for " + word + pos + " is not null: ");
                lex_phones = new StringBuilder("(");
                for (i = 0; i < lex_phone_array.length; i++) {
                    if (i != 0) {
                        lex_phones.append(" ");
                    }
                    lex_phones.append(lex_phone_array[i]);
                }
                lex_phones.append(")");
                assertEquals(flite_phones, lex_phones.toString(), "Phones returned for " + word + pos
                        + " are identical "
                        + "(Our phones: " + lex_phones + ", "
                        + "Flite phones: " + flite_phones + "): ");
            }
        } catch (IOException e) {
            fail("FILE IO problem: ");
        }
    }
}
