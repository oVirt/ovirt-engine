package org.ovirt.engine.core.common.businessentities;

import org.ovirt.engine.core.common.errors.VDSError;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class HostJobInfo {
    private Guid id;
    private String description;
    private HostJobType type;
    private HostJobStatus status;
    private Integer progress;
    private VDSError error;

    public HostJobInfo(Guid id, String description, HostJobType type,
                       HostJobStatus status, Integer progress, VDSError error) {
        this.id = id;
        this.description = description;
        this.type = type;
        this.status = status;
        this.progress = progress;
        this.error = error;
    }

    public HostJobInfo() {
    }

    public VDSError getError() {
        return error;
    }

    public void setError(VDSError error) {
        this.error = error;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public HostJobType getType() {
        return type;
    }

    public void setType(HostJobType type) {
        this.type = type;
    }

    public HostJobStatus getStatus() {
        return status;
    }

    public void setStatus(HostJobStatus status) {
        this.status = status;
    }

    public Guid getId() {
        return id;
    }

    public void setId(Guid id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public enum HostJobType {
        storage, v2v, virt
    }

    public enum HostJobStatus {
        pending, running, done, aborted, failed;

        public boolean isAlive() {
            return this == running || this == pending;
        }
    }

    @Override
    public String toString() {
        return ToStringBuilder.forInstance(this)
                .append("id", getId())
                .append("type", getType())
                .append("description", getDescription())
                .append("status", getStatus())
                .append("progress", getProgress())
                .append("error", getError())
                .build();
    }
}
