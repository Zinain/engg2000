package CCP.src;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Random;

public class MCP {
    private static final String MCP_IP = "10.20.30.1";
    private static final int MCP_PORT = 2000;
    private static final int HEARTBEAT_INTERVAL = 2000;  // 2 seconds
    private static final Random rand = new Random();
    static int seqNum = 0;

    public static void main(String[] args) {
        new Thread(() -> {
        try (DatagramSocket socket = new DatagramSocket(MCP_PORT)) {
            System.out.println("MCP listening on port " + MCP_PORT);

            byte[] buffer = new byte[2048];  // Increased buffer size for larger UDP packets
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.receive(packet);
            String receivedMessage = new String(packet.getData(), 0, packet.getLength());
            System.out.println("Received: " + receivedMessage);

            Command init = new Command("AKIN", seqNum++, null, null, null);

            byte[] req = init.toString().getBytes();

            DatagramPacket response = new DatagramPacket(req, req.length, packet.getAddress(), packet.getPort());
            
            // an attempt to send a string
            socket.send(response);
            System.out.println("JSON request sent" + init.toString());

            Thread.sleep(HEARTBEAT_INTERVAL);

            Command init1 = new Command("AKST", seqNum++, null, null, null);

            byte[] req1 = init1.toString().getBytes();

            DatagramPacket response1 = new DatagramPacket(req1, req1.length, packet.getAddress(), packet.getPort());
            
            // an attempt to send a string
            socket.send(response1);
            System.out.println("JSON request sent" + init1.toString());

            Command init2 = new Command("STRQ", seqNum++, null, null, null);

            byte[] req2 = init2.toString().getBytes();

            DatagramPacket response2 = new DatagramPacket(req2, req2.length, packet.getAddress(), packet.getPort());
            
            // an attempt to send a string
            socket.send(response2);
            System.out.println("JSON request sent" + init2.toString());

            byte[] buffer1 = new byte[2048];  // Increased buffer size for larger UDP packets
            DatagramPacket packet1 = new DatagramPacket(buffer1, buffer1.length);
            socket.receive(packet1);
            String receivedMessage1 = new String(packet1.getData(), 0, packet1.getLength());
            System.out.println("Received: " + receivedMessage1);

            Command init3 = new Command("EXEC", seqNum++, "STOPC", null, null);

            byte[] req3 = init3.toString().getBytes();

            DatagramPacket response3 = new DatagramPacket(req3, req3.length, packet.getAddress(), packet.getPort());
            
            // an attempt to send a string
            socket.send(response3);
            System.out.println("JSON request sent" + init3.toString());

            byte[] buffer2 = new byte[2048];  // Increased buffer size for larger UDP packets
            DatagramPacket packet2 = new DatagramPacket(buffer2, buffer2.length);
            socket.receive(packet2);
            String receivedMessage2 = new String(packet2.getData(), 0, packet2.getLength());
            System.out.println("Received: " + receivedMessage2);

        } catch (Exception e) {
            e.printStackTrace();
        }}).start();
    }

    // Send status request messages to all connected devices
    // private static void sendStatusRequests(DatagramSocket socket) {
    //     String[] checkpointIPs = {"10.20.30.50", "10.20.30.51", "10.20.30.52"};
    //     String[] stationIPs = {"10.20.30.200", "10.20.30.201", "10.20.30.202"};

    //     // Send status requests to CPCs
    //     for (String ip : checkpointIPs) {
    //         sendMessage(socket, createStatusRequestMessage("CPC", "CPXX"), ip, 5001);
    //     }

    //     // Send status requests to STCs
    //     for (String ip : stationIPs) {
    //         sendMessage(socket, createStatusRequestMessage("STC", "STXX"), ip, 4001);
    //     }
    // }

    // Handle incoming messages
    // private static void handleIncomingMessage(JSONObject jsonMessage, InetAddress address, int port, DatagramSocket socket) {
    //     String messageType = jsonMessage.get("message").getAsString();
    //     String clientId = jsonMessage.get("client_id").getAsString();

    //     switch (messageType) {
    //         case "CCIN":  // CCP Initiation Message
    //             sendMessage(socket, createAcknowledgementMessage("CCP", clientId, "AKIN"), address.getHostAddress(), port);
    //             break;
    //         case "CPIN":  // CPC Initiation Message
    //             sendMessage(socket, createAcknowledgementMessage("CPC", clientId, "AKIN"), address.getHostAddress(), port);
    //             break;
    //         case "STIN":  // STC Initiation Message
    //             sendMessage(socket, createAcknowledgementMessage("STC", clientId, "AKIN"), address.getHostAddress(), port);
    //             break;
    //         case "STAT":  // Status Message
    //             System.out.println("Received status from: " + clientId);
    //             sendMessage(socket, createAcknowledgementMessage(jsonMessage.get("client_type").getAsString(), clientId, "AKST"), address.getHostAddress(), port);
    //             break;
    //         case "TRIP":  // IR Trip status message
    //             sendMessage(socket, createAcknowledgementMessage(jsonMessage.get("client_type").getAsString(), clientId, "AKTR"), address.getHostAddress(), port);
    //             break;
    //         default:
    //             // Handle unrecognized messages
    //             System.out.println("Unknown message type: " + messageType);
    //             sendMessage(socket, createNotImplementedMessage(jsonMessage.get("client_type").getAsString(), clientId), address.getHostAddress(), port);
    //     }
    // }

    // // Create a generic status request message
    // private static String createStatusRequestMessage(String clientType, String clientId) {
    //     JsonObject message = new JsonObject();
    //     message.addProperty("client_type", clientType);
    //     message.addProperty("message", "STRQ");
    //     message.addProperty("client_id", clientId);
    //     message.addProperty("sequence_number", generateSequenceNumber());
    //     return message.toString();
    // }

    // Create an acknowledgement message
    // private static String createAcknowledgementMessage(String clientType, String clientId, String ackType) {
    //     JsonObject message = new JsonObject();
    //     message.addProperty("client_type", clientType);
    //     message.addProperty("message", ackType);
    //     message.addProperty("client_id", clientId);
    //     message.addProperty("sequence_number", generateSequenceNumber());
    //     return message.toString();
    // }

    // Create a "not implemented" message
    // private static String createNotImplementedMessage(String clientType, String clientId) {
    //     JsonObject message = new JsonObject();
    //     message.addProperty("client_type", clientType);
    //     message.addProperty("message", "NOIP");
    //     message.addProperty("client_id", clientId);
    //     message.addProperty("sequence_number", generateSequenceNumber());
    //     return message.toString();
    // }

    // Send a JSON message via UDP
    private static void sendMessage(DatagramSocket socket, String message, String ip, int port) {
        try {
            byte[] buffer = message.getBytes();
            InetAddress address = InetAddress.getByName(ip);
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
            socket.send(packet);
            System.out.println("Sent: " + message + " to " + ip + ":" + port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Generate a random sequence number
    private static String generateSequenceNumber() {
        return String.valueOf(rand.nextInt(20000) + 1000);  
    }
}
