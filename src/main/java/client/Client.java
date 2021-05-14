package client;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Client {

    private String username;
    private String password;
    private OutputStream serverOut;
    private InputStream serverIn;
    private BufferedReader bufferedReader;

    private ArrayList<UserStatusListener> userStatusListeners = new ArrayList<>();
    private ArrayList<MessageListener> messageListeners = new ArrayList<>();
    private ArrayList<ConnectRequestListener> connectRequestListeners = new ArrayList<>();

    Socket socket;

    public Client() {

    }

    public Client(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void connect() {
        try {
            socket = new Socket("localhost", 77);
            serverOut = socket.getOutputStream();
            serverIn = socket.getInputStream();
            this.bufferedReader = new BufferedReader(new InputStreamReader(serverIn));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean login() throws IOException {
        String cmd = "login " + username + " " + password + "\n";
        serverOut.write(cmd.getBytes());

        String response = bufferedReader.readLine();
        System.out.println("Response from server: " + response);

        if ("ok login".equalsIgnoreCase(response)) {
            startMessageReader();
            return true;
        } else {
            return false;
        }
    }

    public void logout() {
        String cmd = "logout " + username + "\n";
        try {
            serverOut.write(cmd.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startMessageReader() {
        Thread t = new Thread() {
            @Override
            public void run() {
                readMessageLoop();
            }
        };
        t.start();
    }

    private void readMessageLoop() {
        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] tokens = StringUtils.split(line);
                if (tokens != null && tokens.length > 0) {
                    String cmd = tokens[0];
                    if ("online".equalsIgnoreCase(cmd)) {
                        handleOnline(tokens);
                    } else if ("offline".equalsIgnoreCase(cmd)) {
                        handleOffline(tokens);
                    } else if ("message".equalsIgnoreCase(cmd)) {
                        String[] tokensMsg = StringUtils.split(line, null, 3);
                        handleMessage(tokensMsg);
                    } else if ("connect".equalsIgnoreCase(cmd)) {
                        handleConnectRequest(tokens[1]);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    public void message(String sendTo, String body) throws IOException {
        String cmd = "message " + sendTo + " " + body + "\n";
        serverOut.write(cmd.getBytes());
    }

    public void requestConnect(String username) {
        String cmd = "connect " + username;
        try {
            serverOut.write(cmd.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessage(String[] tokens) {
        String login = tokens[1];
        String body = tokens[2];
        //System.out.println(body);
        for (MessageListener messageListener : messageListeners) {
            messageListener.onMessage(login, body);
        }
    }

    private void handleConnectRequest(String username) {
        for (ConnectRequestListener connectRequestListener : connectRequestListeners) {
            connectRequestListener.acceptConnect(username);
        }
    }

    private void handleOffline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.offline(login);
        }
    }

    private void handleOnline(String[] tokens) {
        String login = tokens[1];
        for (UserStatusListener listener : userStatusListeners) {
            listener.online(login);
        }
    }

    public void addListener(UserStatusListener listener) {
        userStatusListeners.add(listener);
    }

    public void removeListener(UserStatusListener listener) {
        userStatusListeners.remove(listener);
    }

    public void addMessage(MessageListener messageListener) {
        messageListeners.add(messageListener);
    }

    public void addConnectListener(ConnectRequestListener listener) {
        connectRequestListeners.add(listener);
    }

    public void removeMessage(MessageListener messageListener) {
        messageListeners.remove(messageListener);
    }

    public String getUsername() {
        return username;
    }
}
