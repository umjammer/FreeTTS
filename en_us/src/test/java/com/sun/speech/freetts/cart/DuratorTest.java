package com.sun.speech.freetts.cart;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.sun.speech.freetts.PhoneDurations;
import com.sun.speech.freetts.PhoneDurationsImpl;
import com.sun.speech.freetts.Token;
import com.sun.speech.freetts.Utterance;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


/**
 * Test case for the Durator.
 *
 * @author Dirk Schnelle-Walka
 */
public class DuratorTest {

    private Durator durator;

    /**
     * Set up the test environment.
     */
    @BeforeEach
    public void setUp() throws Exception {
        URI urlCart = DuratorTest.class.getResource("/com/sun/speech/freetts/en/us/durz_cart.txt").toURI();
        CARTImpl cart = new CARTImpl(urlCart);
        URI urlPhones = DuratorTest.class.getResource("/com/sun/speech/freetts/en/us/dur_stat.txt").toURI();
        PhoneDurations durations = new PhoneDurationsImpl(urlPhones);
        durator = new Durator(cart, durations);
    }

    @Test
    void testProcessUtterance() {
        List<Token> tokenList = new ArrayList<>();
        Utterance utterance = new Utterance(null, tokenList);
    }
}
