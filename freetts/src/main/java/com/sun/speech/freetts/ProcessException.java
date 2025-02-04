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

/**
 * Thrown by an UtteranceProcessor if any errors
 * are encountered while processing an Utterance.
 */
@SuppressWarnings("serial")
public class ProcessException extends Exception {

    /**
     * Constructs a new object.
     *
     * @param message the reason why the exception was thrown
     */
    public ProcessException(String message) {
        super(message);
    }

    /**
     * Constructs a new object.
     *
     * @param message the reason why the exception was thrown
     * @param cause   the root cause of this exception
     */
    public ProcessException(String message, Throwable cause) {
        super(message, cause);
    }
}
