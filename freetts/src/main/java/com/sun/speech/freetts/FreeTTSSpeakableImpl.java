/**
 * Portions Copyright 2001 Sun Microsystems, Inc.
 * Portions Copyright 1999-2001 Language Technologies Institute,
 * Carnegie Mellon University.
 * All Rights Reserved.  Use is subject to license terms.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.freetts;

import java.io.InputStream;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import org.w3c.dom.Document;


/**
 * Minimal implementation of a FreeTTSSpeakable
 */
public class FreeTTSSpeakableImpl implements FreeTTSSpeakable {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(FreeTTSSpeakableImpl.class.getName());

    private Document doc;
    private String text;
    private InputStream inputStream;
    volatile boolean completed = false;
    volatile boolean cancelled = false;

    /**
     * Constructor.
     *
     * @param text the text to be spoken
     */
    public FreeTTSSpeakableImpl(String text) {
        this.text = text;
    }

    /**
     * Constructor.
     *
     * @param doc the doc to be spoken
     */
    public FreeTTSSpeakableImpl(Document doc) {
        this.doc = doc;
    }

    /**
     * Constructor.
     *
     * @param is the doc to be spoken
     */
    public FreeTTSSpeakableImpl(InputStream is) {
        this.inputStream = is;
    }

    /**
     * Indicate that this speakable has been started.
     */
    @Override
    public void started() {
    }

    /**
     * Indicates that this speakable has been completed.
     */
    @Override
    public synchronized void completed() {
        completed = true;
        notifyAll();
    }

    /**
     * Indicates that this speakable has been cancelled.
     */
    @Override
    public synchronized void cancelled() {
        completed = true;
        cancelled = true;
        notifyAll();
    }

    /**
     * Returns true if this queue item has been
     * processed.
     *
     * @return true if it has been processed
     */
    @Override
    public synchronized boolean isCompleted() {
        return completed;
    }

    /**
     * Waits for this speakable item to be completed.
     *
     * @return true if the item was completed successfully, false if
     * the speakable  was cancelled or an error occurred.
     */
    @Override
    public synchronized boolean waitCompleted() {
        while (!completed) {
            try {
                wait();
            } catch (InterruptedException ie) {
                logger.log(Level.INFO, "FreeTTSSpeakableImpl:Wait interrupted");
                return false;
            }
        }
        return !cancelled;
    }

    /**
     * Returns <code>true</code> if the item contains plain text
     * (not Java Speech Markup Language text).
     *
     * @return true if the item contains plain text
     */
    @Override
    public boolean isPlainText() {
        return text != null;
    }

    /**
     * Returns the text corresponding to this Playable.
     *
     * @return the Playable text
     */
    @Override
    public String getText() {
        return text;
    }

    /**
     * Gets the DOM document for this object.
     *
     * @return the DOM document for this object.
     */
    @Override
    public Document getDocument() {
        return doc;
    }

    /**
     * Returns <code>true</code> if the item is an input stream.
     *
     * @return true if the item is an input stream
     */
    @Override
    public boolean isStream() {
        return inputStream != null;
    }

    /**
     * Gets the input stream.
     *
     * @return the input stream
     */
    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns <code>true</code> if the item is a JSML document
     * (Java Speech Markup Language).
     *
     * @return true if the item is a document
     */
    @Override
    public boolean isDocument() {
        return doc != null;
    }
}
