import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

/** 
 * Threads that handle each client
 */
public class ClientHandler implements Runnable {

    public static ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private Socket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private String username;

    /** 
     * Constructor for this handler
     * @param socket Is the socket that the client connects to
     */
    public ClientHandler(Socket socket) {
        try {
            this.socket = socket;
           
            this.oos = new ObjectOutputStream(socket.getOutputStream());
            this.ois = new ObjectInputStream(socket.getInputStream());

            // this.username = ((Message) objectInputStream.readObject()).text();//waits for message to be sent
        
        } catch (IOException e){
            closeEverything();
        } 
    }


    /** 
     * The thread listens for messages from the client and handles it
     */
    @Override
    public void run() {
        // run on every thread
        //thread waiting and sending for each message
        Message msg;

        // check username uniqueness
        outer: while (socket.isConnected()) {
            try {
                String username = (String) ois.readObject();
                for (ClientHandler handler : clientHandlers) {
                    if (handler.username.equals(username)) {
                        oos.writeObject(new String("username exists"));
                        oos.flush();
                        continue outer;
                    }
                }
                this.username = username;
                oos.writeObject(new String("username unique"));
                oos.flush();
                break;
            } catch (Exception e) {
                closeEverything();
                return;
            }
        }

        // send user list and new connection alert
        if (socket.isConnected()) {
            String[] usernames = new String[clientHandlers.size()];
            for (int i = 0; i < usernames.length; i++) {
                usernames[i] = clientHandlers.get(i).username;
            }
            try{ 
                oos.writeObject(usernames);
                oos.flush();//manual clear before it fills  
            } catch (IOException e){
                closeEverything();
                return;
            }
            clientHandlers.add(this);
            broadcast(new Message(username + " has entered the chat!", "SERVER"));
        }


        while (socket.isConnected()) {
            try{
                msg = (Message) ois.readObject();
                if (msg.text().startsWith("@")) {
                    whisper(msg);
                } else {
                    broadcast(msg);
                }
            } catch (Exception e){
                closeEverything();
                return;
            }
        }
    }

    public void broadcast(Message msg) {
        for (ClientHandler clientHandler : clientHandlers){
            try{ 
                clientHandler.oos.writeObject(msg);
                clientHandler.oos.flush();//manual clear before it fills   
            } catch (IOException e){
                closeEverything();
                return;
            }
        }
    }
  
    /** 
     * Send messages with and handles some exceptions 
     * @param msg the object to send to clients
     */
    public void whisper(Message msg) {
        ArrayList<String> usernames = new ArrayList<String>();
        String text = msg.text();
        while (text.startsWith("@")) {
            String[] parts = text.split(" ", 2);
            usernames.add(parts[0].substring(1));
            text = parts[1];
        }

        // send to users an
        String txt = " whispers to";
        String errTxt = " the following users do not exist: ";
        ArrayList<ClientHandler> handlers = new ArrayList<ClientHandler>();
        outer: for (String name : usernames) {
            for (ClientHandler handler : clientHandlers) {
                if (handler.username.equals(name)) {
                    handlers.add(handler);
                    txt += " " + name;
                    continue outer;
                }
            }
            errTxt += " " + name;
        }
        txt += ": " + text;
        if (usernames.size() > handlers.size()) {
            try {
                oos.writeObject(new Message(errTxt, "SERVER"));
                oos.flush();  
            } catch (IOException e) {
                closeEverything();
                return;
            }
        }

        if (handlers.size() > 0) {
            try {
                oos.writeObject(new Message(txt, msg.from()));
                oos.flush();
                for (ClientHandler handler : handlers) {
                    handler.oos.writeObject(new Message(txt, msg.from()));
                    handler.oos.flush();
                }
            } catch (IOException e) {
                closeEverything();
                return;
            }
        }     
    }

    /** 
     * Neatly closes sockets and input output streams
     */
    public void closeEverything() {
        removeClientHandler();
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
        } catch (IOException e){}
    }

    /** 
     * Remove client and send messages
     */
    public void removeClientHandler() {
        clientHandlers.remove(this);
        broadcast(new Message(username + " has left the chat!","SERVER"));
        System.out.println("Client Disconnected!");
    }

}
