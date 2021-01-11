import Core.Task;
import Core.User;
import db.DBManager;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class RoundRobinLoadBalancer implements LoadBalancer, ResponderProcess {

    private Map<Task, Responder> responderMap        = null;
    private BlockingQueue<Task> taskQueue            = null;
    private Integer replicationFactor;
    private Integer distributionFactor;
    HeartbeatListener heartbeatListener;



    public RoundRobinLoadBalancer(HeartbeatListener hl) {
        //        addWorkersToDB();
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

    }

    @Override
    public Responder processResponder(Task t) {
        return null;
    }
}
