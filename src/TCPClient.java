import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

    // args: serverRouter IP, serverRouter Port, destination IP
    public static void main(String[] args) throws IOException {

        // Variables for setting up connection and communication
        Socket Socket = null; // socket to connect with ServerRouter
        Socket server = null;
        PrintWriter toRouter = null; // for writing to ServerRouter
        BufferedReader fromRouter = null; // for reading form ServerRouter

        String routerAddress = args[0]; // ServerRouter host name
        int routerSocket = Integer.parseInt(args[1]); // port number

        // Tries to connect to the ServerRouter
        try {
            Socket = new Socket(routerAddress, routerSocket);
            toRouter = new PrintWriter(Socket.getOutputStream(), true);
            fromRouter = new BufferedReader(new InputStreamReader(Socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Don't know about router: " + routerAddress);
            System.exit(1);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to: " + routerAddress);
            System.exit(1);
        }

        // Variables for message passing
        String fromServer; // messages received from ServerRouter
        String fromUser; // messages sent to ServerRouter
        String destinationAddress = args[2]; // destination IP (Server)
        long t0, t1, t;

        // Communication process (initial sends/receives
        toRouter.println(destinationAddress);// initial send to router (IP of the destination Server)
        fromServer = fromRouter.readLine();//initial receive from router (verification of connection)

        System.out.println("ServerRouter: " + fromServer);
        toRouter.println("Client on " + Socket.getInetAddress().getHostAddress() + ":" + Socket.getPort()); // Client sends the IP of its machine as initial send

        String status = fromRouter.readLine();
        if (status.equals("Establishing new P2P connection.")) {
            Socket.close();
            server = new Socket(destinationAddress, 5550);
            toRouter = new PrintWriter(server.getOutputStream(), true);
            fromRouter = new BufferedReader(new InputStreamReader(server.getInputStream()));
            toRouter.println("Connection established.");
        }

        t0 = System.currentTimeMillis();

        // Communication while loop
        while ((fromServer = fromRouter.readLine()) != null) {
            System.out.println("ServerRouter: " + fromServer);
            t1 = System.currentTimeMillis();
            if (fromServer.equals("Bye.")) // exit statement
                break;
            t = t1 - t0;
            System.out.println("Cycle time: " + t);
            fromUser = "Hello World";

            System.out.println("Client: " + fromUser);
            toRouter.println(fromUser); // sending the strings to the Server via ServerRouter
            t0 = System.currentTimeMillis();
        }

        // closing connections
        toRouter.close();
        fromRouter.close();
        if (server != null) {
            server.close();
        }
        Socket.close();
    }
}
