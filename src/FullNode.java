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


public class FullNode extends MessageSender implements FullNodeInterface {

    public StartingNode startingNode;

    public String nodeName;
    public byte[] hashID;
    public String ipAddress;
    public int portNumber;
    public final String emailAddress = "artem.korniienko@city.ac.uk";
    private static int counter = 0; // For naming purposes
    public float maxSupportedVersion = 1;
    public boolean connectionAccepted = false;

    Socket socket;
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
        super.nodeName = nodeName;
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
            if (ipAddress.equals(startingNode.getStartingNodeIpAddress()) && portNumber == startingNode.getStartingNodePortNumber()) {
                System.out.println("Opened starting node on port " + portNumber);
                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected!");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

                    String message = reader.readLine();
                    if (message != null && message.startsWith("START")) {
                        System.out.println("Starting node accepted connection.");
                        writer.write(sendStartMessage());
                        writer.flush();
                        handleRequest(reader, writer);
                    } else {
                        throw new RuntimeException("Incorrect communication initialization.");
                    }

                    clientSocket.close();
                }
            } else {
                System.out.println("This is not the starting node. Connecting to starting node...");
                Socket socket = new Socket(startingNode.getStartingNodeIpAddress(), startingNode.getStartingNodePortNumber());

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Writer writer = new OutputStreamWriter(socket.getOutputStream());

                writer.write(sendStartMessage() + "\n");
                writer.flush();

                String response = reader.readLine();
                if (response != null && response.startsWith("START")) {
                    System.out.println("Connection established");
                    writer.write(sendEchoMessage() + "\n");
                    writer.flush();


                    handleRequest(reader, writer);
                } else {
                    throw new RuntimeException("Incorrect communication initialization");
                }

                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Implement this!
        //return;
    }


    public void sendEchoMessage(Socket socket) throws IOException {
        try (Writer writer = new OutputStreamWriter(socket.getOutputStream())) {
            writer.write(sendEchoMessage() + "\n");
            writer.flush();
        }
    }
    private void handleRequest(BufferedReader reader, Writer writer) throws IOException {
        String response;
        while ((response = reader.readLine()) != null) {
            if (response.startsWith("ECHO?")) {
                System.out.println("Received ECHO? request");
                writer.write(sendOhceMessage() + "\n");
                writer.flush();
            }
            else if (response.equals("OHCE"))
            {
                System.out.println("Received OHCE");
            }
            else if (response.equals("END")) {
                System.out.println("Received END request. Closing connection.");
                break;
            }
        }
    }

    public void sendEchoMessageToTheStartingNode() {
        try {
                System.out.println("Sending ECHO message to starting node...");



                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    Writer writer = new OutputStreamWriter(socket.getOutputStream());


                    writer.write("ECHO?\n");
                    writer.flush();

                    String response = reader.readLine();
                    System.out.println("Response from starting node: " + response);
                } catch (IOException e) {
                    e.printStackTrace();
                }


    }
    public String nameGenerator(String nodeInfo) {
        return emailAddress + ":" + "my-iplementation,test-full-node" + String.valueOf(counter) + "\n";
    }
}
