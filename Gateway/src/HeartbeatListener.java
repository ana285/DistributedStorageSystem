import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import Core.WorkerConnectionData;
import Core.WorkerMinimalConnData;
import db.DBManager;

public class HeartbeatListener extends Thread{
    private List<WorkerMinimalConnData> workers       = null;
    private Socket socket;

    public HeartbeatListener() {
        this.workers = new ArrayList<WorkerMinimalConnData>();
        this.workers = DBManager.getInstance().getWorkers();
    }

    @Override
    public void run() {

        List<Thread> threads = new LinkedList<Thread>();

        for(WorkerMinimalConnData worker : workers) {
            System.out.println("Heartbeat listener:" + worker.getName());
            Thread thread = heartbeatListenerHandler(worker);
            threads.add(thread);
        }
        for(Thread thread : threads) {
            thread.start();
        }

        while(true) {
            List<WorkerMinimalConnData> updatedWorkers = DBManager.getInstance().getWorkers();
            List<WorkerMinimalConnData> difference = new ArrayList<>(updatedWorkers);
            difference.removeAll(workers);
            if(difference.size() != 0) {
                System.out.println("New Workers:" + difference.size());
            }
            for(WorkerMinimalConnData worker : difference) {
                System.out.println("Heartbeat listener:" + worker.getName());
                Thread thread = heartbeatListenerHandler(worker);
                threads.add(thread);
                thread.start();
            }
            if(difference.size() != 0) {
                workers = updatedWorkers;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private Thread heartbeatListenerHandler(WorkerMinimalConnData worker){
        return new Thread() {
            WorkerConnectionData work = new WorkerConnectionData(worker);
            public void run() {
                WorkerConnectionData w = new WorkerConnectionData(work);
                System.out.println("Run thread for " + w.toString());
                try {
                    socket = new Socket(w.getAddress(), w.getHeartbeatPort());
                    w.setSocket(socket);

                    //Log the connection
                    System.out.println("Connected with:" + w.toString() + " heartbeat.");

                    // sends output to the socket
                    DataOutputStream outputMessage = new DataOutputStream(w.getSocket().getOutputStream());
                    DataInputStream inputStream = new DataInputStream(w.getSocket().getInputStream());
                    w.setOutputMessage(outputMessage);
                    w.setInFromWorker(inputStream);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Can't connect to the worker " + w.getName() + ". Error message:" + e.getMessage());
                    for (Iterator<WorkerMinimalConnData> iterator = workers.iterator(); iterator.hasNext();) {
                        WorkerMinimalConnData someWorker = iterator.next();
                        if(someWorker.getName().equals(w.getName())) {
                            iterator.remove();
                            DBManager.getInstance().deleteWorker(w.getName());
                        }
                    }
                    return;
                }

                try {
                    int counter = 9;
                    while(true) {
                        String info = w.getInFromWorker().readUTF();
                        if(counter == 10) {
                            System.out.println(w.getName() + ": " + info);
                            counter = 0;
                        }
                        DBManager.getInstance().updateWorkerHeartbeat(info, w.getName());
                        counter++;
                    }
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    System.out.println("Connection with " + w.getName() + " lost.");
                    handleLostWorker(w);
                }
            }
        };
    }

    public void handleLostWorker(WorkerConnectionData worker){
        DBManager.getInstance().deleteWorker(worker.getName());

        // balance it's files in some other nodes

    }
}