// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE


// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress);
}
// DO NOT EDIT ends


public class FullNode implements FullNodeInterface {

    public String startingNodeName;
    public byte[] startingNodeHashID;
    public String startingNodeIpAddress;
    public int startingNodePortNumber;


    public String nodeName;
    public byte[] hashID;
    public String ipAddress;
    public int portNumber;
    public final String emailAddress = "artem.korniienko@city.ac.uk";
    private static int counter = 0; // For naming purposes

    public boolean listen(String ipAddress, int portNumber) {

        this.ipAddress = ipAddress;
        this.portNumber = portNumber;

        // Implement this!
        // Return true if the node can accept incoming connections
        // Return false otherwise
        return true;
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {

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
            this.startingNodeName = startingNodeName.endsWith("\n") ? startingNodeName : startingNodeName + "\n"; // everything that is hashed should end with newline character
        else
            throw new RuntimeException("Incorrect name");

        // Handling exceptions for absence of newline characters
        try {
            startingNodeHashID = HashID.computeHashID(startingNodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Setup of ip address and port number of starting node
        if (Validator.isValidAddress(startingNodeAddress)) {
            startingNodeIpAddress = startingNodeIpAddress.split(":")[0];
            startingNodePortNumber = Integer.parseInt(startingNodeIpAddress.split(":")[1]);
        }




        counter++; // For naming purposes

        // Implement this!
        return;
    }

    public String nameGenerator(String nodeInfo) {
        return emailAddress + ":" + "my-iplementation,test-full-node" + String.valueOf(counter) + "\n";
    }
}
