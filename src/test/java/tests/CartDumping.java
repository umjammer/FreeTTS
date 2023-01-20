package tests;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sun.speech.freetts.cart.CARTImpl;
import com.sun.speech.freetts.en.us.CMUVoice;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 * JUnit tests Tests to dump cart trees into dot files
 *
 * @version 1.0
 */
public class CartDumping {

    @BeforeAll
    static void setup() throws Exception {
        Path out = Paths.get("tmp/dotfiles");
        Files.createDirectories(out);
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
    void testCartDumping() throws IOException {
        CARTImpl numbersCart = new CARTImpl(CMUVoice.class.getResource("nums_cart.txt"));
        CARTImpl phrasingCart = new CARTImpl(CMUVoice.class.getResource("phrasing_cart.txt"));
        CARTImpl accentCart = new CARTImpl(CMUVoice.class.getResource("int_accent_cart.txt"));
        CARTImpl toneCart = new CARTImpl(CMUVoice.class.getResource("int_tone_cart.txt"));
        CARTImpl durzCart = new CARTImpl(CMUVoice.class.getResource("durz_cart.txt"));

        //
        // Dump the CART tree as a dot file.
        //
        // The dot tool is part of the graphviz distribution at http://www.graphviz.org/.
        // If installed, call it as "dot -O -Tpdf *.dot" from the console to generate pdfs.
        //

        numbersCart.dumpDot(new PrintWriter(new FileOutputStream("tmp/dotfiles/numbersCart.dot")));
        phrasingCart.dumpDot(new PrintWriter(new FileOutputStream("tmp/dotfiles/phrasingCart.dot")));
        accentCart.dumpDot(new PrintWriter(new FileOutputStream("tmp/dotfiles/accentCart.dot")));
        toneCart.dumpDot(new PrintWriter(new FileOutputStream("tmp/dotfiles/toneCart.dot")));
        durzCart.dumpDot(new PrintWriter(new FileOutputStream("tmp/dotfiles/durzCart.dot")));
    }
}
