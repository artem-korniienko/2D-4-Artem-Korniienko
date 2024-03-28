import java.util.HashMap;
import java.util.Map;


//Utility purpose class, decouples Full and Temp nodes
public abstract class MessageSender {
    public String nodeName = null;
    public byte[] hashID = null;
    public String ipAddress = null;
    public int portNumber = 0;
    public final String emailAddress = "artem.korniienko@city.ac.uk";
    public int maxSupportedVersion = 1;

    String sendStartMessage()
    {
        return "START " + maxSupportedVersion + " " + nodeName;
    }
    String sendEndMessage(String reason)
    {
        return "END " + reason + "\n";
    }
    String sendEchoMessage()
    {
        return "ECHO?\n";
    }
    String sendOhceMessage()
    {
        return "OHCE";
    }
    String sendSuccessMessage() {return "SUCCESS\n"; }
    String sendFailedMessage() {return "FAILED\n"; }
    String sendNopeMessage() {return "NOPE\n"; }
    String sendNotifiedMessage() {return "NOTIFIED\n"; }
    String sendNearestMessage(String hashID) {return "NEAREST? " + hashID ; }
    String sendPutMessage(int keyLines, int valueLines) { return "PUT? " + keyLines + " " + valueLines + "\n"; }
    String sendGetMessage(int keyLines) {return "GET? " + keyLines + "\n"; }
    String sendNotifyMessage (String name, String address) {return "NOTIFY?\n" + name + "\n" + address + "\n"; }
    String sendValueMessage(int amountOfLines, String value) {return "VALUE " + amountOfLines + "\n" + value; }
    String sendNodesMessage(HashMap<String, Integer> nearest) {
        String returnMessage = "NODES " + nearest.size() + "\n";
        for (Map.Entry<String, Integer> entry : nearest.entrySet()) {
            String nameAddress = entry.getKey();
            nameAddress = nameAddress.replace("\n", "");
            String[] nameAdrressArray = nameAddress.split(" ");
            returnMessage += nameAdrressArray[0] + "\n" + nameAdrressArray[1] + "\n";
        }
        return returnMessage;
    }


}
