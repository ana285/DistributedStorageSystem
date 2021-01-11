import Core.Query;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import db.DBManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Worker {
    private Socket socket                  = null;
    private ServerSocket server            = null;
    private DataInputStream inFromClient   = null;
    private DataOutputStream outToClient   = null;
    private String query                   = "";
    private Integer port                   = null;
    private static String workerName              = null;

    public Worker(int port) {
        this.port = port;
        workerName = "Worker" + port % 10;
        Path path = Paths.get("/Users/rusuna/stuff/faculty/M2_S1/PPAD/Project-Storage/Storage/" + workerName);
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            System.out.println("Directory not created: " + e.getMessage());
        }
    }

    public void run() {
        // starts server and waits for a connection
        try {
            server = new ServerSocket(port);
        }catch(Exception e) {
            System.out.println(e);
        }
        System.out.println("Server started!");
        System.out.println("Waiting for a client ...");
        while (true) {
            try {
                socket = server.accept();
                System.out.println("Client " + socket.getPort() + " accepted.");

                // takes input from the dispatcher
                inFromClient = new DataInputStream(socket.getInputStream());
                outToClient = new DataOutputStream(socket.getOutputStream());
                // reads message from client until "Over" is sent
                while (!query.equals("exit")) {
                    query = inFromClient.readUTF();

                    Gson gson = new GsonBuilder().create();
                    Query query_credentials = gson.fromJson(query , Query.class);
                    System.out.println("Execute query: " + query_credentials.toString());

                    // Execute the query: save the file


                }
                System.out.println("Closing connection");

                // close connection
                socket.close();
                inFromClient.close();
            }catch(Exception e) {
                System.out.println("Exceptie try:Connection pipe broken");
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        // arguments: port
        if (args.length != 2) {
            System.out.println("Try something like this: java -cp .:/usr/share/java/gson-2.8.5.jar:"
                    + "/usr/share/java/mysql-connector-java.jar Worker port heartbeatPort \n");
        } else {
            int port = Integer.parseInt(args[0]);
            int heartbeatPort = Integer.parseInt(args[1]);
            int workerListenerPort = 5000 + port % 10;
            Worker worker = new Worker(port);

            DBManager.getInstance().addWorker(port, heartbeatPort, workerName);

            Heartbeat heartbeat = new Heartbeat(heartbeatPort, workerName);
            heartbeat.start();

//            WorkerListener workerHandler = new WorkerListener(workerName, sqlPort, workerListenerPort);
//            workerHandler.start();

            worker.run();
        }
    }
}
