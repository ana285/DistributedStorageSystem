import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.reflect.Method;
import com.google.gson.Gson;
import Core.HeartbeatInfo;

import javax.management.*;

public class Heartbeat extends Thread{
    private Integer port;
    private ServerSocket server            = null;
    private Socket socket                  = null;
    private DataInputStream inFromClient;
    private DataOutputStream outToClient;
    private String workerName = null;

    public Heartbeat(Integer port, String name){
        this.port = port;
        this.workerName = name;
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            }
            else {
                length += getFolderSize(files[i]);
            }
        }
        return length;
    }

    static String cmdTop = "top -n 2 -b -d 0.2";

    private static double getCpuLoad() {
        Object value = null;

        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
            value = getValue(server, name,"SystemCpuLoad");
        } catch(Exception e){
            e.printStackTrace();
        }
        return (double) value;
    }

    public static Object getValue(MBeanServer server, ObjectName name,
                                  String attrName) throws ReflectionException, InstanceNotFoundException {
        AttributeList attrs =
                server.getAttributes(name, new String[]{attrName});

        Object value = null;
        if (!attrs.isEmpty()) {
            Attribute att = (Attribute) attrs.get(0);
            value = att.getValue();
        }

        return value;
    }

    @Override
    public void run() {
        try {
            server = new ServerSocket(port);
            System.out.println("Heartbeat server started! ");
            System.out.println("Waiting for a client...");
            while (true)
            {
                socket = server.accept();
                System.out.println("A new client is connected for heartbeat: " + socket);

                try {
                    this.inFromClient = new DataInputStream(socket.getInputStream());
                    this.outToClient = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                try {
                    while(true) {
                        try {
                            HeartbeatInfo info = new HeartbeatInfo();
                            File folder = new File("/Users/rusuna/stuff/faculty/M2_S1/PPAD/Project-Storage/Storage/" + workerName);

                            Long size = getFolderSize(folder);
                            info.setOcupiedStorage(size.doubleValue());
                            Double cpu = getCpuLoad() * 100;
                            info.setCPU(cpu);

                            Gson gson = new Gson();
                            String jsonText = gson.toJson(info);
                            outToClient.writeUTF(jsonText);

                            Thread.sleep(1000);

                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    System.out.println("Heartbeat connection lost");
                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            System.out.println("Server socket was not started.");
        }
    }

}
