package server;

public class ServerMain {

    public static void main(String[] args) {
        int port = 77;
        Server server = new Server(port);
        server.start();
    }

}
