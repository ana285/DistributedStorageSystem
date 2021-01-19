import Core.ClientTask;
import Core.Query;
import Core.WorkerMinimalConnData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import db.DBManager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Worker {
    private Socket socket                  = null;
    private ServerSocket server            = null;
    private DataInputStream inFromClient   = null;
    private DataOutputStream outToClient   = null;
    private String query                   = "";
    private Integer port                   = null;
    private static String workerName              = null;
    String pathToSave;

    public Worker(int port) {
        this.port = port;
        workerName = "Worker" + port % 10;
        pathToSave = "/Users/rusuna/stuff/faculty/M2_S1/PPAD/Project-Storage/Storage/" + workerName;
        Path path = Paths.get(pathToSave);
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

                query = inFromClient.readUTF();

                Gson gson = new GsonBuilder().create();
                ClientTask executeTask = gson.fromJson(query , ClientTask.class);
                System.out.println("Execute query: " + executeTask.toString());

                if(executeTask.getType().equals("save")){
                    outToClient.writeUTF("ACK");
                    String fileData = inFromClient.readUTF();

                    File newFile = new File(pathToSave + "/" + executeTask.getFinalFilename());
                    newFile.createNewFile();
                    FileWriter fileWriter = new FileWriter(newFile);
                    fileWriter.write(fileData);
                    fileWriter.close();
                    outToClient.writeUTF("ACK");

                    //TODO save in the database
                    System.out.println("Save data in mongo:" + executeTask.toString());
                    DBManager.getInstance().saveMapping(executeTask.getFinalFilename(), workerName, executeTask.getVersion());

                    executeTask.removeWorker(workerName);
                    System.out.println("Removed myself from the list:" + executeTask.toString());
                    if(executeTask.getWorkersToSave().size() > 0) {
                        sendTaskToAnotherWorker(executeTask, fileData);
                    }
                }

                System.out.println("Closing connection with " + socket.getPort());

                // close connection
                socket.close();
                inFromClient.close();
            }catch(Exception e) {
                System.out.println("Exceptie try:Connection pipe broken");
                e.printStackTrace();
            }
        }
    }

    public void sendTaskToAnotherWorker(ClientTask task, String fileData) {
        List<WorkerMinimalConnData> workers = task.getWorkersToSave();
        WorkerMinimalConnData mainWorker = workers.get(0);
        try {
            System.out.println("Try to connect with: " + mainWorker.getAddress() + ":" + mainWorker.getPort());
            socket = new Socket(mainWorker.getAddress(), mainWorker.getPort());
            System.out.println("Connected with " + mainWorker.getAddress() + ":" + mainWorker.getPort());

            DataOutputStream output = new DataOutputStream(socket.getOutputStream());
            DataInputStream input = new DataInputStream(socket.getInputStream());

            task.setType("save");

            Gson gson = new GsonBuilder().create();
            String jsonText = gson.toJson(task);
            output.writeUTF(jsonText);
            System.out.println("Sent the task");

            String response = input.readUTF();
            if(response.equals("ACK")){
                System.out.println("Let's send the file :)");

                // TODO send in chunks of bytes
                output.writeUTF(fileData);

                response = input.readUTF();
                if(response.equals("ACK")) {
                    System.out.println("File was saved successfully.");
                }
            }

            System.out.println("Closing connection with " + mainWorker.getAddress() + ":" + mainWorker.getPort());

            // close connection
            socket.close();
            inFromClient.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
