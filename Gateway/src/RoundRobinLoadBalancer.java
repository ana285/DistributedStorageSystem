import Core.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import db.DBManager;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.io.FilenameUtils;


public class RoundRobinLoadBalancer implements LoadBalancer, ResponderProcess {

    private Map<Task, Responder> responderMap        = null;
    private BlockingQueue<Task> taskQueue            = null;
    private Integer replicationFactor;
    private Integer distributionFactor;
    HeartbeatListener heartbeatListener;



    public RoundRobinLoadBalancer(HeartbeatListener hl) {
        //        addUsers();
        responderMap = new ConcurrentHashMap<>();
        taskQueue = new LinkedBlockingQueue<>();

//        this.workers = new ArrayList<WorkerConnectionData>();
//        this.workers = DBManager.getInstance().getWorkers();
//        connectToWorkers(workers);

        replicationFactor = 2;
        distributionFactor = 10000;
        heartbeatListener = hl;
        heartbeatListener.start();
    }

    public void addUsers() { //for testing and debug TODO: web app for account creation
        User user1 = new User("ana", "parola");
        User user2 = new User("zava", "123456");
        User user3 = new User("paul", "parola");

        DBManager.getInstance().addUser(user1);
        DBManager.getInstance().addUser(user2);
        DBManager.getInstance().addUser(user3);
    }

    public synchronized void addTaskToQueue(Task task, Responder responder) {
        responderMap.put(task, responder);
        taskQueue.add(task);
    }

    @Override
    public void run() {
        while (true)
        {
            try {
                Task t = taskQueue.take();
                System.out.println("Task luat: " + t.getQuery() + " | " + t.getUser().getUsername());

                if(t.findType() == false) {
                    System.out.println(t.getQuery() + " is wrong!!!");
                    processResponder(t).respond("Wrong syntax");
                    continue;
                }

                Gson gson = new GsonBuilder().create();

                if(t.getType() == Query_type.SAVE) {
                    System.out.println("Execute save :D");
                    executeSave(t);
                    continue;
                }

                if(t.getType() == Query_type.GET_ALL){
                    executeGetAllFiles(t);
                    continue;
                }

                if(t.getType() == Query_type.GET) {
                    executeGetFile(t);
                }

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

        }
    }

    private void executeGetFile(Task t) {

    }

    private void executeGetAllFiles(Task t) {
    }

    private void executeSave(Task t) {
        String mappingId = t.getUser().getUsername() + "_" + t.getFileName();
        List<MappingInfo> workerList = DBManager.getInstance().getWorkersForFile(mappingId);
        if(workerList != null && workerList.size() > 0){
            // it is already saved, update the version
            ClientTask clientTask = new ClientTask(t.getFileName(), mappingId);
            for(MappingInfo info : workerList){
                System.out.println("Mapping info:" + info.toString());
                WorkerMinimalConnData worker = DBManager.getInstance().getWorker(info.getWorker());
                if(worker == null) {
                    processResponder(t).respond("fail");
                    return;
                }
                System.out.println(worker.getName() + " *** " + worker.toString());
                clientTask.setVersion(info.getVersion() + 1);
                clientTask.addWorker(worker);
            }
            Gson gson = new Gson();
            String jsonText = gson.toJson(clientTask);
            processResponder(t).respond(jsonText);
        } else {
            // the first version. Decide where to save the file
            List<WorkerConnectionData> workersForSave = chooseWorkersForSplit(t);
            System.out.println("Workers selected for saving " + mappingId);

//            String extension = FilenameUtils.getExtension(mappingId);
//            String fileNameWithoutExt = FilenameUtils.removeExtension(mappingId);

            ClientTask clientTask = new ClientTask(t.getFileName(), mappingId);
            clientTask.setVersion(1);
            for(WorkerConnectionData worker : workersForSave){
                System.out.println(worker.getName() + " *** " + worker.toString());
                clientTask.addWorker(new WorkerMinimalConnData(worker));
            }
            Gson gson = new Gson();
            String jsonText = gson.toJson(clientTask);
            processResponder(t).respond(jsonText);
        }
    }

    private String getFileExtension(String name) {
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    @Override
    public Responder processResponder(Task t) {
        return responderMap.remove(t);
    }

    private List<WorkerConnectionData> chooseWorkersForSplit(Task t) {
        List<WorkerConnectionData> workersForSplit = new ArrayList<>();
        List<String> usedWorkersNames = DBManager.getInstance().getUsedWorkerNames(t.getUser().getUsername());

        PriorityQueue<WorkerConnectionData> pUsedWorkers = new PriorityQueue<WorkerConnectionData>();
        PriorityQueue<WorkerConnectionData> pUnusedWorkers = new PriorityQueue<WorkerConnectionData>();

        List<WorkerMinimalConnData> updatedWorkers = DBManager.getInstance().getMinimalWorkers();
        for(WorkerMinimalConnData w : updatedWorkers) {
            System.out.println("Updated worker:" + w.toString());
            if(usedWorkersNames.contains(w.getName())) {
                System.out.println("Used worker:" + w.getName());
                pUsedWorkers.add(new WorkerConnectionData(w));
            }else {
                System.out.println("Unused worker:" + w.getName());
                pUnusedWorkers.add(new WorkerConnectionData(w));
            }
        }

        int counter = 0;
        Iterator<WorkerConnectionData> itr = pUnusedWorkers.iterator();
        Iterator<WorkerConnectionData> itr2 = pUsedWorkers.iterator();

        while(counter < replicationFactor) {
            if(itr.hasNext()) {
                workersForSplit.add(itr.next());
            }else if(itr2.hasNext()) {
                workersForSplit.add(itr2.next());
            }else {
                return null;
            }
            counter++;
        }

        return workersForSplit;
    }
}
