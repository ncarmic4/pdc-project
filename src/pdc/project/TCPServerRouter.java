package pdc.project;

import java.io.IOException;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

class RoutingEntry implements Serializable {
    String address;
    int port;
    int routerId;

    public RoutingEntry(String address, int port, int routerId) {
        this.address = address;
        this.port = port;
        this.routerId = routerId;
    }

    public String getConnectionString() {
        return address + ":" + port;
    }
}

public class TCPServerRouter {

    // args: ID (0 or 1), port num, otherRouterAddress (host:port)
    public static void main(String[] args) throws IOException {

        ArrayList<RoutingEntry> routingTable = new ArrayList<>();
        ArrayList<Socket> sockets = new ArrayList<>();

        int id = Integer.parseInt(args[0]); // 0 or 1
        int socketNum = Integer.parseInt(args[1]); // port number
        String[] otherRouterAddr = args[2].split(":");

        Socket clientSocket; // socket for the thread
        int index = 0; // index in the routing table

        //Accepting connections
        ServerSocket serverSocket = null; // server socket for accepting connections
        try {
            serverSocket = new ServerSocket(socketNum);
            System.out.println("ServerRouter " + id + " is Listening on port: " + socketNum);
        } catch (IOException e) {
            System.err.println("ServerRouter " + id + " could not listen on port: " + socketNum);
            System.exit(1);
        }

        // Get the other router in a socket object to be accessed by SThreads
        Socket otherRouter;
        if (id == 0) {
            otherRouter = serverSocket.accept();
        } else if (id == 1) {
            otherRouter = new Socket(otherRouterAddr[0], Integer.parseInt(otherRouterAddr[1]));
        } else {
            throw new IllegalArgumentException("ID must be 0 or 1.");
        }
        System.out.println("Registered another server router: " + otherRouter.getInetAddress().getHostAddress());

        // Creating threads with accepted connections
        while (true) {
            try {
                clientSocket = serverSocket.accept();
                SThread t = new SThread(routingTable, sockets, otherRouter, clientSocket, index, id); // creates a thread with a random port
                t.start(); // starts the thread
                index++; // increments the index
                System.out.println("ServerRouter connected with Client/Server: " + clientSocket.getInetAddress().getHostAddress());
            } catch (IOException e) {
                System.err.println("Client/Server failed to connect.");
                System.exit(1);
            }
        }
    }
}