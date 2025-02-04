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

package com.sun.speech.freetts.diphone;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.sun.speech.freetts.relp.Sample;


/**
 * Represents two adjacent phones. A diphone is defined by its name,
 * the set of audio data, and information used to help stitch diphones
 * together. This class is immutable.
 */
public class Diphone {

    protected final static int MAGIC = 0xFACE0FF;
    protected final static int ALIAS_MAGIC = 0xBABAF00;
    protected final static int NAME_LENGTH = 8;
    private String name;
    private int midPoint;
    private Sample[] samples;
    private int unitSizePart1;
    private int unitSizePart2;

    /**
     * Creates a diphone with the given name, samples and midpoint.
     *
     * @param name     the name of the diphone
     * @param samples  the set of samples for the diphone
     * @param midPoint the index of the sample midpoint
     */
    public Diphone(String name, Sample[] samples, int midPoint) {
        this.name = name;
        this.midPoint = midPoint;
        this.samples = samples;
        this.unitSizePart1 = 0;
        this.unitSizePart2 = 0;

        for (int i = 0; i < midPoint; i++) {
            unitSizePart1 += samples[i].getResidualSize();
        }
        for (int i = midPoint; i < samples.length; i++) {
            unitSizePart2 += samples[i].getResidualSize();
        }
    }

    /**
     * Constructor to be used only by subclasses who do not use the
     * variables except for the name
     *
     * @param name the name of the diphone
     */
    protected Diphone(String name) {
        this.name = name;
        this.midPoint = 0;
        this.samples = null;
        this.unitSizePart1 = 0;
        this.unitSizePart2 = 0;
    }

    /**
     * Returns the samples associated with this diphone.
     *
     * @return the samples associated with this diphone
     */
    public Sample[] getSamples() {
        return samples;
    }

    /**
     * Returns a particular sample.
     *
     * @param which which sample to return
     * @return the desired sample
     */
    public Sample getSamples(int which) {
        return samples[which];
    }

    /**
     * Gets the name of the diphone.
     *
     * @return the name of the diphone
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the midpoint index. the midpoint index is the sample
     * that divides the diphone into the first and second parts.
     *
     * @return the midpoint index.
     */
    public int getMidPoint() {
        return midPoint;
    }

    /**
     * Returns the midpoint index. the midpoint index is the sample
     * that divides the diphone into the first and second parts.
     *
     * @return the midpoint index.
     */
    public int getPbPositionMillis() {
        return getMidPoint();
    }

    /**
     * Returns the sample that is closest to uIndex.
     *
     * @param uIndex   the desired index
     * @param unitPart do we want the first have (1) or the second
     *                 half (2)
     * @return the sample nearest to the given index in the given
     * part
     */
    public Sample nearestSample(float uIndex, int unitPart) {
        int i, iSize = 0, nSize;
        // loop through all the Samples in this Diphone
        int start = (unitPart == 1) ? 0 : midPoint;
        int end = (unitPart == 1) ? midPoint : samples.length;

        for (i = start; i < end; i++) {
            nSize = iSize + samples[i].getResidualSize();

            if (Math.abs(uIndex - (float) iSize) <
                    Math.abs(uIndex - (float) nSize)) {
                return samples[i];
            }
            iSize = nSize;
        }
        return samples[end - 1];
    }

    /**
     * Returns the total number of residuals in the given part for this
     * diphone.
     *
     * @param unitPart indicates which part is of interest (1 or 2)
     * @return the number of residuals in the specified part
     */
    public int getUnitSize(int unitPart) {
        if (unitPart == 1) {
            return unitSizePart1;
        } else {
            return unitSizePart2;
        }
    }

    /**
     * dumps out this Diphone.
     */
    public void dump() {
        System.out.println("Diphone: " + name);
        System.out.println("    MP : " + midPoint);
        for (Sample sample : samples) {
            sample.dump();
        }
    }

    /**
     * Dumps the diphone to the given channel.
     *
     * @param bb the ByteBuffer to write to
     * @throws IOException if IO error occurs
     */
    public void dumpBinary(ByteBuffer bb) throws IOException {
        char[] nameArray = (name + "        ").toCharArray();

        bb.putInt(MAGIC);
        for (int i = 0; i < NAME_LENGTH; i++) {
            bb.putChar(nameArray[i]);
        }
        bb.putInt(midPoint);
        bb.putInt(samples.length);

        for (Sample sample : samples) {
            sample.dumpBinary(bb);
        }
    }

