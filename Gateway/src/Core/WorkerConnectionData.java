package Core;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class WorkerConnectionData implements Comparable<WorkerConnectionData>{

    private String name;
    private String id;
    private String address =                 null;
    private Integer port =                   null;
    private Integer heartbeatPort =          null;
    private Socket socket =                  null;
    private DataOutputStream outputMessage = null;
    private DataInputStream inFromWorker =   null;
    private Double storage =                 null;
    private Double CPU =                     null;
    private Integer memory =                 null;
    private boolean used;

    public WorkerConnectionData(String address, Integer port, Integer heartbeatPort) {
        super();
        this.address = address;
        this.port = port;
        this.heartbeatPort = heartbeatPort;
        setUsed(false);
    }

    public WorkerConnectionData(WorkerConnectionData w){
        this.name = w.name;
        this.id = w.id;
        this.address = w.address;
        this.port = w.port;
        this.heartbeatPort = w.heartbeatPort;
        this.socket = w.socket;
        this.outputMessage = w.outputMessage;
        this.inFromWorker = w.inFromWorker;
        this.storage = w.storage;
        this.CPU = w.CPU;
        this.memory = w.memory;
        this.used = w.used;
    }

    public WorkerConnectionData(WorkerMinimalConnData w){
        this.name = w.getName();
        this.port = w.getPort();
        this.heartbeatPort = w.getHeartbeatPort();
        this.address = w.getAddress();
        this.CPU = w.getCPU();
        this.storage = w.getStorage();
        this.memory = w.getMemory();
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

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public DataOutputStream getOutputMessage() {
        return outputMessage;
    }

    public void setOutputMessage(DataOutputStream outputMessage) {
        this.outputMessage = outputMessage;
    }

    public DataInputStream getInFromWorker() {
        return inFromWorker;
    }

    public void setInFromWorker(DataInputStream inFromWorker) {
        this.inFromWorker = inFromWorker;
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
    public Double getOcupiedStorage() {
        return storage;
    }

    public void setOcupiedStorage(Double ocupiedStorage) {
        this.storage = ocupiedStorage;
    }

    public Double getCPU() {
        return CPU;
    }

    public void setCPU(Double cPU) {
        CPU = cPU;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer memory) {
        this.memory = memory;
    }
    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }


    @Override
    public String toString() {
        return "WorkerConnectionData [name = " + name + ", address=" + address + ", port=" + port + ", heartbeat port=" +
                heartbeatPort + ", ocupiedStorage=" + storage + ", CPU=" + CPU + ", memory=" + memory + "]";

    }

    @Override
    public int compareTo(WorkerConnectionData arg0) {
        return Double.compare(this.storage, arg0.getOcupiedStorage());
    }


}
