import java.io.Serializable;

/**
 * Object used to send messages to and from server
 */
public class Message implements Serializable {
    private String text, from;

    /**
     * Constructor for broadcast
     * 
     * @param text The actual text to send
     * @param from Where it is sent from
     */
    public Message(String text, String from) {
        this.text = text;
        this.from = from;
    }

    /**
     * @return the text
     */
    public String text() {
        return text;
    }

    /**
     * @return the from
     */
    public String from() {
        return from;
    }

    public void setText(String msg) {
        this.text = msg;
    }
}
