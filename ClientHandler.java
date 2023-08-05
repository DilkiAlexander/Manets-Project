import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class ClientHandler implements Runnable {
    private static List<ClientHandler> clients = new ArrayList<>();

    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private String clientName;

    private static SecretKeySpec secretKey;
    private static byte[] key;
    private static final String ALGORITHM = "AES";

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            clientName = reader.readLine();
            System.out.println(clientName + " connected.");

            clients.add(this);

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println(clientName + ": " + message);
                String EncyMsg=  encryptMessage(message);
                String ResultMsg= decryptMessage(EncyMsg);
                sendToAllClients(clientName + ":  Encrpt: "+EncyMsg+" Decrpt: "+ResultMsg);

                if (message.equalsIgnoreCase("server reply")) {
                    sendToClient("Server: This is a reply from the server.", this);
                }
            }

            System.out.println(clientName + " disconnected.");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clientSocket.close();
                clients.remove(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void sendToAllClients(String message) {

        for (ClientHandler client : clients) {
            if (client != this) {
                client.sendMessage(message);
            }
        }
    }

    private void sendToClient(String message, ClientHandler clientHandler) {

        clientHandler.sendMessage(message);

    
    }

    void sendMessage(String message) {
        writer.println(message);
    }

    public void prepareSecreteKey(String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String encryptMessage(String message) {
        try {
        
            String secretK = "E20C31C44E1234DFD57734B671550FE1";

            prepareSecreteKey(secretK);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
            e.printStackTrace();
            return "";
        }
    }

    private String decryptMessage(String Enmsg) {
        try {
    
            String secretK = "E20C31C44E1234DFD57734B671550FE1";
            prepareSecreteKey(secretK);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            return new String(cipher.doFinal(Base64.getDecoder().decode(Enmsg)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
            e.printStackTrace();
            return "";
        }
    }
}
