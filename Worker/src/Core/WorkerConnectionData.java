package Core;

public class WorkerConnectionData{

    private String name;
    private String id;
    private String address =                 null;
    private Integer port =                   null;
    private Integer heartbeatPort =          null;

    public WorkerConnectionData(String address, Integer port, Integer heartbeatPort, String name) {
        super();
        this.address = address;
        this.port = port;
        this.heartbeatPort = heartbeatPort;
        this.name = name;
    }

    public WorkerConnectionData(WorkerConnectionData w){
        this.name = w.name;
        this.id = w.id;
        this.address = w.address;
        this.port = w.port;
        this.heartbeatPort = w.heartbeatPort;
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

    @Override
    public String toString() {
        return "WorkerConnectionData [name = " + name + ", address=" + address + ", port=" + port + ", heartbeat port=" +
                heartbeatPort + "]";
    }
}
