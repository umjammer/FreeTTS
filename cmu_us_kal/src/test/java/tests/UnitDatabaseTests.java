/*
 * Copyright 2001 Sun Microsystems, Inc.
 *
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package tests;

import java.io.IOException;

import com.sun.speech.freetts.diphone.DiphoneUnitDatabase;
import com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JUnit Tests for the DiphoneUnitDatabase test.
 *
 * @version 1.0
 */
public class UnitDatabaseTests {

    DiphoneUnitDatabase udb;
    private final static String BINARY_DB = "cmu_us_kal.bin";
    private final static String TEXT_DB = "cmu_us_kal.txt";

    /**
     * Common code run before each test
     */
    @BeforeEach
    protected void setUp() throws Exception {
        udb = new DiphoneUnitDatabase(KevinVoiceDirectory.class.getResource(BINARY_DB).toURI(), true);
    }

    /**
     * Checks to make sure that the  binary and text version of the DB
     * compare.
     */
    @Test
    void testIdentical() throws Exception {
        DiphoneUnitDatabase udbTextVersion = null;
        try {
            udbTextVersion = new DiphoneUnitDatabase(KevinVoiceDirectory.class.getResource(TEXT_DB).toURI(), false);

        } catch (IOException ioe) {
            System.out.println("Can't load text db " + ioe);
        }
        assertNotNull(udb, "db loaded");
        assertNotNull(udbTextVersion, "txt db loaded");
        assertTrue(udb.compare(udbTextVersion), "DBs identical");
    }

    /**
     * Tests to see that we succeed
     */
    @Test
    void testSuccess() {
        assertTrue(true, "Should succeed");
    }
}


