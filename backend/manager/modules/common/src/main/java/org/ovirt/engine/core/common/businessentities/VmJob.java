package org.ovirt.engine.core.common.businessentities;

import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class VmJob implements BusinessEntity<Guid> {

    private static final long serialVersionUID = -1748312497527481706L;
    private Guid id;
    private Guid vmId;
    private VmJobState jobState;
    private VmJobType jobType;
    private long startTime;

    public VmJob() {
        id = Guid.Empty;
        vmId = Guid.Empty;
        jobState = VmJobState.UNKNOWN;
        jobType = VmJobType.UNKNOWN;
    }

    @Override
    public Guid getId() {
        return id;
    }

    @Override
    public void setId(Guid id) {
        this.id = id;
    }

    public Guid getVmId() {
        return vmId;
    }

    public void setVmId(Guid id) {
        this.vmId = id;
    }

    public VmJobState getJobState() {
        return jobState;
    }

    public void setJobState(VmJobState jobState) {
        this.jobState = jobState;
    }

    public VmJobType getJobType() {
        return jobType;
    }

    public void setJobType(VmJobType jobType) {
        this.jobType = jobType;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VmJob)) {
            return false;
        }
        VmJob other = (VmJob) obj;
        return Objects.equals(id, other.id)
                && Objects.equals(vmId, other.vmId)
                && jobType == other.jobType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                id,
                vmId,
                jobType
        );
    }

    @Override
    public String toString() {
        return "VM Job [" + getId() +"]";
    }
}
