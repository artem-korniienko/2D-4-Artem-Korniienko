// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {

    public StartingNode startingNode;

    public String nodeName;
    public byte[] hashID;
    public String ipAddress;
    public int portNumber;
    public final String emailAddress = "artem.korniienko@city.ac.uk";
    private static int counter = 0; // For naming purposes

    ServerSocket serverSocket;

    public boolean listen(String ipAddress, int portNumber) {

        this.ipAddress = ipAddress;
        this.portNumber = portNumber;
        try {
            serverSocket = new ServerSocket(portNumber);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        // Implement this!
        // Return true if the node can accept incoming connections
        // Return false otherwise;
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {

        // Create name for current node
        nodeName = nameGenerator("");
        // Handling exceptions for absence of newline characters
        try {
            hashID = HashID.computeHashID(startingNodeName);
        } catch (Exception e)
        {
            e.printStackTrace();
        }



        // Setup fields with information about starting node
        if (Validator.isValidName(startingNodeName)) //Check weather input name is correct
            startingNode = new StartingNode(startingNodeName.endsWith("\n") ? startingNodeName : startingNodeName + "\n"); // everything that is hashed should end with newline character)
        else
            throw new RuntimeException("Incorrect name");

        // Handling exceptions for absence of newline characters
        try {
            startingNode.setStartingNodeHashID(HashID.computeHashID(startingNodeName));
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        // Setup of ip address and port number of starting node
        if (Validator.isValidAddress(startingNodeAddress)) {
            startingNode.setStartingNodeIpAddress(startingNodeAddress.split(":")[0]);
            startingNode.setStartingNodePortNumber(Integer.parseInt(startingNodeAddress.split(":")[1]));
        }

        counter++; // For naming purposes

        try {
            InetAddress startingNodeHost = InetAddress.getByName(startingNode.getStartingNodeIpAddress());
            int startingNodePort = startingNode.getStartingNodePortNumber();
            if (ipAddress.equals(startingNodeHost.getHostAddress()) && portNumber == startingNodePort) {
                System.out.println("Opened starting node on port " + portNumber);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected!");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

                    String message = reader.readLine();
                    System.out.println("The client said : " + message);

                    System.out.println("Sending a message to the client");
                    writer.write("Nice to meet you\n");
                    writer.flush();

                    clientSocket.close();
                }
            } else {
                System.out.println("This is not the starting node. Connecting to starting node...");
                Socket socket = new Socket(startingNodeHost, startingNodePort);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Writer writer = new OutputStreamWriter(socket.getOutputStream());

                writer.write("Hello starting node\n");
                writer.flush();

                String response = reader.readLine();
                System.out.println("Response from starting node: " + response);

                socket.close();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }






        // Implement this!
        return;
    }

    public String nameGenerator(String nodeInfo) {
        return emailAddress + ":" + "my-iplementation,test-full-node" + String.valueOf(counter) + "\n";
    }
}
