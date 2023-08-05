import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;
    public static void main(String[] args) {
        try {
            Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            System.out.print("Enter your name: ");
            String clientName = reader.readLine();
            writer.println(clientName);

            // Start a thread to read messages from the server
            Thread readThread = new Thread(() -> {
                try {
                    String message;
                    while ((message = serverReader.readLine()) != null) {
                        System.out.println(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            readThread.start();

            // Read and send messages to the server
            String message;
            while (true) {
                message = reader.readLine();
                writer.println(message);
                if (message.equalsIgnoreCase("exit")) {
                    socket.close();
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

   

    
}
