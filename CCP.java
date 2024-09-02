package engg2000;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CCP extends Thread {
    public static void main(String[] args) {
        Thread thread1 = new Thread(() -> startUpMCP("address", 1234));

        thread1.start();
    }

    public static void startUpMCP(String serverAddress, int port){
        try (Socket socket = new Socket(serverAddress, port)) {
            System.out.println("Connected to " + serverAddress + " on port " + port);

            // Send and receive data
            OutputStream output = socket.getOutputStream();
            PrintWriter writer = new PrintWriter(output, true);

            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            String text;

            do {
                System.out.print("Enter message to " + serverAddress + ": ");
                text = reader.readLine();

                writer.println(text);

                String serverResponse = serverReader.readLine();
                System.out.println("Response from " + serverAddress + ": " + serverResponse);

            } while (!text.equals("bye"));

        } catch (UnknownHostException ex) {
            System.out.println("Server not found: " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("I/O error: " + ex.getMessage());
        }
    }

    public static void startUpBR(int SERVER_PORT){
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started. Waiting for connections...");

            // Executor to handle multiple clients
            ExecutorService executorService = Executors.newCachedThreadPool();

            Socket clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress());
            // Handle each client in a separate thread
            executorService.submit(() -> bladeHeartBeat(clientSocket));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void interpret(){
    }
    public static void passStatusBR(){
    }
    public static void passStatusMCP(){
    }
    public static void bladeHeartBeat(Socket clientSocket){
        int HEARTBEAT_TIMEOUT = 5000; // 5 seconds

        try (DataInputStream in = new DataInputStream(clientSocket.getInputStream())) {

            while (true) {
                long lastHeartbeatTime = System.currentTimeMillis();

                // Listen for heartbeat
                if (in.readUTF().equals("HEARTBEAT")) {
                    System.out.println("Received heartbeat from client");
                    lastHeartbeatTime = System.currentTimeMillis();
                }

                // Check for timeout
                if (System.currentTimeMillis() - lastHeartbeatTime > HEARTBEAT_TIMEOUT) {
                    System.out.println("Heartbeat timeout. Closing connection.");
                    clientSocket.close();
                    break;
                }

                // Sleep a bit to avoid busy waiting
                Thread.sleep(100);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void reconnect(){
    }
    public static void emergencyStop(){
    }
}
