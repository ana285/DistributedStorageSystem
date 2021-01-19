package Core;

import java.util.ArrayList;
import java.util.List;

public class ClientTask {
    private String filename;
    private String finalFilename;
    private List<WorkerMinimalConnData> workersToSave;
    Integer version;

    public ClientTask(){
        filename = null;
        finalFilename = null;
        workersToSave = new ArrayList<WorkerMinimalConnData>();
    }

    public ClientTask(String fn, String ffn){
        filename = fn;
        finalFilename = ffn;
        workersToSave = new ArrayList<WorkerMinimalConnData>();
    }

    public String getFilename() {
        return filename;
    }

    public String getFinalFilename() {
        return finalFilename;
    }

    public List<WorkerMinimalConnData> getWorkersToSave(){
        return workersToSave;
    }

    public void setFilename(String fn) {
        filename = fn;
    }

    public void setFinalFilename(String ffn) {
        finalFilename = ffn;
    }

    public void setWorkersToSave( List<WorkerMinimalConnData> w){
        workersToSave = w;
    }

    public void addWorker(WorkerMinimalConnData w){
        workersToSave.add(w);
    }

    public String getWorkerNames() {
        String finalString = "";
        for(WorkerMinimalConnData w :workersToSave){
            finalString += w.getName() + "; ";
        }
        return finalString;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version){
        this.version = version;
    }

    @Override
    public String toString() {
        return "WorkerConnectionData [filename = " + filename + ", finalfilename=" + finalFilename + ", workers=" + getWorkerNames() + "]";
    }
}
