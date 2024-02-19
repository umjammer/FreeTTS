/**
 * Copyright 2007 JVoiceXML group
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.freetts.rtp;

import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import javax.media.ControllerClosedEvent;
import javax.media.ControllerErrorEvent;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.MediaLocator;
import javax.media.NoPlayerException;
import javax.media.Player;
import javax.media.RealizeCompleteEvent;


/**
 * Simple program to play back an RTP audio stream.
 *
 * @author Dirk Schnelle
 */
public class RtpClientDemo implements ControllerListener {

    /** Logger instance. */
    private static final Logger logger = System.getLogger(RtpClientDemo.class.getName());

    @Override
    public void controllerUpdate(ControllerEvent control) {
        Player player = (Player) control.getSourceController();
        // If player wasn't created successfully from controller, return
        if (player == null) {
            System.out.println("Player is null");
            return;
        }

        if (control instanceof RealizeCompleteEvent) {
            System.out.println("Starting player...");
            player.start();
        }

        if (control instanceof ControllerClosedEvent) {
            logger.log(Level.INFO, "controller closed");
            System.exit(0);
        }

        if (control instanceof ControllerErrorEvent) {
            System.out.println("Error in ControllerErrorEvent: " + control);
            player.removeControllerListener(this);
            System.exit(0);
        }
    }

    /**
     * Starts the program
     *
     * @param args none expected
     * @see "mvn -P demo antrun:run@rtp-client"
     */
    public static void main(String[] args) {
        RtpClientDemo client = new RtpClientDemo();

        MediaLocator loc = new MediaLocator("rtp://127.0.0.1:49150/audio/1");
        Player player;
        try {
            player = javax.media.Manager.createPlayer(loc);
        } catch (NoPlayerException | IOException e) {
            logger.log(Level.ERROR, e.getMessage(), e);
            return;
        }
        player.addControllerListener(client);

        player.realize();

        System.out.println("waiting for data...");
    }
}
