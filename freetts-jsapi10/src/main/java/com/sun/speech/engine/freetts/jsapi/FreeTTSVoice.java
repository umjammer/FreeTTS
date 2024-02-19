/**
 * Copyright 2003 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package com.sun.speech.engine.freetts.jsapi;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.lang.reflect.InvocationTargetException;

import com.sun.speech.engine.synthesis.BaseVoice;
import com.sun.speech.freetts.ValidationException;
import com.sun.speech.freetts.Validator;

import static java.lang.System.getLogger;


/**
 * Extends the BaseVoice class to encapsulate FreeTTSSynthesizer specific data.
 */
public class FreeTTSVoice extends BaseVoice {

    private static final Logger logger = getLogger(FreeTTSVoice.class.getName());

    private com.sun.speech.freetts.Voice freettsVoice;
    private Validator validator;

    /**
     * Constructs a FreeTTSVoice
     *
     * @param freettsVoice  the freetts voice
     * @param validatorName the classname of the validator to use
     */
    public FreeTTSVoice(com.sun.speech.freetts.Voice freettsVoice, String validatorName) {
        super(freettsVoice.getName() + Math.random(), freettsVoice.getName(),
                genderToInt(freettsVoice.getGender()),
                ageToInt(freettsVoice.getAge()), freettsVoice.getStyle(),
                freettsVoice.getPitch(), freettsVoice.getPitchRange(),
                freettsVoice.getRate(), freettsVoice.getVolume());
        this.freettsVoice = freettsVoice;

        if (validatorName != null) {
            try {
                Class<?> clazz = Class.forName(validatorName);
                validator = (Validator) clazz.getDeclaredConstructor().newInstance();
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException cnfe) {
                logger.log(Level.ERROR, cnfe.getMessage(), cnfe);
            }
        } else {
            validator = null;
        }
    }

    /**
     * Convert a freetts gender to jsapi gender
     *
     * @param gender the freetts gender
     * @return the jsapi gender
     */
    private static int genderToInt(com.sun.speech.freetts.Gender gender) {
        if (gender == com.sun.speech.freetts.Gender.MALE) {
            return GENDER_MALE;
        } else if (gender == com.sun.speech.freetts.Gender.FEMALE) {
            return GENDER_FEMALE;
        } else if (gender == com.sun.speech.freetts.Gender.NEUTRAL) {
            return GENDER_NEUTRAL;
        } else if (gender == com.sun.speech.freetts.Gender.DONT_CARE) {
            return GENDER_DONT_CARE;
        } else {
            throw new Error("jaspi does not have an equivalent to gender " + gender.toString());
        }
    }

    /**
     * Convert a freetts age to jsapi age
     *
     * @param age the freetts age
     * @return the jsapi age
     */
    private static int ageToInt(com.sun.speech.freetts.Age age) {
        if (age == com.sun.speech.freetts.Age.CHILD) {
            return AGE_CHILD;
        } else if (age == com.sun.speech.freetts.Age.TEENAGER) {
            return AGE_TEENAGER;
        } else if (age == com.sun.speech.freetts.Age.YOUNGER_ADULT) {
            return AGE_YOUNGER_ADULT;
        } else if (age == com.sun.speech.freetts.Age.MIDDLE_ADULT) {
            return AGE_MIDDLE_ADULT;
        } else if (age == com.sun.speech.freetts.Age.OLDER_ADULT) {
            return AGE_OLDER_ADULT;
        } else if (age == com.sun.speech.freetts.Age.NEUTRAL) {
            return AGE_NEUTRAL;
        } else if (age == com.sun.speech.freetts.Age.DONT_CARE) {
            return AGE_DONT_CARE;
        } else {
            throw new Error("jaspi does not have an equivalent to age " + age.toString());
        }
    }

    /**
     * Gets the id for this voice.
     * Should be unique for a synthesizer.
     *
     * @return the voice id
     */
    @Override
    public String getId() {
        return voiceId;
    }

    /**
     * Gets a string representation of the object
     *
     * @return the name of this voice
     */
    public String toString() {
        return getName();
    }

    /**
     * Gets a FreeTTS com.sun.speech.freetts.Voice from this JSAPI voice
     *
     * @return a FreeTTS Voice or null, if the voice cannot be found
     */
    public synchronized com.sun.speech.freetts.Voice getVoice() {
        return freettsVoice;
    }

    /**
     * Sets the id for this voice.
     *
     * @param id the new id
     */
    @Override
    public void setId(String id) {
        voiceId = id;
    }

    /**
     * Creates a copy of this <code>BaseVoice</code>.
     *
     * @return the cloned object
     */
    @Override
    public Object clone() {
        return super.clone();
    }

    /**
     * Validates this FreeTTSVoice.
     *
     * @throws ValidationException if this FreeTTSVoice is invalid
     */
    public void validate() throws ValidationException {
        if (validator != null) {
            validator.validate();
        }
    }
}

