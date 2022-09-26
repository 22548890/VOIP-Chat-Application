import java.io.*;
import java.net.*;
import javax.sound.sampled.*;

public class CallerThread implements Runnable {

    @Override
    public void run() {
        TargetDataLine line;
        DatagramPacket packet;

        InetAddress address;
        int port = 43215;

        AudioFormat format = new AudioFormat(22050, 16, 2, true, true);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

        try {
            line = (TargetDataLine) AudioSystem.getLine(info);
            System.out.println("Calling ...");
            line.open(format);
            line.start();

            byte[] data = new byte[1024];

            address = InetAddress.getByName("localhost");
            DatagramSocket socket = new DatagramSocket();
            while (true) {
                line.read(data, 0, data.length);
                packet = new DatagramPacket(data, data.length, address, port);
                socket.send(packet);
            }
            
        } catch (LineUnavailableException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }  

    }
    
}