    /**
     * Dumps the diphone to the given channel.
     *
     * @param os the DataOutputStream to write to
     * @throws IOException if IO error occurs
     */
    public void dumpBinary(DataOutputStream os) throws IOException {
        char[] nameArray = (name + "        ").toCharArray();

        os.writeInt(MAGIC);
        for (int i = 0; i < NAME_LENGTH; i++) {
            os.writeChar(nameArray[i]);
        }
        os.writeInt(midPoint);
        os.writeInt(samples.length);

        for (Sample sample : samples) {
            sample.dumpBinary(os);
        }
    }

    /**
     * Determines if the two diphones are equivalent. This is for
     * testing databases. This is not the same as "equals"
     *
     * @param other the diphone to compare this one to
     * @return <code>true</code> if the diphones match; otherwise
     * <code>false</code>
     */
    boolean compare(Diphone other) {
        if (!name.equals(other.getName())) {
            return false;
        }

        if (midPoint != other.getMidPoint()) {
            return false;
        }

        if (samples.length != other.getSamples().length) {
            return false;
        }

        for (int i = 0; i < samples.length; i++) {
            if (!samples[i].compare(other.getSamples(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Loads a new diphone from  the given buffer.
     *
     * @param bb the byte buffer to load the diphone from
     * @return the new diphone
     * @throws IOException if IO error occurs
     */
    public static Diphone loadBinary(ByteBuffer bb) throws IOException {
        StringBuilder sb = new StringBuilder();
        int midPoint;
        int numSamples;
        Sample[] samples;

        int magic = bb.getInt();
        if (magic == ALIAS_MAGIC) {
            for (int i = 0; i < NAME_LENGTH; i++) {
                char c = bb.getChar();
                if (!Character.isWhitespace(c)) {
                    sb.append(c);
                }
            }
            String name = sb.toString().trim();
            sb.setLength(0);
            for (int i = 0; i < NAME_LENGTH; i++) {
                char c = bb.getChar();
                if (!Character.isWhitespace(c)) {
                    sb.append(c);
                }
            }
            String origName = sb.toString().trim();
            return new AliasDiphone(name, origName);
        } else if (magic != MAGIC) {
            throw new Error("Bad magic number in diphone");
        }

        for (int i = 0; i < NAME_LENGTH; i++) {
            char c = bb.getChar();
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }

        midPoint = bb.getInt();
        numSamples = bb.getInt();

        samples = new Sample[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = Sample.loadBinary(bb);
        }
        return new Diphone(sb.toString().trim(), samples, midPoint);
    }

    /**
     * Loads a new  diphone from  the given DataInputStream.
     *
     * @param dis the datainput stream to load the diphone from
     * @return the new diphone
     * @throws IOException if IO error occurs
     */
    public static Diphone loadBinary(DataInputStream dis) throws IOException {
        StringBuilder sb = new StringBuilder();
        int midPoint;
        int numSamples;
        Sample[] samples;

        int magic = dis.readInt();
        if (magic == ALIAS_MAGIC) {
            for (int i = 0; i < NAME_LENGTH; i++) {
                char c = dis.readChar();
                if (!Character.isWhitespace(c)) {
                    sb.append(c);
                }
            }
            String name = sb.toString().trim();
            sb.setLength(0);
            for (int i = 0; i < NAME_LENGTH; i++) {
                char c = dis.readChar();
                if (!Character.isWhitespace(c)) {
                    sb.append(c);
                }
            }
            String origName = sb.toString().trim();
            return new AliasDiphone(name, origName);
        } else if (magic != MAGIC) {
            throw new Error("Bad magic number in diphone");
        }

        for (int i = 0; i < NAME_LENGTH; i++) {
            char c = dis.readChar();
            if (!Character.isWhitespace(c)) {
                sb.append(c);
            }
        }

        midPoint = dis.readInt();
        numSamples = dis.readInt();

        samples = new Sample[numSamples];
        for (int i = 0; i < numSamples; i++) {
            samples[i] = Sample.loadBinary(dis);
        }
        return new Diphone(sb.toString().trim(), samples, midPoint);
    }
}

