import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class ReceiverThread implements Runnable {

    static AudioInputStream ais;
    static AudioFormat format;
    static int port = 43215;

    static DataLine.Info dataLineInfo;
    static SourceDataLine sourceDataLine;


    @Override
    public void run() {
        System.out.println("Receiving call ...");

        try {
            InetAddress address = InetAddress.getByName("localhost");
            DatagramSocket socket = new DatagramSocket(port);

            byte[] data = new byte[512];

            format = new AudioFormat(44100, 16, 2, true, true);

            dataLineInfo = new DataLine.Info(TargetDataLine.class, format);

            sourceDataLine = (SourceDataLine) AudioSystem.getSourceDataLine(format);
            sourceDataLine.open(format);
            sourceDataLine.start();

            DatagramPacket packet = new DatagramPacket(data, data.length);
            ByteArrayInputStream bais = new ByteArrayInputStream(packet.getData());

            while (Client.endCall() != true) {
                socket.receive(packet);
                ais = new AudioInputStream(bais, format, packet.getLength());

                // System.out.println("Listening ...");
                sourceDataLine.write(packet.getData(), 0, packet.getData().length);
            }
            // call ended
            System.out.println("Call ended");
            sourceDataLine.drain();
            sourceDataLine.close();
            socket.close();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        }
    }

}
