package Core;

import java.util.Locale;

public class Task {
    private int taskId;
    private String query;
    private User user;
    private String fileName;
    private Query_type type;

    public Task(int id, String query, User user){
        this.taskId = id;
        this.query = query;
        this.user = user;
    }

    public Task(String query, User user){
        this.query = query;
        this.user = user;
    }

    public String getFileName(){
        return fileName;
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

    public Query_type getType(){
        return type;
    }

    public void setType(Query_type type){
        this.type = type;
    }

    public boolean findType() {
        if(this.query.length() <= 0) {
            return false;
        }
        query = query.trim();
        if(checkSaveFile(this.query)) {
            this.type = Query_type.SAVE;
            return true;
        }
        if(checkGetAllFiles(this.query)) {
            this.type = Query_type.GET_ALL;
            return true;
        }
        if(checkGetFile(this.query)) {
            this.type = Query_type.GET;
            return true;
        }

        return false;
    }

    private boolean checkSaveFile(String query2) {
        String[] splitStr = query2.trim().split("\\s+");
        if(splitStr[0].toLowerCase().equals("save") && splitStr[1].length() > 0){
            this.fileName = splitStr[1];
            return true;
        }
        return false;
    }

    private boolean checkGetAllFiles(String query2) {
        String[] splitStr = query2.trim().split("\\s+");
        if(splitStr[0].toLowerCase().equals("get-all")){
            return true;
        }
        return false;
    }

    private boolean checkGetFile(String query2) {
        String[] splitStr = query2.trim().split("\\s+");
        if(splitStr[0].toLowerCase().equals("get") && splitStr[1].length() > 0){
            this.fileName = splitStr[1];
            return true;
        }
        return false;
    }
}
