import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class CallerThread implements Runnable {

    private String calleeIP;

    public CallerThread(String calleeIP) {
        this.calleeIP = calleeIP;
    }

    @Override
    public void run() {

        ReceiverThread receiver = new ReceiverThread();
        Thread thread = new Thread(receiver);
        thread.start();

        TargetDataLine line;
        DatagramPacket packet;

        InetAddress address;
        int port = 43215;

        AudioFormat format = new AudioFormat(44100, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            System.out.println("Calling ...");
            line.open(format);
            line.start();

            byte[] data = new byte[512];

            address = InetAddress.getByName(calleeIP);
            // address = InetAddress.getByName("25.86.115.11");
            DatagramSocket socket = new DatagramSocket();
            while (Client.endCall() != true) { // ends call both sides
                line.read(data, 0, data.length);
                packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
            }
            // call ended
            System.out.println("Call ended");
            line.drain();
            line.close();
            socket.close();

        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
