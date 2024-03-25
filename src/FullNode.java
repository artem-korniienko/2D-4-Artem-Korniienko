// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// YOUR_NAME_GOES_HERE
// YOUR_STUDENT_ID_NUMBER_GOES_HERE
// YOUR_EMAIL_GOES_HERE

import java.io.*;
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
    public int maxSupportedVersion = 1;
    public boolean connectionAccepted = false;

    private String currentClientName = "NN:NN";
    public HashMap<Integer, List<String>> networkMap;
    public HashMap<String, String> map = new HashMap<>();

    Socket socket;
    ServerSocket serverSocket;
    Set<String> visitedNodes = new HashSet<>();



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

        // TODO: Temperary node implementaion - DONE
        // TODO: Validatae names and inputs?  - DONE
        // TODO: Protocol version - integer - DONE
        // TODO: check my implementations regarding start package(all the types of methods that return something, fields for the name)
        // TODO: check that we have recieved NOTIFIED Correctly look line number 146 - DONE
        // TODO: ensure i delete everything if incorrect format
        // TODO: Full node active mapping - Almost done? there is a bug connected to it that needs to be fixed
        // TODO: check and refactor code if needed
        // TODO: reared specification and carefully test with lots of nodes
        // TODO: submit :)


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
            int startingNodeCounter = 0;
            while (true) {
                System.out.println("Listening for incoming connections...");
                if (!isStartingNode()) {
                    if (startingNodeCounter < 1)
                        connectToStartingNode();
                    try {
                        addMapElement(startingNode.getStartingNodeName(), startingNode.getStartingNodeIpAddress() + ":" + startingNode.getStartingNodePortNumber());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected!");
                    handleClientConnection(clientSocket);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                startingNodeCounter++;
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
                writer.write("NOTIFY?\n");
                writer.write(this.nodeName);
                writer.write(this.ipAddress + ":" + this.portNumber + "\n");
                writer.flush();
                response = reader.readLine();
                if (response.contains("NOTIFIED")) {
                    activeMapping(reader, writer, startingNode.getStartingNodeName(), startingNode.getStartingNodeIpAddress() + ":" + startingNode.getStartingNodePortNumber());
                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    executor.scheduleAtFixedRate(() -> {
                        try {
                            activeMapping(reader, writer, startingNode.getStartingNodeName(), startingNode.getStartingNodeIpAddress() + ":" + startingNode.getStartingNodePortNumber());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }, 0, 5, TimeUnit.SECONDS);
                }
                else {
                    throw new RuntimeException("Failed to connect to previous node");
                }

            } else {
                throw new RuntimeException("Failed to connect to previous node");
            }

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void activeMapping(BufferedReader reader, Writer writer, String nodeName, String nodeAddress) throws Exception {
        List<String> nodesToCheck = new ArrayList<>();
        nodeName = nodeName.replace("\n", "");
        nodesToCheck.add(nodeName + " " + nodeAddress);

        Set<String> visitedNodes = new HashSet<>(); // Track visited nodes

        int i = 0;

        while (!nodesToCheck.isEmpty() && i < 1000) {
            System.out.println("active mapping start");
            List<String> newNodesToCheck = new ArrayList<>();

            Iterator<String> iterator = nodesToCheck.iterator();
            while (iterator.hasNext()) {
                String node = iterator.next();
                try {
                    if (node.contains(this.nodeName.replace("\n", "")))
                        continue;

                    if (!visitedNodes.contains(node.split(" ")[1])) {
                        List<String> nearestNodes = sendNearestRequest(node);
                        iterator.remove();

                        for (String nearestNode : nearestNodes) {
                            String nearestNodeName = nearestNode.split(" ")[0];
                            String nearestNodeAddress = nearestNode.split(" ")[1];
                            addMapElement(nearestNodeName, nearestNodeAddress);
                            if (!visitedNodes.contains(nearestNode.split(" ")[1]))
                                newNodesToCheck.add(nearestNodeName + " " + nearestNodeAddress);
                        }
                        visitedNodes.add(node.split(" ")[1]);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
            }

            nodesToCheck.addAll(newNodesToCheck);

            i++;
            System.out.println("active mapping end");
        }
        System.out.println("FINISHED ACTIVE MAPPING");
    }

    private List<String> sendNearestRequest(String nodeNameAddress) throws Exception{
        List<String> nearestNodes = new LinkedList<>();
        try {
            String[] nodeNameAddressParts = nodeNameAddress.split(" ");
            String ipAddress = nodeNameAddressParts[1].split(":")[0];
            int portNumber = Integer.parseInt(nodeNameAddressParts[1].split(":")[1]);

            Socket socket = new Socket(ipAddress, portNumber);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Writer writer = new OutputStreamWriter(socket.getOutputStream());

            writer.write("START " + this.maxSupportedVersion + " " + this.emailAddress + ":dummyNodeForActiveMapping" + "\n");
            writer.flush();
            reader.readLine();

            writer.write("NEAREST? " + HashID.bytesToHex(HashID.computeHashID(nodeName)) + "\n");
            writer.flush();

            String response = reader.readLine();
            if (response != null && response.startsWith("NODES")) {
                int numberOfNodes = Integer.parseInt(response.split(" ")[1]);
                for (int i = 0; i < numberOfNodes; i++) {
                    String nodeNameAddressResponse = reader.readLine().trim();
                    nodeNameAddressResponse += " " + reader.readLine();
                    nearestNodes.add(nodeNameAddressResponse);
                }
            }

            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nearestNodes;
    }

    private void handleClientConnection(Socket clientSocket) throws Exception {
        Thread thread = new Thread(() -> {
            try {
                String clientAddress = "NN:NN";

                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                Writer writer = new OutputStreamWriter(clientSocket.getOutputStream());
                writer.write(sendStartMessage());
                writer.flush();

                String message = reader.readLine();
                String[] returnStartMessage = message.split(" ");

                System.out.println(returnStartMessage[0].equals("START"));
                System.out.println(returnStartMessage[1].equals(String.valueOf(this.maxSupportedVersion)) + " returnMesage[1] " + returnStartMessage[1] + "    String.valuerOF " + String.valueOf(this.maxSupportedVersion));
                boolean magic = Validator.isValidName(returnStartMessage[2]);
                System.out.println(magic);


                if (returnStartMessage[0].equals("START")
                        && returnStartMessage[1].equals(String.valueOf(this.maxSupportedVersion))
                        ) {
                    System.out.println("Node accepted connection.");
                    try {
                        addMapElement(returnStartMessage[2]);
                        while ((message = reader.readLine()) != null) {
                            System.out.println("::::::: " + message + " ::::::::");
                            if (message.equals("ECHO?")) {
                                System.out.println("Received ECHO? request");
                                writer.write(sendOhceMessage() + "\n");
                                writer.flush();
                            } else if (message.equals("OHCE")) {
                                System.out.println("Received OHCE");
                            } else if ((message.equals("SHOWMAP?")))
                            {
                                writer.write(getNetworkMap());
                                writer.flush();
                            } else if ((message.startsWith("NEAREST?"))) {
                                String[] parts = (message.split(" "));
                                if (parts.length == 2) {
                                    HashMap<String, Integer> nearest = findThreeNearestNodes(parts[1]);
                                    writer.write(sendNodesMessage(nearest));
                                    writer.flush();
                                }
                                else {
                                    writer.write(sendEndMessage("Incorrect-NEAREST?-request-use"));
                                    writer.flush();
                                }
                            } else if ((message.equals("NOTIFY?"))){

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
                                clientAddress = addressBuilder.toString();
                                setSynchronizedClientName(clientAddress);
                                writer.write(sendNotifiedMessage());
                                writer.flush();
                                activeMapping(reader, writer, returnStartMessage[2], clientAddress);
                                ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                                executor.scheduleAtFixedRate(() -> {
                                    try {
                                        activeMapping(reader, writer, returnStartMessage[2], getSynchronizedClientName());
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }, 0, 5, TimeUnit.SECONDS);

                            } else if ((message.startsWith("GET?"))) {
                                String[] parts = (message.split(" "));
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
                                    writer.write(sendEndMessage("Incorrect-GET?-request-use"));
                                    writer.flush();
                                }
                            }

                            else if ((message.startsWith("PUT?"))) {
                                String[] parts = (message.split(" "));
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
                            } else if (message.equals("")) {
                                // For NOTIFY? after connection
                            }
                            else if ((message.contains("END"))) {
                                System.out.println("Received END request. Closing connection.");
                                writer.write(sendEndMessage("Recieved-END-request"));
                                writer.flush();
                                break;
                            }
                            else {
                                writer.write(sendEndMessage("Incorrect-Request"));
                                writer.flush();
                                if (!clientAddress.equals("NN:NN"))
                                    deleteMapElement(clientAddress);
                                break;
                            }

                        }
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
            // Every 5 seconds active mapping request
        });
        thread.start();
    }

    public synchronized void deleteMapElement(String nodeAddress) {
        for (List<String> nodeList : networkMap.values()) {
            Iterator<String> iterator = nodeList.iterator();
            while (iterator.hasNext()) {
                String nodeWithAddress = iterator.next();
                if (nodeWithAddress.endsWith(nodeAddress)) {
                    iterator.remove();
                }
            }
        }
    }

    public synchronized void addMapElement(String addedNodeName) throws Exception {
        int distance = calculateDistance(addedNodeName);
        System.out.println("DISTANCE: "+ distance);
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
    public void handleNotify(String message, BufferedReader reader, BufferedWriter writer) throws Exception
    {
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
    public synchronized void setSynchronizedClientName(String value) {
        this.currentClientName= value;
    }

    public synchronized String getSynchronizedClientName() {
        return currentClientName;
    }
}