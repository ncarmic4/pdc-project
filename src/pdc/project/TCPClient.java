package pdc.project;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;

public class TCPClient {

    // args: serverRouter IP, serverRouter Port, destination IP
    public static void main(String[] args) throws IOException, InterruptedException {

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
        }

        /* COMMUNICATION */
        OutputStream outputStream = server.getOutputStream();
        InputStream inputStream = server.getInputStream();

        for (int i = 0; i < Util.imageNames.length; i++) {
            String[] type = Util.imageNames[i].split(".");
            Util.sendImage("images/" + Util.imageNames[i], type[1], outputStream);
            Thread.sleep(5000);
            ImageIO.write(Util.receiveImage(inputStream), type[1], new File("images/" + type[0] + "-cropped." + type[1]));
        }

        inputStream.close();
        outputStream.close();

        // closing connections
        toRouter.close();
        fromRouter.close();
        server.close();
        Socket.close();
    }
}
