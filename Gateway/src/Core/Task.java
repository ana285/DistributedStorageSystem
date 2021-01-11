package Core;

public class Task {
    private int taskId;
    private String query;
    private User user;

    public Task(int id, String query, User user){
        this.taskId = id;
        this.query = query;
        this.user = user;
    }

    public Task(String query, User user){
        this.query = query;
        this.user = user;
    }


    public int getTaskId() {
        return taskId;
    }
    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }
    public String getQuery() {
        return query;
    }
    public void setQuery(String query) {
        this.query = query;
    }
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
}
