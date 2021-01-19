package Core;

public class MappingInfo {
    private String workerName;
    private String mappingId;
    private Integer version;

    public String getWorker() {
        return workerName;
    }

    public void setWorker(String workerName) {
        this.workerName = workerName;
    }

    public String getMappingId() {
        return mappingId;
    }

    public void setMappingId(String mappingId) {
        this.mappingId = mappingId;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer v) {
        version = v;
    }

    @Override
    public String toString() {
        return "MappingInfo [workername = " + workerName + ", mappingId=" + mappingId + ", version=" + version + "]";
    }
}
