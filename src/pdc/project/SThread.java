package pdc.project;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class SThread extends Thread {

    private ArrayList<RoutingEntry> routingTable;
    private ArrayList<Socket> sockets;
    private Socket otherRouter;
    private PrintWriter out, outTo; // writers (for writing back to the machine and to destination)
    private BufferedReader in; // reader (for reading from the machine connected to)
    private String inputLine, outputLine, destination, addr; // communication strings
    private Socket outSocket; // socket for communicating with a destination
    private int ind; // index in the routing table\

    private RoutingEntry current;
    private boolean server;

    // Constructor
    SThread(ArrayList<RoutingEntry> routingTable, ArrayList<Socket> sockets, Socket otherRouter, Socket toClient, int index, int id)
            throws IOException {
        out = new PrintWriter(toClient.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(toClient.getInputStream()));
        addr = toClient.getInetAddress().getHostAddress();
        ind = index;

        this.routingTable = routingTable;
        this.sockets = sockets;
        this.otherRouter = otherRouter;
        current = new RoutingEntry(toClient.getInetAddress().getHostAddress(), toClient.getPort(), id);
        routingTable.add(current);
        sockets.add(toClient);
    }

    // Run method (will run for each machine that connects to the ServerRouter)
    public void run() {
        try {
            // Initial sends/receives
            destination = in.readLine(); // initial read (the destination for writing)
            System.out.println("Forwarding to " + destination);
            out.println("Connected to the router."); // confirmation of connection

            // waits 10 seconds to let the routing table fill with all machines' information
            try {
                Thread.currentThread().sleep(10000);
            } catch (InterruptedException ie) {
                System.out.println("Thread interrupted");
            }

            // Determine if destination is in the associated routing table. If not,
            // check the ask the other server router for its routing table.
            // If the address exists, establish a new TCP conn
            server = in.readLine().startsWith("Server");
            int i = findAddress(routingTable, destination);
            if (i != -1) {
                out.println("Getting connection from router.");
                outSocket = sockets.get(i);
            } else {
                // send current routing table
                ObjectOutputStream objOut = new ObjectOutputStream(otherRouter.getOutputStream());
                objOut.writeObject(routingTable);

                // Ask the other router for its routing table
                ObjectInputStream objIn = new ObjectInputStream(otherRouter.getInputStream());
                ArrayList<RoutingEntry> otherRoutingTable = (ArrayList<RoutingEntry>) objIn.readObject();
                i = findAddress(otherRoutingTable, destination);
                RoutingEntry routingEntry = otherRoutingTable.get(i);

                out.println("Establishing new P2P connection.");
                if (server) {
                    out.println(current.getConnectionString());
                } else {
                    out.println(routingEntry.getConnectionString());
                }
                
                Thread.currentThread().interrupt();
            }

            System.out.println("Found destination: " + destination);
            outTo = new PrintWriter(outSocket.getOutputStream(), true); // assigns a writer

            // Communication loop
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Client/Server said: " + inputLine);
                if (inputLine.equals("Bye.")) // exit statement
                    break;
                outputLine = inputLine; // passes the input from the machine to the output string for the destination

                if (outSocket != null) {
                    outTo.println(outputLine); // writes to the destination
                }
            }// end while
        }// end try
        catch (IOException | ClassNotFoundException e) {
            System.err.println("Could not listen to socket.");
            System.exit(1);
        }
    }

    public static int findAddress(ArrayList<RoutingEntry> routingTable, String destination) {
        for (int i = 0; i < routingTable.size(); i++) {
            if (destination.equals(routingTable.get(i).address)) {
                return i;
            }
        }
        return -1;
    }
}