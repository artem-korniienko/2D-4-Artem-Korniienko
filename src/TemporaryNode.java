// IN2011 Computer Networks
// Coursework 2023/2024
//
// Submission by
// Artem Korniienko
// 220052548
// artem.korniienko@city.ac.uk


import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;

// DO NOT EDIT starts
interface TemporaryNodeInterface {
    public boolean start(String startingNodeName, String startingNodeAddress);
    public boolean store(String key, String value);
    public String get(String key);
}
// DO NOT EDIT ends


public class TemporaryNode extends MessageSender implements TemporaryNodeInterface {
    public StartingNode startingNode;
    public String nodeName;
    public byte[] hashID;

    public final String emailAddress = "artem.korniienko@city.ac.uk";
    private static int counter = 0; // For naming purposes

    BufferedReader reader;
    Writer writer;
    Socket socket;

    TemporaryNode() {
        Random ran = new Random();
        counter = ran.nextInt(129048);
        this.nodeName = nameGenerator("-");
        super.nodeName = this.nodeName;
    }

    public boolean start(String startingNodeName, String startingNodeAddress) {
        try {
            hashID = HashID.computeHashID(startingNodeName);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (Validator.isValidName(startingNodeName))
            startingNode = new StartingNode(startingNodeName.endsWith("\n") ? startingNodeName : startingNodeName + "\n");
        else
            throw new RuntimeException("Incorrect name");

        try {
            startingNode.setStartingNodeHashID(HashID.computeHashID(startingNodeName));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        if (Validator.isValidAddress(startingNodeAddress)) {
            startingNode.setStartingNodeIpAddress(startingNodeAddress.split(":")[0]);
            startingNode.setStartingNodePortNumber(Integer.parseInt(startingNodeAddress.split(":")[1]));
        }

        try {
            socket = new Socket(startingNode.getStartingNodeIpAddress(), startingNode.getStartingNodePortNumber());

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new OutputStreamWriter(socket.getOutputStream());

            String response = reader.readLine();
            String[] returnStartMessage = response.split(" ");
            if (returnStartMessage[0].equals("START")
                    && returnStartMessage[1].equals(String.valueOf(this.maxSupportedVersion))
                    && Validator.isValidName(returnStartMessage[2])) {
                writer.write(sendStartMessage());
                writer.flush();
            } else {
                throw new RuntimeException("Have not received START message back. Or the format of the start message is incorrect");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    //Almost BFS to find the closest node. look: activeMapping() in the Full Node
    public String findClosestNode(String hashID) {
        String closestNode = "";
        int difference = Integer.MAX_VALUE;

        try {
            List<String> nearestNodes = sendNearestRequest(hashID);

            Set<String> visitedNodes = new HashSet<>();

            for (String node : nearestNodes) {
                String nodeName = node.split(" ")[0];
                String ipAddress = node.split(" ")[1];

                if (!visitedNodes.contains(ipAddress)) {
                    visitedNodes.add(ipAddress);

                    String closestFromNearest = findClosestNodeFromNode(nodeName, ipAddress);

                    int currentDifference = HashID.distance(this.nodeName, closestFromNearest.split(" ")[0]);
                    if (currentDifference < difference) {
                        difference = currentDifference;
                        closestNode = closestFromNearest;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return closestNode;
    }

    private List<String> sendNearestRequest(String hashID) throws Exception {
        List<String> nearestNodes = new LinkedList<>();
        try {

            String ipAddress = startingNode.getStartingNodeIpAddress();
            int portNumber = startingNode.getStartingNodePortNumber();

            Socket socket = new Socket(ipAddress, portNumber);

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            Writer writer = new OutputStreamWriter(socket.getOutputStream());

            Random ran = new Random();
            writer.write("START " + this.maxSupportedVersion + " " + this.emailAddress + ":dummyNodeForActiveMapping" + ran.nextInt(321541) + "\n");
            writer.flush();
            reader.readLine();

            writer.write("NEAREST? " + hashID + "\n");
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


    private String findClosestNodeFromNode(String nodeName, String ipAddress) throws Exception {
        String closestNode = "";
        int difference = Integer.MAX_VALUE;

        Socket socket = new Socket(ipAddress.split(":")[0], Integer.parseInt(ipAddress.split(":")[1]));
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
                String nodeInfo = reader.readLine().trim();
                nodeInfo += " " + reader.readLine();

                String[] nodeParts = nodeInfo.split(" ");
                String currentNodeName = nodeParts[0];
                String currentIpAddress = nodeParts[1];
                int currentDifference = HashID.distance(this.nodeName, currentNodeName);

                if (currentDifference < difference) {
                    difference = currentDifference;
                    closestNode = currentNodeName + " " + currentIpAddress;
                }
            }
        }

        socket.close();
        return closestNode;
    }

    public boolean store(String key, String value) {
        if (!key.endsWith("\n"))
            key += "\n";
        if (!value.endsWith("\n"))
            key += "\n";
        int keyLines = calculateNewLineCharacrter(key);
        int valueLines = calculateNewLineCharacrter(value);
        try {
            writer.write(sendPutMessage(keyLines, valueLines));
            writer.write(key);
            writer.write(value);
            writer.flush();
            String response = reader.readLine();
            if (!response.contains("SUCCESS"))
                return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String get(String key) {
        if (!key.endsWith("\n"))
            key += "\n";
        String response = "";
        try {
            writer.write(sendGetMessage(calculateNewLineCharacrter(key)));
            writer.write(key);
            writer.flush();
            response = reader.readLine();
            if (!response.contains("NOPE")) {
                String[] responseArray = response.split(" ");
                response = "";
                for (int i = 0; i < Integer.parseInt(responseArray[1]); i++) {
                    response += reader.readLine() + "\n";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return response;
    }

    private int calculateNewLineCharacrter(String string) {
        int newLineCharacterCounter = 0;
        char[] stringArray = string.toCharArray();
        for (int i = 0; i < string.length(); i++) {
            if (stringArray[i] == '\n')
                newLineCharacterCounter++;
        }
        return newLineCharacterCounter;
    }

    public String nameGenerator(String nodeInfo) {
        return emailAddress + ":" + "artems-iplementation,temp-node" + String.valueOf(counter) + "\n";
    }
}