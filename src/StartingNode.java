import java.net.InetAddress;

//Utility use class, helps encapsulate information about starting node
public class StartingNode {

    private String startingNodeName;
    private byte[] startingNodeHashID;
    private String startingNodeIpAddress;
    private int startingNodePortNumber;
    private InetAddress host;

    public StartingNode(String startingNodeName) {
        this.startingNodeName = startingNodeName;
    }

    public String getStartingNodeName() {
        return startingNodeName;
    }

    public void setStartingNodeName(String startingNodeName) {
        this.startingNodeName = startingNodeName;
    }

    public byte[] getStartingNodeHashID() {
        return startingNodeHashID;
    }

    public void setStartingNodeHashID(byte[] startingNodeHashID) {
        this.startingNodeHashID = startingNodeHashID;
    }

    public String getStartingNodeIpAddress() {
        return startingNodeIpAddress;
    }

    public void setStartingNodeIpAddress(String startingNodeIpAddress) {
        this.startingNodeIpAddress = startingNodeIpAddress;

        try {
            host = InetAddress.getByName(startingNodeIpAddress);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    public int getStartingNodePortNumber() {
        return startingNodePortNumber;
    }

    public void setStartingNodePortNumber(int startingNodePortNumber) {
        this.startingNodePortNumber = startingNodePortNumber;
    }
}
