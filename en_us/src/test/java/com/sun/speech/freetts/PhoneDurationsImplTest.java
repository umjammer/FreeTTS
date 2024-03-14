/**
 *
 */

package com.sun.speech.freetts;

import java.net.URI;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Test case for the PhoneDurations.
 *
 * @author Dirk Schnelle-Walka
 */
public class PhoneDurationsImplTest {

    private PhoneDurations durations;

    /**
     * Set up the test environment.
     */
    @BeforeEach
    public void setUp() throws Exception {
        URI url = PhoneDurationsImplTest.class.getResource("/com/sun/speech/freetts/en/us/dur_stat.txt").toURI();
        durations = new PhoneDurationsImpl(url);
    }

    /**
     * Test method for {@link com.sun.speech.freetts.PhoneDurationsImpl#getPhoneDuration(java.lang.String)}.
     */
    @Test
    void testGetPhoneDuration() {
        PhoneDuration duration = durations.getPhoneDuration("ey");
        assertEquals(0.165883f, duration.getMean());
        assertEquals(0.075700f, duration.getStandardDeviation());
    }

    /**
     * Test method for {@link com.sun.speech.freetts.PhoneDurationsImpl#getPhoneDuration(java.lang.String)}.
     */
    @Test
    void testGetPhoneDurationUnknown() {
        assertNull(durations.getPhoneDuration("asdf"));
    }
}
