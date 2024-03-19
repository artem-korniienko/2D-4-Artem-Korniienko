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
import java.util.Random;

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

        // Setup fields with information about starting node
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

            writer.write(sendStartMessage() + "\n");
            writer.flush();

            String response = reader.readLine();
            if (response != null && response.startsWith("START")) {
                System.out.println("Connection established with previous node");
            } else {
                throw new RuntimeException("Failed to connect to previous node");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean store(String key, String value) {
        int keyLines = calculateNewLineCharacrter(key);
        int valueLines = calculateNewLineCharacrter(value);
        try {
            writer.write(sendPutMessage(keyLines, valueLines));
            writer.write(key);
            writer.write(value);
            writer.flush();
            String response = reader.readLine();
            if (response.contains("SUCCESS")) {
                System.out.println("Data stored successfully");
            } else {
                System.out.println("Failed to store data");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String get(String key) {
        int keyLines = calculateNewLineCharacrter(key);
        String response = "";
        try {
            writer.write(sendGetMessage(keyLines));
            writer.write(key);
            writer.flush();
            response = reader.readLine();
            if (!response.equals(sendNopeMessage())) {
                String[] responseArray = response.split(" ");
                response = "";
                for (int i = 0; i < Integer.parseInt(responseArray[1]); i++) {
                    response += reader.readLine() + "\n";
                }
            } else {
                System.out.println("Failed to retrieve data");
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
        return emailAddress + ":" + "my-iplementation,test-full-node" + String.valueOf(counter) + "\n";
    }
}