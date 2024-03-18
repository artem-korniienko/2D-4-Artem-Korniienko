import java.util.HashMap;
import java.util.Map;

public abstract class MessageSender {
    public String nodeName = null;
    public byte[] hashID = null;
    public String ipAddress = null;
    public int portNumber = 0;
    public final String emailAddress = "artem.korniienko@city.ac.uk";
    public String maxSupportedVersion = "1.0";

    String sendStartMessage()
    {
        return "START " + Float.parseFloat(maxSupportedVersion) + " " + nodeName;
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
    String sendNotifyMessage (String name, String address) {return "NOTIFY?\n" + name + address + "\n"; }
    String sendValueMessage(int amountOfLines, String value) {return "VALUE " + amountOfLines + "\n" + value; }
    String sendNodesMessage(HashMap<String, Integer> nearest) {
        String returnMessage = "NODES " + nearest.size() + "\n";
        for (Map.Entry<String, Integer> entry : nearest.entrySet())
        {
            returnMessage += entry.getKey();
            returnMessage += "\n";
        }
        return returnMessage;
    }

}
