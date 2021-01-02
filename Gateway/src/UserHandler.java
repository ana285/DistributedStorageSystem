import Core.User;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import db.DBManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class UserHandler extends Thread implements Responder {
    private DataInputStream inFromClient   = null;
    private DataOutputStream outToClient = null;
    private Socket socket                  = null;
    private String query                   = null;
    private User user                      = null;
    private LoadBalancer lb         = null;
    private boolean connected;

    public UserHandler(Socket socket, LoadBalancer lb) throws IOException {
        this.socket = socket;
        this.inFromClient = new DataInputStream(socket.getInputStream());
        this.outToClient = new DataOutputStream(socket.getOutputStream());
        this.lb = lb;
        this.connected = false;
    }

    public boolean connect() {
        boolean result = false;
        try {
            System.out.println("Try to connect");
            query = inFromClient.readUTF();
            System.out.println("Read: " + query);

            result = verifyCredentials(query);
            if(result == false) {
                outToClient.writeUTF("fail");
                socket.close();
                return false;

            }else {
                outToClient.writeUTF("success");
                return true;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    private boolean verifyCredentials(String query) {
        boolean result = false;
        Gson gson = new GsonBuilder().create();
        user = gson.fromJson(query , User.class);
        System.out.println("Connection for: " + user.toString());

        User dbUser = DBManager.getInstance().getUser(user.getUsername());
        //verify if the database exists and make it active if yes
        if(dbUser == null) {
            return false;
        }else {
            System.out.println("Database user: " + dbUser.toString() + user.getPassword() + "_" + dbUser.getPassword());
            if(!user.getPassword().equals(dbUser.getPassword())) {
                System.out.println("Credentials don't match!");
                return false;
            }
            if(user.getDatabase() != dbUser.getDatabase()) { //set different active db
                DBManager.getInstance().UpdateUserDatabase(dbUser.getUsername(), user.getDatabase());
                result = true;
            }
            user = dbUser;
            System.out.println("Updated user: " + DBManager.getInstance().getUser(user.getUsername()).toString());
        }
        return result;
    }

    public void run() {

        connected = connect();
        if(connected == false) {
            return;
        }
        while(true) {
            while (!query.equals("exit")) {
                try {
                    query = inFromClient.readUTF();
                    System.out.println("User sent:" + query);
                    query = changeDatabaseName(query);
                    Task task = new Task(query, user);
                    lb.addTaskToQueue(task, this);

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    System.out.println("Exceptie: " + e.getMessage());
                    return;
                }
            }
        }
    }

    private String changeDatabaseName(String query) {
        return query.replaceAll(user.getDatabase(), user.getUsername() + "_" + user.getDatabase());
    }

    @Override
    public void respond(String rs) {
        try {
            outToClient.writeUTF(rs);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
