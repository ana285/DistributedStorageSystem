import Core.Task;

public interface LoadBalancer extends Runnable {
    public void addTaskToQueue(Task task, Responder responder);
}
