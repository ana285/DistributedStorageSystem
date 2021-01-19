package Core;

public class WorkerMinimalConnData{

    private String name;
    private String id;
    private String address =                 null;
    private Integer port =                   null;
    private Integer heartbeatPort =          null;
    private Double storage =                 null;
    private Double CPU =                     null;
    private Integer memory =                 null;

    public WorkerMinimalConnData(String address, Integer port, Integer heartbeatPort, String name) {
        super();
        this.address = address;
        this.port = port;
        this.heartbeatPort = heartbeatPort;
        this.name = name;
    }

    public WorkerMinimalConnData(WorkerMinimalConnData w){
        this.name = w.name;
        this.id = w.id;
        this.address = w.address;
        this.port = w.port;
        this.heartbeatPort = w.heartbeatPort;
        this.storage = w.storage;
        this.CPU = w.CPU;
        this.memory = w.memory;
    }

    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public Integer getPort() {
        return port;
    }
    public void setPort(Integer port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHeartbeatPort() {
        return heartbeatPort;
    }

    public void setHeartbeatPort(Integer heartbeatPort) {
        this.heartbeatPort = heartbeatPort;
    }

    public Double getStorage() {
        return storage;
    }
    public Double getCPU() {
        return CPU;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setStorage(Double s) {
        storage = s;
    }
    public void getCPU(Double c) {
        CPU = c;
    }

    public void setMemory(Integer mem) {
        memory = mem;
    }

    @Override
    public String toString() {
        return "WorkerConnectionData [name = " + name + ", address=" + address + ", port=" + port + ", heartbeat port=" +
                heartbeatPort + "]";
    }

    @Override
    public boolean equals(Object obj) {
        WorkerMinimalConnData worker = (WorkerMinimalConnData)obj;

        if ( this.name.equalsIgnoreCase(worker.getName())){
            return true;
        }
        return false;
    }
}
