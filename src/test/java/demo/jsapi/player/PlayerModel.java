/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.jsapi.player;

import javax.speech.synthesis.SynthesizerModeDesc;
import javax.swing.ListModel;


/**
 * Defines the data model used by the GUI of the <code>Player</code>.
 * Defines ways to control volume, speaking rate, pitch and range, etc.
 * Also gives you information such as the list of <code>Synthesizers</code>, 
 * list of <code>Voices</code>, the play list, etc., that the user interface
 * will need. Also allows you to get and play different types of
 * <code>Playable</code> objects.
 */
public interface PlayerModel {

    /**
     * Performs text-to-speech on the given Playable.
     *
     * @param Playable the Playable to perform text-to-speech
     */
    void play(Playable Playable);

    /**
     * Performs text-to-speech on the object at the given index of
     * the play list.
     *
     * @param index the index of the Playable object on the play list
     */
    void play(int index);

    /**
     * Returns true if the player is paused.
     *
     * @return <code>true</code> if the player is paused,
     *         <code>false</code> otherwise
     */
    boolean isPaused();

    /**
     * Pauses the player.
     */
    void pause();

    /**
     * Resumes the player.
     */
    void resume();

    /**
     * Stops the player if it is playing.
     */
    void stop();

    /**
     * Cancels the currently playing item.
     */
    void cancel();

    /**
     * Sets whether the Monitor is visible.
     *
     * @param visible true to set it to visible
     */
    void setMonitorVisible(boolean visible);

    /**
     * Tells whether the monitor is visible.
     *
     * @return true if the monitor is visible, false otherwise
     */
    boolean isMonitorVisible();

    /**
     * Creates the list of synthesizers.
     */
    void createSynthesizers();

    /**
     * Returns the monitor of the synthesizer at the given index.
     *
     * @param index the position of the synthesizer in the synthesizer list
     *
     * @return the monitor of the specified synthesizer
     */
    Monitor getMonitor(int index);

    /**
     * Returns the monitor of the current synthesizer.
     *
     * @return the monitor of the current synthesizer
     */
    Monitor getMonitor();

    /**
     * Sets the current monitor.
     *
     * @param monitor the current monitor
     */
    void setMonitor(Monitor monitor);

    /**
     * Sets the Synthesizer at the given index to use
     *
     * @param index index of the synthesizer in the list
     */
    void setSynthesizer(int index);

    /**
     * Sets the Voice at the given index to use.
     *
     * @param index the index of the voice in the list
     */
    void setVoice(int index);

    /**
     * Sets the list of voices using the given Synthesizer mode description.
     *
     * @param modeDesc the synthesizer mode description
     */
    void setVoiceList(SynthesizerModeDesc modeDesc);

    /**
     * Returns the volume.
     *
     * @return the volume, or -1 if unknown, or an error occurred
     */
    float getVolume();

    /**
     * Sets the volume.
     *
     * @param volume set the volume of the synthesizer
     *
     * @return true if new volume is set; false otherwise
     */
    boolean setVolume(float volume);

    /**
     * Returns the speaking rate.
     *
     * @return the speaking rate, or -1 if unknown or an error occurred
     */
    float getSpeakingRate();

    /**
     * Sets the speaking rate in the number of words per minute.
     *
     * @param wordsPerMin the speaking rate
     *
     * @return true if new speaking rate is set; false otherwise
     */
    boolean setSpeakingRate(float wordsPerMin);

    /**
     * Returns the baseline pitch for the current synthesis voice.
     *
     * @return the baseline pitch for the current synthesis voice
     */
    float getPitch();

    /**
     * Sets the baseline pitch for the current synthesis voice.
     *
     * @param pitch the baseline pitch
     *
     * @return true if new pitch is set; false otherwise
     */
    boolean setPitch(float pitch);

    /**
     * Returns the pitch range for the current synthesis voice.
     *
     * @return the pitch range for the current synthesis voice
     */
    float getRange();

    /**
     * Sets the pitch range for the current synthesis voice.
     *
     * @param range the pitch range
     *
     * @return true if new range is set; false otherwise
     */
    boolean setRange(float range);

    /**
     * Returns the play list.
     *
     * @return the play list
     */
    ListModel<Playable> getPlayList();

    /**
     * Returns the list of voices of the current synthesizer
     *
     * @return the list of voices
     */
    ListModel<Object> getVoiceList();

    /**
     * Returns the list synthesizers.
     *
     * @return the synthesizer list
     */
    ListModel<Object> getSynthesizerList();

    /**
     * Returns the Playable object at the given index of the play list.
     *
     * @param index the index of the Playable object on the play list
     *
     * @return the Playable object
     */
    Object getPlayableAt(int index);

    /**
     * Adds the given Playable object to the end of the play list.
     *
     * @param Playable the Playable object to add
     */
    void addPlayable(Playable Playable);

    /**
     * Removes the Playable at the given position from the list
     *
     * @param index the index of the Playable to remove
     */
    void removePlayableAt(int index);

    /**
     * Closes the PlayerModel
     */
    void close();
}
