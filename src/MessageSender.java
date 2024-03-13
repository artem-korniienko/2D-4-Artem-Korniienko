public abstract class MessageSender {
    public String nodeName = null;
    public byte[] hashID = null;
    public String ipAddress = null;
    public int portNumber = 0;
    public final String emailAddress = "artem.korniienko@city.ac.uk";
    public float maxSupportedVersion = 1;

    String sendStartMessage()
    {
        return "START " + String.valueOf(maxSupportedVersion) + " " + nodeName;
    }
    String sendEndMessage(String reason)
    {
        return "END " + reason;
    }
    String sendEchoMessage()
    {
        return "ECHO?";
    }
    String sendOhceMessage()
    {
        return "OHCE";
    }

}
