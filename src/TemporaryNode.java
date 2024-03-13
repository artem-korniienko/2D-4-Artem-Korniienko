// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE


import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.Socket;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode implements TemporaryNodeInterface {
    public StartingNode startingNode;
    public String nodeName;
    public byte[] hashID;

    public final String emailAddress = "artem.korniienko@city.ac.uk";
    private static int counter = 0; // For naming purposes

    public boolean start(String startingNodeName, String startingNodeAddress) {

        // Create name for current node
        nodeName = nameGenerator("");
        // Handling exceptions for absence of newline characters
        try {
            hashID = HashID.computeHashID(startingNodeName);
        } catch (Exception e) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup of ip address and port number of starting node
        if (Validator.isValidAddress(startingNodeAddress)) {
            startingNode.setStartingNodeIpAddress(startingNodeAddress.split(":")[0]);
            startingNode.setStartingNodePortNumber(Integer.parseInt(startingNodeAddress.split(":")[1]));
        }

        try {

            InetAddress host = InetAddress.getByName(startingNode.getStartingNodeIpAddress());

            int port = startingNode.getStartingNodePortNumber();

            System.out.println("TCPClient connecting to " + host.toString() + ":" + port);
            Socket clientSocket = new Socket(host, port);

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());

            System.out.println("Sending a message to the server");
            writer.write("Hello Server!\n");
            writer.flush();

            String response = reader.readLine();
            System.out.println("The server said : " + response);

            // Close down the connection
            clientSocket.close();
        }
        catch (Exception e){

        }
	// Implement this!
	// Return true if the 2D#4 network can be contacted
	// Return false if the 2D#4 network can't be contacted
	return true;
    }

    public boolean store(String key, String value) {
	// Implement this!
	// Return true if the store worked
	// Return false if the store failed
	return true;
    }

    public String get(String key) {
	// Implement this!
	// Return the string if the get worked
	// Return null if it didn't
	return "Not implemented";
    }

    public String nameGenerator(String nodeInfo) {
        return emailAddress + ":" + "my-iplementation,test-full-node" + String.valueOf(counter) + "\n";
    }
}
