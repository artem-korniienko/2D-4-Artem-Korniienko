// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Artem Korniienko
// 220052548
// artem.korniienko@city.ac.uk

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
    public String ipAddress;
    public int portNumber;

    public final String emailAddress = "artem.korniienko@city.ac.uk";
    private static int counter = 0; // For naming purposes
    public int maxSupportedVersion = 1;


    private String currentClientName = "NN:NN";
    public HashMap<Integer, List<String>> networkMap;
    public HashMap<String, String> map = new HashMap<>();

    ServerSocket serverSocket;

    //I am generating name with random identifier for each one of my full nodes
    public FullNode() {
        Random ran = new Random();
        counter = ran.nextInt(129048);
        this.nodeName = nameGenerator("-");
        super.nodeName = this.nodeName;
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
    }

    public void handleIncomingConnections(String startingNodeName, String startingNodeAddress) {
        // TODO: check my implementations regarding start package(all the types of methods that return something, fields for the name)
        // TODO: ensure i delete everything if incorrect format
        // TODO: check and refactor code if needed
        // TODO: reared specification and carefully test with lots of nodes
        // TODO: submit :)
        // TODO: ensure we are not adding duplicate elements(with the same address at least)
        // TODO: check so that we are addinh same addresses but different names

        //TODO: add instruction file
        //TODO: test against all requierments
        //TODO: test on martins-network
        //TODO: record
        //TODO: submit

        if (Validator.isValidName(startingNodeName))
            startingNode = new StartingNode(startingNodeName.endsWith("\n") ? startingNodeName : startingNodeName + "\n");
        else
            throw new RuntimeException("Incorrect name");

        try {
            startingNode.setStartingNodeHashID(HashID.computeHashID(startingNodeName));
        } catch (Exception e) {
            e.printStackTrace();
        }

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
                if (!isStartingNode()) {
                    if (startingNodeCounter < 1)
                        connectToStartingNode(); // if current node is not starting node(starting node address, and address on which this nodes listen for the connections are different)
                    // then we separately connect to the starting node, this code ommitted if current node is starting node. I also use a counter to ensure that this code is executed only once
                    try {
                        addMapElement(startingNode.getStartingNodeName(), startingNode.getStartingNodeIpAddress() + ":" + startingNode.getStartingNodePortNumber());
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Constantly checking for any incoming connections
                try {
                    Socket clientSocket = serverSocket.accept();
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

            String response = reader.readLine();
            String[] returnStartMessage = response.split(" ");

            if (returnStartMessage[0].equals("START")
                    && returnStartMessage[1].equals(String.valueOf(this.maxSupportedVersion))
                    && Validator.isValidName(returnStartMessage[2])) {

                writer.write(sendStartMessage());
                writer.flush();
                //Sending notify request straight after the exchange of the start messages in order to provide more network robustness and make sure that this new node is known in the network
                writer.write("NOTIFY?\n");
                writer.write(this.nodeName);
                writer.write(this.ipAddress + ":" + this.portNumber + "\n");
                writer.flush();

                response = reader.readLine();
                if (response.contains("NOTIFIED")) {
                    //Start active mapping, and then schedule it for every 5 seconds
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

    //Method that is used to query other nodes directories and tries to get whole network map
    //In fact, this is a try to perform BFS on the network by examining the closest nodes, of the input node, adding them to the map, and adding them to the queue of the nodes that needs to be checked
    private void activeMapping(BufferedReader reader, Writer writer, String nodeName, String nodeAddress) throws Exception {

        List<String> nodesToCheck = new ArrayList<>();
        nodeName = nodeName.replace("\n", "");
        nodesToCheck.add(nodeName + " " + nodeAddress);

        Set<String> visitedNodes = new HashSet<>();

        int i = 0;

        while (!nodesToCheck.isEmpty() && i < 1000) {
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
        }
    }

    //Helper method of the active mapping, connects to a node, sends the nearest requests and gets a string of the nearest nodes in the correct format
    private List<String> sendNearestRequest(String nodeNameAddress) throws Exception{
        List<String> nearestNodes = new LinkedList<>();
        try {
            String[] nodeNameAddressParts = nodeNameAddress.split(" ");
            String ipAddress = nodeNameAddressParts[1].split(":")[0];
            int portNumber = Integer.parseInt(nodeNameAddressParts[1].split(":")[1]);

            Socket socket = new Socket(ipAddress, portNumber);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Writer writer = new OutputStreamWriter(socket.getOutputStream());

            Random ran = new Random();
            writer.write("START " + this.maxSupportedVersion + " " + this.emailAddress + ":dummyNodeForActiveMapping" + ran.nextInt(321541) + "\n");
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


    //Main method with the logic for  responding to requests.
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

                if (returnStartMessage[0].equals("START")
                        && returnStartMessage[1].equals(String.valueOf(this.maxSupportedVersion))
                        && Validator.isValidName(returnStartMessage[2])) {
                    try {
                        addMapElement(returnStartMessage[2]);
                        while ((message = reader.readLine()) != null) {
                            if (message.equals("ECHO?")) {
                                writer.write(sendOhceMessage() + "\n");
                                writer.flush();
                            } else if (message.equals("OHCE")) {
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
                                    if (!clientAddress.equals("NN:NN"))
                                        deleteMapElement(clientAddress);
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
                                    if (!clientAddress.equals("NN:NN"))
                                        deleteMapElement(clientAddress);
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
                                    if (shouldContain) {
                                        map.put(HashID.bytesToHex(requestHASHID), valueBuilder.toString());
                                        writer.write(sendSuccessMessage());
                                        writer.flush();
                                    }
                                    else {
                                        writer.write(sendFailedMessage());
                                        writer.flush();
                                    }
                                } else {
                                    writer.write("Invalid PUT request format\n");
                                    writer.flush();
                                    if (!clientAddress.equals("NN:NN"))
                                        deleteMapElement(clientAddress);
                                }
                            } else if (message.equals("")) {
                                // For NOTIFY? after connection
                            }
                            else if ((message.contains("END"))) {
                                writer.write(sendEndMessage("Recieved-END-request"));
                                writer.flush();
                                if (!clientAddress.equals("NN:NN"))
                                    deleteMapElement(clientAddress);
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

    //Adding elements to the map of the node, if there are more than 3 nodes on certain distance, I remove the oldest one and add a new one
    public synchronized void addMapElement(String addedNodeName) throws Exception {
        int distance = calculateDistance(addedNodeName);
        List<String> nodeList = networkMap.getOrDefault(distance, new ArrayList<>());

        if (nodeList.size() >= 3) {
            nodeList.remove(0);
        }
        String nodeWithAddress = addedNodeName + " NN:NN";
        if (addedNodeName.equals(this.nodeName)) {
            nodeWithAddress =  addedNodeName + " " + ipAddress + ":" + portNumber;
        }
        if (!nodeList.contains(nodeWithAddress) && !containsSameIP(nodeWithAddress.split(" ")[1])) {
            nodeList.add(nodeWithAddress);
            networkMap.put(distance, nodeList);
        }
    }
    public synchronized boolean containsSameIP(String ip)
    {
        for (Map.Entry<Integer, List<String>> entry : networkMap.entrySet())
        {
            for (String element :entry.getValue())
            {
                if (element.contains(ip))
                    return true;
            }
        }
        return false;
    }
    public synchronized void addMapElement(String addedNodeName, String addedNodeAddress) throws Exception {
        int distance = calculateDistance(addedNodeName);
        List<String> nodeList = networkMap.getOrDefault(distance, new ArrayList<>());

        if (nodeList.size() >= 3) {
            nodeList.remove(0);
        }
        String nodeWithAddress = addedNodeName + " " + addedNodeAddress;
        if (addedNodeName.equals(this.nodeName)) {
            nodeWithAddress =  addedNodeName + " " + ipAddress + ":" + portNumber;
        }
        if (!nodeList.contains(nodeWithAddress) && !containsSameIP(nodeWithAddress.split(" ")[1])) {
            nodeList.add(nodeWithAddress);
            networkMap.put(distance, nodeList);
        }
    }
    public int calculateDistance(String secondNodeName) throws Exception
    {
        return HashID.distance(HashID.computeHashID(nodeName), HashID.computeHashID(secondNodeName));
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
    //Used for utility purposes, used with SHOWMAP? request
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
    HashMap<String, Integer> findThreeNearestNodes(byte[] hashID) throws Exception {
        HashMap<String, Integer> nearestNodes = new HashMap<>();
        TreeMap<Integer, List<String>> sortedMap = new TreeMap<>();

        HashMap<Integer, List<String>> networkMapCopy = new HashMap<>(networkMap);

        for (Map.Entry<Integer, List<String>> entry : networkMapCopy.entrySet()) {
            List<String> nodesCopy = new ArrayList<>(entry.getValue());
            for (String node : nodesCopy) {
                if (!node.contains("NN:NN")) {
                    int nodeDistance = HashID.distance(hashID, HashID.computeHashID(node));
                    sortedMap.putIfAbsent(nodeDistance, new ArrayList<>());
                    sortedMap.get(nodeDistance).add(node);
                }
            }
        }

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
        return emailAddress + ":" + "artems-implementation,full-node" + nodeInfo + FullNode.getCounter() + "\n";
    }
    public synchronized void setSynchronizedClientName(String value) {
        this.currentClientName= value;
    }
    public synchronized String getSynchronizedClientName() {
        return currentClientName;
    }
    public static int getCounter() {
        return counter;
    }
}