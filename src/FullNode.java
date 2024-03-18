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
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// DO NOT EDIT starts
interface FullNodeInterface {
    public boolean listen(String ipAddress, int portNumber);
    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) throws Exception;
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

    public HashMap<Integer, List<String>> networkMap;
    public HashMap<String, String> map = new HashMap<>();

    Socket socket;
    ServerSocket serverSocket;


    public FullNode() {
        Random ran = new Random();
        counter = ran.nextInt(129048);
        this.nodeName = nameGenerator("-");
        super.nodeName = this.nodeName;
    }

    public static int getCounter() {
        return counter;
    }

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

        // Setup fields with information about starting node
        if (Validator.isValidName(startingNodeName))
            startingNode = new StartingNode(startingNodeName.endsWith("\n") ? startingNodeName : startingNodeName + "\n");
        else
            throw new RuntimeException("Incorrect name");

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



        networkMap = new HashMap<>();

        try {
            addMapElement(nodeName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                System.out.println("Listening for incoming connections...");
                if (!isStartingNode()) { // If not the starting node, connect to the previous node
                    connectToStartingNode();
                }

                try {
                    Socket clientSocket = serverSocket.accept(); // Accept incoming connections
                    System.out.println("Client connected!");
                    handleClientConnection(clientSocket); // Handle each client connection in a separate thread
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isStartingNode() {
        return ipAddress.equals(startingNode.getStartingNodeIpAddress()) && portNumber == startingNode.getStartingNodePortNumber();
    }

    private void connectToStartingNode() {
        try {
            Socket socket = new Socket(startingNode.getStartingNodeIpAddress(), startingNode.getStartingNodePortNumber());

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Writer writer = new OutputStreamWriter(socket.getOutputStream());

            writer.write(sendStartMessage() + "\n");
            writer.flush();

            String response = reader.readLine();
            if (response != null && response.startsWith("START")) {
                System.out.println("Connection established with previous node");
            } else {
                throw new RuntimeException("Failed to connect to previous node");
            }

            // Close the socket after all tasks are executed
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleClientConnection(Socket clientSocket) throws Exception {
        Thread thread = new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
                writer.write(sendStartMessage());
                writer.flush();
                String message = reader.readLine();
                String[] returnStartMessage = message.split(" ");
                if (returnStartMessage[0].equals("START")
                        && returnStartMessage[1].equals(String.valueOf(this.maxSupportedVersion))) {
                    System.out.println("Node accepted connection.");
                    try {
                        addMapElement(returnStartMessage[2]);
                        handleRequest(reader, writer);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                } else {
                    throw new RuntimeException("Incorrect communication initialization.");
                }
            } catch (IOException | RuntimeException e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    public synchronized void addMapElement(String addedNodeName) throws Exception {
        int distance = calculateDistance(addedNodeName);
        List<String> nodeList = networkMap.getOrDefault(distance, new ArrayList<>());

        if (nodeList.size() >= 3) {
            // Remove the oldest node
            nodeList.remove(0);
        }
        String nodeWithAddress = addedNodeName + " NN:NN";
        if (addedNodeName.equals(this.nodeName)) {
            nodeWithAddress =  addedNodeName + " " + ipAddress + ":" + portNumber;
        }
        if (!nodeList.contains(nodeWithAddress)) {
            nodeList.add(nodeWithAddress);
            networkMap.put(distance, nodeList);
        }
    }
    public synchronized void addMapElement(String addedNodeName, String addedNodeAddress) throws Exception {
        int distance = calculateDistance(addedNodeName);
        List<String> nodeList = networkMap.getOrDefault(distance, new ArrayList<>());

        if (nodeList.size() >= 3) {
            // Remove the oldest node
            nodeList.remove(0);
        }
        String nodeWithAddress = addedNodeName + " " + addedNodeAddress;
        if (addedNodeName.equals(this.nodeName)) {
            nodeWithAddress =  addedNodeName + " " + ipAddress + ":" + portNumber;
        }
        if (!nodeList.contains(nodeWithAddress)) {
            nodeList.add(nodeWithAddress);
            networkMap.put(distance, nodeList);
        }
    }
    public int calculateDistance(String secondNodeName) throws Exception
    {
        return HashID.distance(HashID.computeHashID(nodeName), HashID.computeHashID(secondNodeName));
    }

    public void sendEchoMessage(Socket socket) throws IOException {
        try (Writer writer = new OutputStreamWriter(socket.getOutputStream())) {
            writer.write(sendEchoMessage() + "\n");
            writer.flush();
        }
    }
    public void sendPutMessage(Socket socket) throws IOException {
        try (Writer writer = new OutputStreamWriter(socket.getOutputStream())) {
            writer.write("PUT? 1 1");
            writer.flush();
        }
    }
    private void handleRequest(BufferedReader reader, Writer writer) throws Exception {
        String response;
        while ((response = reader.readLine()) != null) {
            if (response.equals("ECHO?")) {
                System.out.println("Received ECHO? request");
                writer.write(sendOhceMessage() + "\n");
                writer.flush();
            } else if (response.equals("OHCE")) {
                System.out.println("Received OHCE");
            } else if (response.equals("SHOWMAP?"))
            {
                writer.write(getNetworkMap());
                writer.flush();
            } else if (response.startsWith("NEAREST?")) {
                String[] parts = response.split(" ");
                if (parts.length == 2) {
                    HashMap<String, Integer> nearest = findThreeNearestNodes(parts[1]);
                    writer.write(sendNodesMessage(nearest));
                    writer.flush();
                }
                else {
                    writer.write(sendEndMessage("Incorrect NEAREST? request use"));
                    writer.flush();
                }
            } else if (response.equals("NOTIFY?")){

                boolean contains = false;
                StringBuilder nameBuilder = new StringBuilder();
                StringBuilder addressBuilder = new StringBuilder();
                StringBuilder newAddress = new StringBuilder();
                for (int i = 0; i < 1; i++) {
                    nameBuilder.append(reader.readLine());
                }
                for (int i = 0; i < 1; i++) {
                    addressBuilder.append(reader.readLine());
                }

                for (Map.Entry<Integer, List<String>> entry : networkMap.entrySet()) {
                    List<String> nodeList = entry.getValue();
                    for (int i = 0; i < nodeList.size(); i++) {
                        String nameAddress = nodeList.get(i);
                        if (nameAddress.contains(nameBuilder.toString())) {
                            String[] addressArray = nameAddress.split(" ");
                            addressArray[1] = addressBuilder.toString().trim();
                            newAddress.append(addressArray[0]).append(" ").append(addressArray[1]);
                            nodeList.set(i, newAddress.toString());
                            contains = true;
                            break;
                        }
                    }
                }
                if (!contains && Validator.isValidName(nameBuilder.toString()) && Validator.isValidAddress(addressBuilder.toString()))
                {
                    addMapElement(nameBuilder.toString());
                    for (Map.Entry<Integer, List<String>> entry : networkMap.entrySet()) {
                        List<String> nodeList = entry.getValue();
                        for (int i = 0; i < nodeList.size(); i++) {
                            String nameAddress = nodeList.get(i);
                            if (nameAddress.contains(nameBuilder.toString())) {
                                String[] addressArray = nameAddress.split(" ");
                                addressArray[1] = addressBuilder.toString().trim();
                                newAddress.append(addressArray[0]).append(" ").append(addressArray[1]);
                                nodeList.set(i, newAddress.toString());
                                contains = true;
                                break;
                            }
                        }
                    }
                }
                writer.write(sendNotifiedMessage());
                writer.flush();
            } else if (response.contains("GET?")) {
                String[] parts = response.split(" ");
                boolean contains = false;

                if (parts.length == 2) {
                    int keyLines = Integer.parseInt(parts[1]);

                    StringBuilder keyBuilder = new StringBuilder();
                    for (int i = 0; i < keyLines; i++) {
                        keyBuilder.append(reader.readLine()).append("\n");
                    }

                    byte[] requestHASHID = HashID.computeHashID(keyBuilder.toString());

                    String value = "";

                    for (Map.Entry<String, String> entry : map.entrySet())
                    {
                        if (entry.getKey().equals(HashID.bytesToHex(requestHASHID))) {
                            contains = true;
                            value = entry.getValue();
                        }
                    }
                    if (contains) {
                        writer.write(sendValueMessage(calculateNewLineCharacrter(value), value));
                        writer.flush();
                    }
                    else {
                        writer.write(sendNopeMessage());
                        writer.flush();
                    }

                }
                else {
                    writer.write(sendEndMessage("Incorrect GET? request use"));
                    writer.flush();
                }
            }

            else if (response.startsWith("PUT?")) {
                String[] parts = response.split(" ");
                boolean shouldContain = false;
                if (parts.length == 3) {
                    int keyLines = Integer.parseInt(parts[1]);
                    int valueLines = Integer.parseInt(parts[2]);

                    StringBuilder keyBuilder = new StringBuilder();
                    StringBuilder valueBuilder = new StringBuilder();
                    for (int i = 0; i < keyLines; i++) {
                        keyBuilder.append(reader.readLine()).append("\n");
                    }
                    for (int i = 0; i < valueLines; i++) {
                        valueBuilder.append(reader.readLine()).append("\n");
                    }
                    byte[] requestHASHID = HashID.computeHashID(keyBuilder.toString());
                    HashMap<String, Integer> threeNearestNodes = findThreeNearestNodes(requestHASHID);
                    for (Map.Entry<String, Integer> entry : threeNearestNodes.entrySet())
                    {
                        if (entry.getKey().contains(nodeName))
                            shouldContain = true;
                    }
                    if (shouldContain)
                    {
                        map.put(HashID.bytesToHex(requestHASHID), valueBuilder.toString());
                        writer.write(sendSuccessMessage());
                        writer.flush();
                        for (Map.Entry<String, String> entry : map.entrySet())
                        {
                            System.out.println(entry.getKey() + ":" + entry.getValue());
                        }
                    }
                    else
                    {
                        writer.write(sendFailedMessage());
                        writer.flush();
                    }
                } else {
                    System.out.println("Invalid PUT request format");
                    // Respond with an error message
                    writer.write("Invalid PUT request format\n");
                    writer.flush();
                }
            } else if (response.contains("END")) {
                System.out.println("Received END request. Closing connection.");
                writer.write(sendEndMessage("Recieved END request"));
                writer.flush();
                break;
            }
            else {
                writer.write(sendEndMessage("Incorrect Command"));
                writer.flush();
                break;
            }
        }
    }
    private int calculateNewLineCharacrter(String string)
    {
        int newLineCharacterCounter = 0;
        char[] stringArray = string.toCharArray();
        for (int i = 0; i < string.length(); i++) {
            if (stringArray[i] == '\n')
                newLineCharacterCounter++;
        }
        return newLineCharacterCounter;
    }
    public String getNetworkMap() {
        StringBuilder result = new StringBuilder();

        result.append("Nodes connected to ").append(nodeName).append(":\n");
        for (Map.Entry<Integer, List<String>> entry : networkMap.entrySet()) {
            List<String> nodeList = entry.getValue();
            for (String node : nodeList) {
                if (!node.equals(nodeName)) {
                    result.append(node).append("\n");
                }
            }
        }

        result.append("Whole Map:\n");
        for (Map.Entry<Integer, List<String>> entry : networkMap.entrySet()) {
            Integer key = entry.getKey();
            List<String> values = entry.getValue();

            result.append("Key: " + key + ", Values: ");
            if (values.isEmpty()) {
                result.append("[]");
            } else {
                result.append("[");
                for (int i = 0; i < values.size() - 1; i++) {
                    result.append(values.get(i) + ", ");
                }
                result.append(values.get(values.size() - 1) + "]" + "\n");
            }
        }

        return result.toString();
    }
    HashMap<String, Integer> findThreeNearestNodes(String hashID) throws Exception
    {
        return findThreeNearestNodes(HashID.hexToBytes(hashID));
    }
    HashMap<String, Integer> findThreeNearestNodes(byte[] hashID) throws Exception
    {
        HashMap<String, Integer> nearestNodes = new HashMap<>();
        TreeMap<Integer, List<String>> sortedMap = new TreeMap<>();

        // Calculate distances and store them in sorted map
        for (Map.Entry<Integer, List<String>> entry : networkMap.entrySet()) {
            int distance = entry.getKey();
            for (String node : entry.getValue()) {
                if (!node.contains("NN:NN")) { // Filter out nodes that don`t have address
                    int nodeDistance = HashID.distance(hashID, HashID.computeHashID(node));
                    sortedMap.putIfAbsent(nodeDistance, new ArrayList<>());
                    sortedMap.get(nodeDistance).add(node);
                }
            }
        }

        // Iterate over sorted distances to find the nearest nodes
        for (Map.Entry<Integer, List<String>> entry : sortedMap.entrySet()) {
            for (String node : entry.getValue()) {
                if (nearestNodes.size() < 3) {
                    nearestNodes.put(node, entry.getKey());
                } else {
                    return nearestNodes;
                }
            }
        }

        return nearestNodes;
    }
    public String nameGenerator(String nodeInfo) {
        return emailAddress + ":" + "my-implementation,test-full-node" + nodeInfo + FullNode.getCounter() + "\n";
    }
}