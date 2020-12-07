import com.sun.security.ntlm.Server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPServer {

    // args: serverRouter IP, serverRouter Port, destination IP
    public static void main(String[] args) throws IOException {

        // Variables for setting up connection and communication
        Socket Socket = null; // socket to connect with ServerRouter
        Socket client = null;
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
        String fromServer; // messages sent to ServerRouter
        String fromClient; // messages received from ServerRouter
        String destinationAddress = args[2]; // destination IP (Client)

        // Communication process (initial sends/receives)
        toRouter.println(destinationAddress); // initial send to router (IP of the destination Client)
        fromClient = fromRouter.readLine(); // initial receive from router (verification of connection)

        System.out.println("ServerRouter: " + fromClient);
        toRouter.println("Server on " + Socket.getInetAddress().getHostAddress() + ":" + Socket.getPort()); // Client sends the IP of its machine as initial send

        if (fromRouter.readLine().equals("Establishing new P2P connection.")) {
            Socket.close();
            ServerSocket serverSocket = new ServerSocket(5550);
            client = serverSocket.accept();
            toRouter = new PrintWriter(client.getOutputStream(), true);
            fromRouter = new BufferedReader(new InputStreamReader(client.getInputStream()));
            toRouter.println("Connection established.");
        }

        // Communication while loop
        while ((fromClient = fromRouter.readLine()) != null) {
            System.out.println("Client said: " + fromClient);
            if (fromClient.equals("Bye.")) // exit statement
                break;

            fromServer = fromClient.toUpperCase(); // converting received message to upper case
            System.out.println("Server said: " + fromServer);
            toRouter.println(fromServer); // sending the converted message back to the Client via ServerRouter
        }

        // closing connections
        toRouter.close();
        fromRouter.close();
        if (client != null) {
            client.close();
        }
        Socket.close();
    }
}
