//major project
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private ServerSocket serverSocket;
    private List<ClientHandler> clients = new ArrayList<>();
    private boolean running = true;

    public Server() {
        try {
            serverSocket = new ServerSocket(1234);
            System.out.println("Server started. Waiting for clients to connect...");

            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void broadcastMessage(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    public List<ClientHandler> getClients() {
        return clients;
    }

    public void stopServer() {
    try {
        running = false;
        serverSocket.close();
        serverSocket = null;
        for (ClientHandler client : clients) {
            client.closeConnection();
        }
        clients.clear();
    } catch (IOException e) {
        e.printStackTrace();
    }
}

    

public static void main(String[] args) {
    Server server = new Server();

    // Add a shutdown hook to stop the server when the application is terminated
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
        server.stopServer();
    }));
}

}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private Server server;

    public ClientHandler(Socket clientSocket, Server server) {
        this.clientSocket = clientSocket;
        this.server = server;
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        try {
            writer.write(message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
    try {
        String message;
        while ((message = reader.readLine()) != null) {
            System.out.println("Client message: " + message);
            server.broadcastMessage(message, this);
        }
    } catch (IOException e) {
        // Client has disconnected
        System.out.println("Client disconnected: " + clientSocket.getInetAddress());
    } finally {
        // Remove client from the list
        server.getClients().remove(this);
        closeConnection();
    }
}

}

//press ctrl+c to disconnect the server
