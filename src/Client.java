import java.io.*;
import java.net.DatagramPacket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

public class Client implements ActionListener {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String username;
    private String msg = "";
    private JButton btnVN;
    private JButton btnSend;
    private JFrame frame;
    private JTextArea enteredText;
    private JTextField typedText;
    private DefaultListModel<String> listModelUsers;
    private JList<String> usersList;
    private DefaultListModel<String> listModelRooms;
    private JList<String> roomsList;
    private static TargetDataLine line = null;
    private boolean bRecord = false;

    /**
     * Performs actions regarding the GUI
     * 
     * @param e for the action performed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        // get and send text from typedText.getText()
        if (e.getSource() == btnSend) {
            msg = typedText.getText();
            sendMessage(msg);
            typedText.setText("");
            // IF BTNvN PRESSED

        } else {
            if (bRecord == false) {
                bRecord = true;
                try {
                    Image img = ImageIO.read(getClass().getResource("/2.png"));
                    btnVN.setIcon(new ImageIcon(img));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                // record audio using sound API
                AudioFormat format = new AudioFormat(8000, 8, 2, true, true);
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
                        File file = new File("group_42\\test.wav");
                        try {
                            AudioSystem.write(stream, AudioFileFormat.Type.WAVE, file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                stopper.start();
                typedText.requestFocusInWindow();

            } else {
                // set boolean false
                bRecord = false;
                try {
                    Image img = ImageIO.read(getClass().getResource("rec.png"));
                    btnVN.setIcon(new ImageIcon(img));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                System.out.println("Stopped");
                line.stop();
                line.close();
                sendMessage("/vn");

                // try {
                // sendFile("group_42\\test.wav");
                // } catch (IOException e1) {
                // e1.printStackTrace();
                // }
            }
        }
        typedText.requestFocusInWindow();

    }

    private void sendFile(String filePath) throws IOException {

    }

    /**
     * Constructor forPerforms actions regarding the GUI
     * 
     * @param e for the action performed
     */
    public Client(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, String username) {
        this.socket = socket;
        this.ois = ois;
        this.oos = oos;
        this.username = username;

        frame = new JFrame();

        // create button with image record.png
        btnVN = new JButton();
        btnVN.addActionListener(this);
        try {
            Image img = ImageIO.read(getClass().getResource("rec.png"));
            btnVN.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            System.out.println(ex);
        }
        btnSend = new JButton();
        try {
            Image img = ImageIO.read(getClass().getResource("/send.png"));
            btnSend.setIcon(new ImageIcon(img));
        } catch (Exception ex) {
            System.out.println(ex);
        }

        btnSend.addActionListener(this);
        enteredText = new JTextArea(10, 32);
        typedText = new JTextField(32);

        listModelUsers = new DefaultListModel<String>();
        listModelUsers.addElement("Online Users:");
        listModelRooms = new DefaultListModel<String>();
        listModelRooms.addElement("Rooms:   ");

        usersList = new JList<String>(listModelUsers);
        roomsList = new JList<String>(listModelRooms);

        enteredText.setEditable(false);
        btnVN.setFocusable(false);
        usersList.setFocusable(false);
        roomsList.setFocusable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(this);

        Container content = frame.getContentPane();
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        // content.add(typedText, BorderLayout.SOUTH);
        // add button next to typedText
        content.add(usersList, BorderLayout.EAST);
        content.add(roomsList, BorderLayout.WEST);

        // set size of button and textarea
        btnVN.setPreferredSize(new Dimension(50, 50));
        btnSend.setPreferredSize(new Dimension(50, 50));
        enteredText.setPreferredSize(new Dimension(300, 50));

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        panel.add(typedText);
        panel.add(btnSend);
        panel.add(btnVN);
        typedText.requestFocusInWindow();
        content.add(panel, BorderLayout.SOUTH);
        // frame.add(button, BorderLayout.SOUTH);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        frame.setTitle("Client: " + username);
    }

    /**
     * Handles messages typed in input area
     * 
     * @param text the string type in input
     */
    public void sendMessage(String text) {
        try {
            Message msg = null;
            if (text.startsWith("/vn")) {
                System.out.println("Sending voice note");
                msg = new Message("/vn" + voiceFileString(), "m");
            } else {
                if (text.startsWith("/exit")) {
                    closeEverything();
                } else {
                    msg = new Message(text, username);
                }
            }
            oos.writeObject(msg);
            oos.flush();
        } catch (IOException e) {
            closeEverything();
        }
    }

    private String voiceFileString() {
        byte[] byteData;
        String encodedString = null;
        try {
            byteData = Files.readAllBytes(Paths.get("group_42\\test.wav"));
            encodedString = Base64.getEncoder().encodeToString(byteData);
        } catch (IOException e) {
            System.out.println("Error reading file");
        }

        // print size of encoded string in bytes
        // System.out.println(encodedString.length());

        // convert back to file
        // byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        // Files.write(Paths.get("rec.wav"), decodedBytes);
        return encodedString;
    }

    /**
     * Creates the thread that listens for messages
     */
    public void listenForMessage() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(username, socket, ois, oos, enteredText,
                listModelUsers, listModelRooms);
        Thread thread = new Thread(clientListenerThread);
        thread.start(); // waiting for msgs
    }

    /**
     * Closes socket and streams neatly
     */
    public void closeEverything() {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    /**
     * Closes socket and streams neatly
     */
    public static void closeEverything(ObjectInputStream ois, ObjectOutputStream oos, Socket socket) {
        try {
            if (ois != null) {
                ois.close();
            }
            if (oos != null) {
                oos.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        int port = 12345;
        Socket socket = null;

        String ip = "";
        while (ip.isBlank()) {
            ip = JOptionPane.showInputDialog("Enter the IP address: ", "localhost");
        }

        try {
            socket = new Socket(ip, port);
        } catch (UnknownHostException e) {
            System.out.println("ERROR: Unknown host");
            System.exit(0);
        } catch (IOException e) {
            System.out.println("ERROR: Couldn't get the connection to " + ip);
            System.exit(0);
        }

        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            closeEverything(ois, oos, socket);
        }

        String username = "";
        while (true) {
            username = JOptionPane.showInputDialog("Enter your unique username: ");
            if (username.isBlank() || !username.matches("^[0-9A-Za-z]*$") || username.equals("SERVER")) {
                continue;
            }
            try {
                oos.writeObject(new String(username));
                oos.flush();

                String resp = (String) ois.readObject();
                if (resp.equals("username unique")) {
                    break;
                }
            } catch (Exception e) {
                closeEverything(ois, oos, socket);
            }
        }

        Client client = new Client(socket, ois, oos, username);

        client.listenForMessage();
        // client listen for file transfer

    }
}