import java.io.*;
import java.net.*;
import java.util.*;
import javax.sound.sampled.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class test {
    // record audio with gui button
    private static JButton button;

    private static JFrame frame = new JFrame("Record Audio");
    private static TargetDataLine line = null;

    public static void main(String[] args) throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame.setSize(200, 100);
        frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        button = new JButton("Record");
        button.setBackground(Color.GREEN);
        button.setForeground(Color.BLACK);
        button.setPreferredSize(new Dimension(140, 75));
        frame.add(button);
        frame.setVisible(true);

        // click to record and to stop
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (button.getText().equals("Record")) {
                    button.setText("Stop");
                    button.setBackground(Color.RED);
                    // record audio using sound API
                    AudioFormat format = new AudioFormat(16000, 8, 1, true, true);
                    DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);

                    try {
                        line = (TargetDataLine) AudioSystem.getLine(info);
                        System.out.println("Recording...");
                        line.open(format);
                        line.start();
                    } catch (LineUnavailableException e1) {
                        e1.printStackTrace();
                    }

                    Thread stopper = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            AudioInputStream stream = new AudioInputStream(line);
                            File file = new File("test.wav");
                            try {
                                AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    stopper.start();

                } else {
                    button.setText("Record");
                    button.setBackground(Color.GREEN);
                    System.out.println("Stopped");
                    line.stop();
                    line.close();
                }
            }

        });

    }
}
