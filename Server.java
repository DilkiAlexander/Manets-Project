import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private static final int PORT = 12345;
    private static List<ClientHandler> clientHandlers = new ArrayList<>();

    public static void main(String[] args) {
        try {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server started. Listening on port " + PORT);

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected: " + clientSocket);

                    // Create a new thread to handle the client
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandlers.add(clientHandler);
                    Thread clientThread = new Thread(clientHandler);
                    clientThread.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void removeClientHandler(ClientHandler clientHandler) {
        clientHandlers.remove(clientHandler);
    }

    public static void broadcastMessage(String message) {
        for (ClientHandler handler : clientHandlers) {
            handler.sendMessage(message);
        }
    }
}
