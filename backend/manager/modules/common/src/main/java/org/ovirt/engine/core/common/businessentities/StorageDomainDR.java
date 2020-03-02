package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class StorageDomainDR implements Serializable{

    private static final long serialVersionUID = 8897241841053545233L;

    private Guid storageDomainId;

    private Guid geoRepSessionId;

    private String scheduleCronExpression;

    private Guid jobId;

    public StorageDomainDR() {
        //require for GWT serialization
    }

    public Guid getStorageDomainId() {
        return storageDomainId;
    }

    public void setStorageDomainId(Guid storageDomainId) {
        this.storageDomainId = storageDomainId;
    }

    public Guid getGeoRepSessionId() {
        return geoRepSessionId;
    }

    public void setGeoRepSessionId(Guid geoRepSessionId) {
        this.geoRepSessionId = geoRepSessionId;
    }

    public String getScheduleCronExpression() {
        return scheduleCronExpression;
    }

    public void setScheduleCronExpression(String syncScheduleExpression) {
        this.scheduleCronExpression = syncScheduleExpression;
    }

    public Guid getJobId() {
        return jobId;
    }

    public void setJobId(Guid jobId) {
        this.jobId = jobId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                storageDomainId,
                geoRepSessionId,
                scheduleCronExpression,
                jobId
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof StorageDomainDR)) {
            return false;
        }
        StorageDomainDR other = (StorageDomainDR) obj;
        return Objects.equals(storageDomainId, other.storageDomainId)
                && Objects.equals(geoRepSessionId, other.geoRepSessionId)
                && Objects.equals(scheduleCronExpression, other.scheduleCronExpression)
                && Objects.equals(jobId, other.jobId);
    }

}
