package tools.arcticToFreeTTS;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;


public class STSFrame extends Frame {

    public int[] residuals;

    public STSFrame(int numChannels, BufferedReader reader)
            throws IOException {
        String line = reader.readLine();
        pitchmarkTime = Float.parseFloat(line);

        parameters = new int[numChannels];

        line = reader.readLine();
        StringTokenizer tokenizer = new StringTokenizer(line);
        for (int i = 0; i < numChannels; i++) {
            parameters[i] = Integer.parseInt(tokenizer.nextToken());
        }

        line = reader.readLine();
        tokenizer = new StringTokenizer(line);
        residuals = new int[Integer.parseInt(tokenizer.nextToken())];
        for (int i = 0; i < residuals.length; i++) {
            residuals[i] = Integer.parseInt(tokenizer.nextToken());
        }
    }

    /**
     * Dumps the ASCII form of this Frame.
     */
    @Override
    public void dumpData(PrintStream out) {
        out.print("FRAME");
        for (int parameter : parameters) {
            out.print(" " + parameter);
        }
        out.println();
        out.print("RESIDUAL " + residuals.length);
        for (int residual : residuals) {
            out.print(" " + residual);
        }
        out.println();
    }
}
