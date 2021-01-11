import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientListener extends Thread{
    private Socket socket                  = null;
    private ServerSocket server            = null;
    private LoadBalancer lb                = null;

    public ClientListener(LoadBalancer lb) {
        this.lb = lb;
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        try {
            server = new ServerSocket(5051);
            System.out.println("Server started on port 5051. Waiting connections...");
            while (true)
            {
                socket = null;
                try
                {
                    socket = server.accept();
                    System.out.println("A new client is connected : " + socket);

                    System.out.println("Assigning new thread for " + socket);

                    Thread userThread = new UserHandler(socket, lb);
                    userThread.start();
                }
                catch (Exception e){
                    //server.close();
                    e.printStackTrace();
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
}