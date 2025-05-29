import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientGUI extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;
    private Socket socket;
    private BufferedReader reader;
    private BufferedWriter writer;
    private String username;

    public ClientGUI(String username) {
        this.username = username;
        setTitle(username + "'s Chat");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(chatArea);
        add(scrollPane, BorderLayout.CENTER);

        JPanel messagePanel = new JPanel(new BorderLayout());
        messageField = new JTextField();
        messagePanel.add(messageField, BorderLayout.CENTER);

        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        messagePanel.add(sendButton, BorderLayout.EAST);

        add(messagePanel, BorderLayout.SOUTH);

        connectToServer();

        setVisible(true);
    }

    private void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                writer.write(username + " has disconnected.\n");
                writer.flush();
                socket.close();
                reader.close();
                writer.close();
                System.out.println("Disconnected from server.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @Override
    public void dispose() {
        disconnect();
        super.dispose();
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 1234);
            System.out.println("Connected to server.");

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

            startListening();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startListening() {
        new Thread(() -> {
            try {
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Server message: " + message);
                    String[] parts = message.split(": ");
                    if (parts.length == 2) {
                        String sender = parts[0];
                        String msg = parts[1];
                        if (!sender.equals(username)) {
                            appendMessage(sender + ": " + msg);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void sendMessage() {
        try {
            String message = messageField.getText();
            writer.write(username + ": " + message + "\n");
            writer.flush();
            appendMessage("You: " + message + " (sent)");
            messageField.setText("");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void appendMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String[] parts = message.split(": ");
            if (parts.length == 2) {
                String sender = parts[0];
                String msg = parts[1];
                if (sender.equals(username)) {
                    chatArea.append("You: " + msg + " (sent)\n");
                } else {
                    chatArea.append(sender + ": " + msg + "\n");
                }
            } else {
                chatArea.append(message + "\n");
            }
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ClientGUI user1 = new ClientGUI("User1");
            ClientGUI user2 = new ClientGUI("User2");
            user1.setTitle("User1's Chat");
            user2.setTitle("User2's Chat");
        });
    }
}
