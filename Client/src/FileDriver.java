import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import Core.ClientTask;
import Core.WorkerMinimalConnData;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.*;

public class FileDriver {
    private String       username;
    private String       password;
    private String       url;
    private String       host;
    private int          port;
    Socket               socket;
    DataOutputStream   outputMessageForServer;
    DataInputStream      inputStreamFromServer;

    FileDriver(String username, String pass, String url) {
        this.username =        username;
        this.password =        pass;
        this.url =             url; // "host:port"
        host = url.split(":")[0];
        port = Integer.parseInt(url.substring(url.indexOf(':')+1, url.length()));
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }


    public boolean connect(){
        boolean result = false;
        try {
            System.out.println("Try to connect with: " + host + ":" + port);
            socket = new Socket(host ,port);
            System.out.println("Connected with " + host + ":" + port);

            outputMessageForServer = new DataOutputStream(socket.getOutputStream());
            inputStreamFromServer = new DataInputStream(socket.getInputStream());

            Gson gson = new GsonBuilder().create();
            String jsonText = gson.toJson(new User(username, password));
            outputMessageForServer.writeUTF(jsonText);
            System.out.println("Sent");

            String connectionResult = inputStreamFromServer.readUTF();
            System.out.println("Recieved:" + connectionResult);
            if(connectionResult.equals("success")) {
                result = true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return result;
    }

    public boolean handleQuery(String query) {
        try {
            if(query.trim().isEmpty()){
                return true;
            }
            outputMessageForServer.writeUTF(query);

            System.out.println("Recieved:");
            String connectionResult;
            boolean finished = false;
            if(query.toLowerCase().startsWith("select")) {
                do {
                    connectionResult = inputStreamFromServer.readUTF();
                    System.out.println(connectionResult);

                } while (connectionResult.length() != 16 * 1024 && !connectionResult.equals("finish"));
            }else{
                connectionResult = inputStreamFromServer.readUTF();
                System.out.println(connectionResult);
            }


            return true;
        } catch (IOException e) {
            System.out.println("Conection lost. " + e.getMessage());
            return false;
        }

    }

    public void handleUpload() {
        System.out.println("handle Upload");
        JFileChooser jfc = new JFileChooser();
        jfc.showDialog(null,"Please Select the File");
        jfc.setVisible(true);
        File filename = jfc.getSelectedFile();
        System.out.println("File name " + filename.getName());

        String query = "save " + filename.getName();
        String response;
        try {
            outputMessageForServer.writeUTF(query);
            System.out.println("Message sent to the server: " + query);
            response = inputStreamFromServer.readUTF();
            Gson gson = new GsonBuilder().create();
            ClientTask task = gson.fromJson(response, ClientTask.class);
            task.setUsername(username);
            System.out.println("What I received: " + task.toString());
            handleUploadTask(task, filename.getAbsolutePath());
        } catch (IOException e) {
           System.out.println("Something went wrong while trying to save the file.");
        }

    }

    public void handleDisplayFiles() {
        System.out.println("handleDisplayFiles();");
    }

    public void handleDownloadFile() {
        System.out.println("handleDownloadFile();");
    }

    public void handleUploadTask(ClientTask task, String absolutePath){
        List<WorkerMinimalConnData> workers = task.getWorkersToSave();
        if(workers.size() > 0) {
            WorkerMinimalConnData mainWorker = workers.get(0);
            // task.removeWorkerFromList(0);
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
                    String data = "";
                    data = new String(Files.readAllBytes(Paths.get(absolutePath)));
                    output.writeUTF(data);

                    response = input.readUTF();
                    if(response.equals("ACK")) {
                        System.out.println("File was saved successfully.");
                    }
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            System.out.println("Server returned no workers to save the file on!");
        }
    }
}