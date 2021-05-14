package server;

import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.net.Socket;
import java.util.List;

public class ServerWorker extends Thread {

    private Server server;
    private Socket clientSocket;
    private String username = null;
    private OutputStream outputStream;

    public ServerWorker(Server server, Socket clientSocket) {
        this.server = server;
        this.clientSocket = clientSocket;
    }

    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleClientSocket() throws IOException {
        InputStream inputStream = clientSocket.getInputStream();
        this.outputStream = clientSocket.getOutputStream();

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] token = StringUtils.split(line);

            if (token.length > 0) {
                String cmd = token[0];
                if ("login".equalsIgnoreCase(cmd)) {
                    System.out.println("successful");
                    handleLogin(token);
                } else if ("logout".equalsIgnoreCase(cmd)) {
                    handleLogout(token);
                } else if ("message".equalsIgnoreCase(cmd)) {
                    String[] tokensMsg = StringUtils.split(line, null, 3);
                    handleMessage(tokensMsg);
                } else if ("connect".equalsIgnoreCase(cmd)) {
                    System.out.println(token[1]);
                    handleConnectRequest(token);
                } else {
                    String msg = "unknown " + cmd + "\n";
                    outputStream.write(msg.getBytes());
                }
            }
        }
        clientSocket.close();
    }

    public String getUsername() {
        return username;
    }

    private void handleMessage(String[] tokensMsg) throws IOException {
        String sendTo = tokensMsg[1];
        String body = tokensMsg[2];
        List<ServerWorker> workers = server.getWorkerList();
        for (ServerWorker worker : workers) {
            if (sendTo.equalsIgnoreCase(worker.getUsername())) {
                System.out.println(username);
                String msg = "message " + username + ": " + body + "\n";
                worker.send(msg);
            }
        }
    }

    private void handleConnectRequest(String[] token) throws IOException {
        String sendTo = token[1];

        List<ServerWorker> workers = server.getWorkerList();

        for (ServerWorker worker : workers) {
            if (sendTo.equalsIgnoreCase(worker.getUsername())) {
                String msg = "connect " + username + "\n";
                worker.send(msg);
            }
        }

    }

    private void handleLogin( String[] token) throws IOException {
        if (token.length == 3) {
            String login = token[1];
            String password = token[2];
            String msg;
            if (!server.isLogged(login)) {

                msg = "ok login\n";
                outputStream.write(msg.getBytes());
                this.username = login;
                System.out.println("User logged in successfully: " + login);

                List<ServerWorker> workers = server.getWorkerList();

                // send current user all other online logins
                for (ServerWorker worker : workers) {
                    if (worker.getUsername() != null) {
                        if (!login.equals(worker.getUsername())) {
                            System.out.println(worker.getUsername());
                            String message = "online " + worker.getUsername() + "\n";
                            send(message);
                        }
                    }
                }

                // send other online users current user's status;
                String onlMsg = "online " + login + "\n";
                for (ServerWorker worker : workers) {
                    if (!login.equals(worker.getUsername())) {
                        worker.send(onlMsg);
                    }
                }
            } else {
                msg = "error login: you're already logged in!\n";
                outputStream.write(msg.getBytes());
                System.err.println(login + " has been logged in!");
            }
        }
    }

    private void handleLogout( String[] token) throws IOException {
        if (token.length == 2) {
            String logout = token[1];
            String msg;
            if (true) {
                msg = "ok logout\n";
                outputStream.write(msg.getBytes());
                System.out.println("User logged out successfully: " + logout);

                List<ServerWorker> workers = server.getWorkerList();

                // send other online users current user's status;
                String onlMsg = "offline " + logout + "\n";
                for (ServerWorker worker : workers) {
                    if (!logout.equals(worker.getUsername())) {
                        worker.send(onlMsg);
                    }
                }
                server.removeWorker(this);

            } else {
                msg = "error login\n";
                outputStream.write(msg.getBytes());
                System.err.println("Logout failed for " + logout);
            }
        }
    }

    private void send(String onlMsg) throws IOException {
        if (username != null) {
            outputStream.write(onlMsg.getBytes());
        }
    }

}
