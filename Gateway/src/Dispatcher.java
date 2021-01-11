import db.DBManager;

public class Dispatcher{

    public static void main(String args[])
    {
        HeartbeatListener heartbeatListener = new HeartbeatListener();
        LoadBalancer lb = new RoundRobinLoadBalancer(heartbeatListener);
        Thread clientListener = new ClientListener(lb);

        (new Thread(lb)).start();
        clientListener.start();
    }
}