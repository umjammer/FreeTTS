/**
 * Copyright 2001 Sun Microsystems, Inc.
 * <p>
 * See the file "license.terms" for information on usage and
 * redistribution of this file, and for a DISCLAIMER OF ALL
 * WARRANTIES.
 */

package demo.util;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


/**
 * A bare-bones Server containing a ServerSocket waiting for connection
 * requests. Subclasses should implement the <code>spawnProtocolHandler</code>
 * method.
 */
public abstract class TTSServer implements Runnable {

    /**
     * The port number to listen on. It is the value specified by the
     * System property "port".
     */
    protected int port = Integer.parseInt(System.getProperty("port", String.valueOf(2222)));

    /**
     * Implements the run() method of Runnable interface. It starts a
     * ServerSocket, listens for connections, and spawns a handler for
     * each connection.
     */
    @Override
    public void run() {
        ServerSocket ss;

        try {
            ss = new ServerSocket(port);
            System.out.println("Waiting on " + ss);
        } catch (IOException ioe) {
            System.out.println("Can't open socket on port " + port);
            ioe.printStackTrace();
            return;
        }

        while (true) {
            try {
                Socket socket = ss.accept();
                System.out.println("... new socket connection");
                spawnProtocolHandler(socket);
            } catch (IOException ioe) {
                System.err.println("Could not accept socket " + ioe);
                ioe.printStackTrace();
                break;
            }
        }

        try {
            ss.close();
        } catch (IOException ioe) {
            System.err.println("Could not close server socket " + ioe);
            ioe.printStackTrace();
        }
    }

    /**
     * This method is called after a connection request is made to this
     * TTSServer. The <code>Socket</code> created as a result of the
     * connection request is passed to this method.
     *
     * @param socket the socket
     */
    protected abstract void spawnProtocolHandler(Socket socket);
}
