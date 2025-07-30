package CCP.src;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.json.JSONObject;

public class Main extends Thread {
    private static BlockingQueue<String> queue1 = new LinkedBlockingQueue<>();
    private static BlockingQueue<String> queue2 = new LinkedBlockingQueue<>();

    public static DatagramSocket mcpSocket;
    public static DatagramSocket brServerSocket;
    public static Socket brSocket;

    public static Command fromMCPCom;
    public static int MCPSeqNum = 0;
    public final static int port = 2000;
    public static long MCPTime;

    public static Command toMCPCom;
    public static int CCPSeqNum;

    public static boolean waiting = false;
    public static long BRCTime;

    public static void main(String[] args) {


        try {
            ExecutorService executorService = Executors.newFixedThreadPool(2);

            executorService.submit(() -> {
                try {
                    startUpBR();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });

            if(queue2.take().equalsIgnoreCase("init")){
                executorService.submit(() -> {
                    try {
                        startUpMCP();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void startUpMCP() throws InterruptedException{
        CCPSeqNum = (int) ((Math.random()  * (30000 - 1000)) + 1000);
        Command init = new Command("CCIN", CCPSeqNum++, null, null, null);

        try {
            // Convert JSON object to byte array
            byte[] req = init.toString().getBytes();

            InetAddress address = InetAddress.getByName("10.20.30.182");
            System.out.println("Connecting to MCP...");
            mcpSocket = new DatagramSocket();
            DatagramPacket request = new DatagramPacket(req, req.length, address, port);
            
            // an attempt to send a string
            mcpSocket.send(request);
            System.out.println("JSON request sent");
            
            while (true){

                byte[] buffer = new byte[512];
                DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                mcpSocket.receive(response);

                String responseStr = new String(response.getData(), 0, response.getLength());
                System.out.println("Response: " + responseStr);

                JSONObject replyJson = interpret(responseStr);

                if(replyJson!=null){
                    byte[] replyArray = replyJson.toString().getBytes();
                    DatagramPacket reply = new DatagramPacket(replyArray, replyArray.length, address, port);
                    mcpSocket.send(reply);
                }

                String BRresp = null;

                if(waiting){
                    BRresp = queue2.take();
                    waiting = false;
                }

                if(BRresp!=null){
                    Command rep = new Command("STAT", CCPSeqNum++, null, BRresp, null);
                    BRCTime = System.currentTimeMillis();
                    byte[] replyArray = rep.toString().getBytes();
                    DatagramPacket reply = new DatagramPacket(replyArray, replyArray.length, address, port);
                    mcpSocket.send(reply);
                }

                if(MCPTime - System.currentTimeMillis() >= 6000){
                    queue1.put("STOP");
                }

                if(BRCTime - System.currentTimeMillis() >= 6000){
                    Command rep = new Command("STAT", CCPSeqNum++, null, "ERR", null);
                    byte[] replyArray = rep.toString().getBytes();
                    DatagramPacket reply = new DatagramPacket(replyArray, replyArray.length, address, port);
                    mcpSocket.send(reply);
                }

            }
        } catch (IOException e) {
            System.out.println("Failed to connect to MCP.");
            e.printStackTrace();
        } finally {
            if (mcpSocket != null && !mcpSocket.isClosed()) {
                mcpSocket.close();
            }
        }
    }

    public static void startUpBR() throws InterruptedException{

        try {
            System.out.println("Starting UDP server for BR...");
            brServerSocket = new DatagramSocket(3011);
            System.out.println("UDP Server for BR is listening ");
            byte[] receiveBuffer = new byte[512];

            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            brServerSocket.receive(receivePacket);
            System.out.println("Received data from BR client");

            String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("Message from BR: " + receivedData);

            queue2.put(receivedData);

            String responseMessage = "AKIN";
            byte[] responseData = responseMessage.getBytes();

            DatagramPacket responsePacket = new DatagramPacket(
                responseData,
                responseData.length,
                receivePacket.getAddress(),
                receivePacket.getPort()
            );

            brServerSocket.send(responsePacket);

            while (true) {

                byte[] responseDataLoop = queue1.take().getBytes();

                if(responseDataLoop != null){
                    DatagramPacket responsePacketLoop = new DatagramPacket(
                        responseDataLoop,
                        responseDataLoop.length,
                        receivePacket.getAddress(),
                        receivePacket.getPort()
                    );

                    brServerSocket.send(responsePacketLoop);
                    
                    System.out.println("Response sent to BR client");
                }

                byte[] receive = new byte[512];

                DatagramPacket receivePack = new DatagramPacket(receive, receive.length);
    
                brServerSocket.receive(receivePack);
                String receiveData = new String(receivePack.getData(), 0, receivePack.getLength());

                queue2.put(receiveData);
            }
        } catch (IOException e) {
            System.out.println("Failed to start UDP server for BR.");
            e.printStackTrace();
        } finally {
            if (brServerSocket != null && !brServerSocket.isClosed()) {
                brServerSocket.close();
            }
        }
    }

    public static JSONObject interpret(String str) throws InterruptedException {
        // code to recieve JSON obect from MCP, save into JSON object and then convert to class Command object 

        JSONObject recieved = new JSONObject(str); // temp ack message

        if (MCPSeqNum == 0) {
            MCPSeqNum = recieved.getInt("sequence_number");
        } else if (recieved.getInt("sequence_number") - MCPSeqNum == 1) {
            MCPSeqNum = recieved.getInt("sequence_number");
        } else {
            queue1.put("STOP");
            MCPSeqNum = recieved.getInt("sequence_number");
            return new Command("STAT", CCPSeqNum++, null, "ERR", null);
        }

        switch(recieved.getString("message")){
            case "AKIN":
                break;
            case "AKST":
                break;
            case "STRQ":
                queue1.put("STRQ");
                waiting = true;
                MCPTime = System.currentTimeMillis();
                break;
            case "EXEC":
                Command akex = new Command("AKEX", CCPSeqNum++, null, null, null);
                queue1.put(recieved.getString("action"));
                waiting = true;
                return akex;
            default:
                Command def = new Command("NOIP", CCPSeqNum++, null, null, null);
                return def;

        }
        return null;

    }


    public static void reconnect(){
    }

    public static void emergencyStop(){
    }

}
