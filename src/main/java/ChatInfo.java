import client.Client;

import javax.swing.*;

public class ChatInfo {

    private Client client;
    private DefaultListModel model;
    private String username;

    public ChatInfo() {

    }

    public ChatInfo(Client client, String username) {
        this.client = client;
        this.username = username;
        model = new DefaultListModel();
    }

    public String getUsername() {
        return username;
    }

    public DefaultListModel getModel() {
        return model;
    }

    public void setModel(DefaultListModel model) {
        this.model = model;
    }

}
