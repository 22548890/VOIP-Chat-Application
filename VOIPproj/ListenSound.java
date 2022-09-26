package VOIPproj;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.*;

public class ListenSound {

    public void listen() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        // listen sound saved
        File file = new File("test.wav");
        AudioInputStream stream = AudioSystem.getAudioInputStream(file);
        AudioFormat format = stream.getFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        int numBytesRead = 0;
        byte[] abData = new byte[128000];
        while (numBytesRead != -1) {
            numBytesRead = stream.read(abData, 0, abData.length);
            if (numBytesRead >= 0) {
                int nBytesWritten = line.write(abData, 0, numBytesRead);
            }
        }
        line.drain();
        line.close();
    }
}
