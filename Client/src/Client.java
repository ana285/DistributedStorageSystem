import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {
    private boolean connected = false;
    private static BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public FileDriver readConnectionData() {
        try {
            System.out.println("Insert connection credentials");
            System.out.print("Insert url <host:port>:");
            //TODO REGEX for url
            String url = input.readLine();
            System.out.print("Insert username:");
            String username = input.readLine();
            System.out.print("Insert password:");
            String pass = input.readLine();
            FileDriver fileDriver = new FileDriver(username, pass, url);
            return fileDriver;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static void displayMenu() {
        System.out.println("\nPress a number to select an option. Your next action will be:");
        System.out.println("1. Upload a file");
        System.out.println("2. See all your files");
        System.out.println("3. Download a file");
        System.out.println("4. Exit");
        System.out.print("Insert option:");
    }


    public static void main(String [] args) {

        Client clientApp = new Client();
        FileDriver fileDriver = null;

        while(!clientApp.isConnected()) {
            fileDriver = clientApp.readConnectionData();
            while(fileDriver == null) {
                fileDriver = clientApp.readConnectionData();
            }
            clientApp.setConnected(fileDriver.connect());
        }

        // client is connected so he can use the storage now
        // TODO -> create a web client for this part
        System.out.println("Start saving your files:");
        while(true) {
            displayMenu();
            try {
                String usersOption = input.readLine();
                switch(usersOption.trim()){
                    case "1":
                        fileDriver.handleUpload();
                        break;
                    case "2":
                        fileDriver.handleDisplayFiles();
                        break;
                    case "3":
                        fileDriver.handleDownloadFile();
                        break;
                    case "4":
                        return;
                }
            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                return;
            }
        }

    }
}