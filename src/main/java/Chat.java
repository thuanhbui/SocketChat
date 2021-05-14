import client.Client;
import client.ConnectRequestListener;
import client.MessageListener;
import client.UserStatusListener;
import server.ServerMain;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;

public class Chat extends JPanel implements UserStatusListener, MessageListener, ConnectRequestListener {
    public JPanel panel1;
    public JList onlineUsers;
    private JPanel screen;
    private JPanel title;
    private JList message;
    private JTextField messageField;
    private JButton sendButton;
    private JLabel nameLabel;

    private DefaultListModel model;
    private ArrayList<ChatInfo> chatInfoArrayList = new ArrayList<>();
    private Client client;

    public Chat(Client client) {
        this.client = client;
        this.client.addListener(this);
        this.client.addMessage(this);

        nameLabel.setText("Hello, " + client.getUsername());

        model = new DefaultListModel();

        onlineUsers.setModel(model);

        onlineUsers.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 1) {
                    String login = (String) onlineUsers.getSelectedValue();
                    if (login != null) {
                        if (isConnected(login)) {
                            System.out.println("connected");
                            message.setModel(getChatInfo(login).getModel());
                        } else {
                            System.out.println("not connect");
                            //getClient().requestConnect(login);
                            chatInfoArrayList.add(new ChatInfo(getClient(), login));
                            message.setModel(getChatInfo(login).getModel());
                        }
                    }
                }
            }
        });

        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String login = (String) onlineUsers.getSelectedValue();
                    String text = messageField.getText();
                    getClient().message(login, text);
                    getChatInfo(login).getModel().addElement("You: " + text);
                    messageField.setText("");
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        });

    }


    @Override
    public void online(String username) {
        model.addElement(username);
    }

    @Override
    public void offline(String username) {
        model.removeElement(username);
    }


    @Override
    public void onMessage(String fromLogin, String body) {
        String line = fromLogin + " " + body;
        for (ChatInfo chatInfo : chatInfoArrayList) {
            if (fromLogin.equalsIgnoreCase(chatInfo.getUsername() + ":")) {
                chatInfo.getModel().addElement(line);
                return;
            }
        }
        String fromLogin_ = fromLogin.substring(0, fromLogin.length() - 1);
        chatInfoArrayList.add(new ChatInfo(getClient(), fromLogin_));
        for (ChatInfo chatInfo : chatInfoArrayList) {
            if (fromLogin.equalsIgnoreCase(chatInfo.getUsername() + ":")) {
                chatInfo.getModel().addElement(line);
            }
        }
    }

    public boolean isConnected(String username) {
        for (ChatInfo chatInfo : chatInfoArrayList) {
            if (username.equalsIgnoreCase(chatInfo.getUsername())) return true;
        }
        return false;
    }

    public ChatInfo getChatInfo(String username) {
        for (ChatInfo chatInfo : chatInfoArrayList) {
            if (username.equalsIgnoreCase(chatInfo.getUsername())) return chatInfo;
        }
        return null;
    }

    public Client getClient() {
        return client;
    }

    @Override
    public void acceptConnect(String username) {
        chatInfoArrayList.add(new ChatInfo(client, username));
        message.setModel(getChatInfo(username).getModel());
    }
}
