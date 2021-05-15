package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Server extends Thread {

    //private static Server _instance = null;
    private int port;
    private String address = "localhost";
    private ArrayList<ServerWorker> workers = new ArrayList<>();

    private Server() {

    }

    public Server(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Server is ready...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from " + clientSocket);
                ServerWorker worker = new ServerWorker(this, clientSocket);
                workers.add(worker);
                worker.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void removeWorker(ServerWorker worker) {
        this.workers.remove(worker);
    }

    public List<ServerWorker> getWorkerList() {
        return workers;
    }

    public boolean isLogged(String username) {
        for (ServerWorker serverWorker : workers) {
            if (username.equalsIgnoreCase(serverWorker.getUsername())) {
                return true;
            }
        }
        return false;
    }

}
