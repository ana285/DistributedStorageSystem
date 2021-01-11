package Core;

public class HeartbeatInfo {

    private Double ocupiedStorage;
    private Double CPU;
    private Integer memory;

    public HeartbeatInfo() {
        this.ocupiedStorage = null;
        CPU = null;
        this.memory = null;
    }

    public HeartbeatInfo(Double ocupiedStorage, Double cPU, Integer memory) {
        super();
        this.ocupiedStorage = ocupiedStorage;
        CPU = cPU;
        this.memory = memory;
    }

    public Double getOcupiedStorage() {
        return ocupiedStorage;
    }

    public void setOcupiedStorage(Double ocupiedStorage) {
        this.ocupiedStorage = ocupiedStorage;
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

    public void setMemory(int load) {
        this.memory = load;
    }

    @Override
    public String toString() {
        return "HeartbeatInfo [ocupiedStorage=" + ocupiedStorage + ", CPU=" + CPU + ", memory=" + memory + "]";
    }
}
