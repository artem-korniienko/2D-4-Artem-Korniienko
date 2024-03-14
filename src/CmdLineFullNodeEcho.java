
import java.net.Socket;


public class CmdLineFullNodeEcho {
    public CmdLineFullNodeEcho() {
    }

    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("Usage error!");
            System.err.println("CmdLineFullNode startingNodeName startingNodeAddress ipAddress portNumber");
        } else {
            String startingNodeName = args[0];
            String startingNodeAddress = args[1];
            String ipAddress = args[2];

            int portNumber;
            try {
                portNumber = Integer.parseInt(args[3]);
            } catch (Exception var6) {
                System.err.println("Exception parsing the port number");
                System.err.println(var6);
                return;
            }

            FullNode fn = new FullNode();
            if (fn.listen(ipAddress, portNumber)) {
                Thread handleConnectionsThread = new Thread(() -> {
                    fn.handleIncomingConnections(startingNodeName, startingNodeAddress);
                });
                handleConnectionsThread.start();
                testEchoMessage(fn, startingNodeAddress);
            } else {
                System.err.println("Could not listen for incoming connections");
            }
        }
    }

    private static void testEchoMessage(FullNode fn, String startingNodeAddress) {
        try {
            Thread.sleep(100);
            Socket socket = new Socket(startingNodeAddress.split(":")[0], Integer.parseInt(startingNodeAddress.split(":")[1]));
            fn.sendEchoMessage(socket);
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
