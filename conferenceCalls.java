import java.io.IOException;
import java.net.*;

import javax.sound.sampled.*;

public class conferenceCalls {
    // call and listen threads to multiple using MulticastSocket
    public static void main(String[] args) throws IOException {
        // call thread
        Thread call = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    call();
                } catch (LineUnavailableException | IOException e) {
                    e.printStackTrace();
                }
            }
        });
        // listen thread
        Thread listen = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    listen();
                } catch (IOException | LineUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });
        // start threads
        call.start();
        listen.start();
    }

    // call method
    public static void call() throws LineUnavailableException, IOException {
        // set up audio format
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 8000.0f;
        int channels = 2;
        int sampleSize = 16;
        boolean bigEndian = false;
        InetAddress addr;
        int port = 2169;
        AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate,
                bigEndian);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line matching " + info + " not supported.");
            return;
        }
        // set up socket
        addr = InetAddress.getByName("239.0.0.1");
        MulticastSocket socket = new MulticastSocket();
        // set up line
        TargetDataLine line = (TargetDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        byte[] data = new byte[508];
        DatagramPacket dgp;
        // send data
        // end of call
        while (true) {
            line.read(data, 0, data.length);
            dgp = new DatagramPacket(data, data.length, addr, port);
            socket.send(dgp);
        }
    }

    // listen method
    public static void listen() throws IOException, LineUnavailableException {
        // set up audio format
        AudioFormat.Encoding encoding = AudioFormat.Encoding.PCM_SIGNED;
        float rate = 8000.0f;
        int channels = 2;
        int sampleSize = 16;
        boolean bigEndian = false;
        InetAddress addr;
        int port = 2169;
        AudioFormat format = new AudioFormat(encoding, rate, sampleSize, channels, (sampleSize / 8) * channels, rate,
                bigEndian);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
        if (!AudioSystem.isLineSupported(info)) {
            System.out.println("Line matching " + info + " not supported.");
            return;
        }
        // set up socket
        addr = InetAddress.getByName("239.0.0.1");
        MulticastSocket socket = new MulticastSocket(port);
        socket.joinGroup(addr);
        // set up line
        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        line.open(format);
        line.start();
        byte[] data = new byte[508];
        DatagramPacket dgp = new DatagramPacket(data, data.length);
        // receive data
        // end of call
        while (true) {
            socket.receive(dgp);
            line.write(data, 0, data.length);
        }
    }
}
