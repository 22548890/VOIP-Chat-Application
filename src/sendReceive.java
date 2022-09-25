import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

public class sendReceive {
    public static void main(String[] args) throws IOException {
        File file = new File("rec.wav");
        // convert to base64 String
        byte[] byteData = Files.readAllBytes(Paths.get("test.wav"));
        String encodedString = Base64.getEncoder().encodeToString(byteData);
        // print size of encoded string in bytes
        System.out.println(encodedString);

        // convert back to file
        byte[] decodedBytes = Base64.getDecoder().decode(encodedString);
        Files.write(Paths.get("rec.wav"), decodedBytes);

    }

}
