import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class Client implements ActionListener {
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String username;
    private String msg="";

    private JFrame frame;
    private JTextArea  enteredText;
    private JTextField typedText;
    private DefaultListModel<String> listModelUsers;
    private JList<String> usersList;
    private DefaultListModel<String> listModelRooms;
    private JList<String> roomsList;


      /** 
     * Performs actions regarding the GUI
     * 
     * @param e for the action performed
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        //get and send text from typedText.getText()
        msg=typedText.getText();
        
        sendMessage(msg);

        typedText.setText("");
        typedText.requestFocusInWindow(); 
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

        JButton btn = new JButton("send");
        btn.addActionListener(this);
        
        enteredText = new JTextArea(10, 32);
        typedText   = new JTextField(32);
      
        listModelUsers = new DefaultListModel<String>();
        listModelUsers.addElement("Online Users:");
        listModelRooms = new DefaultListModel<String>();
        listModelRooms.addElement("Rooms:   ");

        usersList = new JList<String>(listModelUsers);
        roomsList = new JList<String>(listModelRooms);
        
        enteredText.setEditable(false);
        usersList.setFocusable(false);
        roomsList.setFocusable(false);
        enteredText.setBackground(Color.LIGHT_GRAY);
        typedText.addActionListener(this);

        Container content = frame.getContentPane();
        content.add(new JScrollPane(enteredText), BorderLayout.CENTER);
        content.add(typedText, BorderLayout.SOUTH);
        content.add(usersList, BorderLayout.EAST);
        content.add(roomsList, BorderLayout.WEST);
        typedText.requestFocusInWindow();

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
        try{ 
            Message msg=  null;
            if (text.startsWith("/exit")) {
                closeEverything();
            } else {
                msg = new Message(text, username);
            }
            oos.writeObject(msg);
            oos.flush();   
        }  catch (IOException e) {
            closeEverything();
        }
    }

    /** 
     * Creates the thread that listens for messages
     */
    public void listenForMessage() {
        ClientListenerThread clientListenerThread = new ClientListenerThread(username, socket, ois, oos, enteredText, listModelUsers, listModelRooms);
        Thread thread = new Thread(clientListenerThread);
        thread.start(); //waiting for msgs
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
        } catch (IOException e){
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
        }catch (IOException e){
            e.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        int port = 12345;
        Socket socket= null;
        
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
    }
}
