package tools.arcticToFreeTTS;

import java.io.PrintStream;


public class Frame {

    public float pitchmarkTime;
    public int[] parameters;

    public Frame() {
    }

    public Frame(float pitchmarkTime, int[] parameters) {
        this.pitchmarkTime = pitchmarkTime;
        this.parameters = parameters;
    }

    /**
     * Dumps the ASCII form of this Frame.
     */
    public void dumpData(PrintStream out) {
        out.print("FRAME");
        for (int parameter : parameters) {
            out.print(" " + parameter);
        }
        out.println();
        out.println("RESIDUAL 0");
    }
}
