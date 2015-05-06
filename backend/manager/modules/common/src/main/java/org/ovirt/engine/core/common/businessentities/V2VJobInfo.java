package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.compat.Guid;

public class V2VJobInfo implements BusinessEntity<Guid> {

    private Guid vmId;
    private JobStatus status;
    /** description of the current state of the job */
    private String description;
    /** overall progress */
    private int progress;

    public enum JobStatus {
        WAIT_FOR_START,
        STARTING,
        COPYING_DISK,
        DONE,
        ERROR,
        ABORTED,
        UNKNOWN,
        NOT_EXIST
    }

    public V2VJobInfo() {
    }

    public V2VJobInfo(Guid vmId, JobStatus status) {
        this.vmId = vmId;
        this.status = status;
    }

    /**
     * @return true if the job was monitored, false otherwise
     */
    public boolean isMonitored() {
        return status != JobStatus.WAIT_FOR_START;
    }

    @Override
    public Guid getId() {
        return vmId;
    }

    @Override
    public void setId(Guid id) {
        this.vmId = id;
    }

    public JobStatus getStatus() {
        return status;
    }

    public void setStatus(JobStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof V2VJobInfo) {
            return vmId.equals(((V2VJobInfo) obj).vmId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return vmId.hashCode();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
