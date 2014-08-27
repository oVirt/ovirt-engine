package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

import org.ovirt.engine.core.compat.Guid;

public class GlusterGeoRepSessionDetails implements Serializable{

    private static final long serialVersionUID = -8084667500866439692L;

    private Guid sessionId;
    private Guid masterBrickId;
    private Guid slaveNodeUuid;
    private String slaveHostName;
    private GeoRepSessionStatus status;
    private String checkPointStatus;
    private GeoRepCrawlStatus crawlStatus;
    private Long dataOpsPending;
    private Long metaOpsPending;
    private Long entryOpsPending;
    private Long failures;
    private Date updatedAt;
    private Date lastSyncedAt;
    private Date checkPointTime;
    private Date checkPointCompletedAt;
    private boolean checkpointCompleted;
    private String masterBrickHostName;

    public String getMasterBrickHostName() {
        return masterBrickHostName;
    }

    public void setMasterBrickHostName(String masterBrickHostName) {
        this.masterBrickHostName = masterBrickHostName;
    }

    public Guid getMasterBrickId() {
        return masterBrickId;
    }

    public void setMasterBrickId(Guid masterBrickId) {
        this.masterBrickId = masterBrickId;
    }

    public Guid getSlaveNodeUuid() {
        return slaveNodeUuid;
    }

    public void setSlaveNodeUuid(Guid slaveNodeUuid) {
        this.slaveNodeUuid = slaveNodeUuid;
    }

    public String getSlaveHostName() {
        return slaveHostName;
    }

    public void setSlaveHostName(String slaveNodeName) {
        this.slaveHostName = slaveNodeName;
    }

    public GeoRepSessionStatus getStatus() {
        return status;
    }

    public void setStatus(GeoRepSessionStatus status) {
        this.status = status;
    }

    public String getCheckPointStatus() {
        return checkPointStatus;
    }

    public void setCheckPointStatus(String checkPointStatus) {
        this.checkPointStatus = checkPointStatus;
    }

    public GeoRepCrawlStatus getCrawlStatus() {
        return crawlStatus;
    }

    public void setCrawlStatus(GeoRepCrawlStatus crawlStatus) {
        this.crawlStatus = crawlStatus;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getDataOpsPending() {
        return dataOpsPending;
    }

    public void setDataOpsPending(Long dataOpsPending) {
        this.dataOpsPending = dataOpsPending;
    }

    public Long getMetaOpsPending() {
        return metaOpsPending;
    }

    public void setMetaOpsPending(Long metaOpsPending) {
        this.metaOpsPending = metaOpsPending;
    }

    public Long getEntryOpsPending() {
        return entryOpsPending;
    }

    public void setEntryOpsPending(Long entryOpsPending) {
        this.entryOpsPending = entryOpsPending;
    }

    public Date getLastSyncedAt() {
        return lastSyncedAt;
    }

    public void setLastSyncedAt(Date lastSyncedAt) {
        this.lastSyncedAt = lastSyncedAt;
    }

    public Date getCheckPointTime() {
        return checkPointTime;
    }

    public void setCheckPointTime(Date checkPointTime) {
        this.checkPointTime = checkPointTime;
    }

    public Date getCheckPointCompletedAt() {
        return checkPointCompletedAt;
    }

    public void setCheckPointCompletedAt(Date checkPointCompletedAt) {
        this.checkPointCompletedAt = checkPointCompletedAt;
    }

    public boolean isCheckpointCompleted() {
        return checkpointCompleted;
    }

    public void setCheckpointCompleted(boolean checkpointCompleted) {
        this.checkpointCompleted = checkpointCompleted;
    }

    public Long getFailures() {
        return failures;
    }

    public void setFailures(Long failures) {
        this.failures = failures;
    }

    public Guid getSessionId() {
        return sessionId;
    }

    public void setSessionId(Guid id) {
        this.sessionId = id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof GlusterGeoRepSessionDetails)) {
            return false;
        }

        GlusterGeoRepSessionDetails geoRep = (GlusterGeoRepSessionDetails) obj;
        return Objects.equals(getSessionId(), geoRep.getSessionId()) &&
                Objects.equals(getMasterBrickId(), geoRep.getMasterBrickId()) &&
                Objects.equals(getSlaveNodeUuid(), geoRep.getSlaveNodeUuid()) &&
                Objects.equals(getSlaveHostName(), geoRep.getSlaveHostName()) &&
                Objects.equals(getStatus(), geoRep.getStatus()) &&
                Objects.equals(getCheckPointStatus(), geoRep.getCheckPointStatus()) &&
                Objects.equals(getCrawlStatus(), geoRep.getCrawlStatus()) &&
                Objects.equals(getDataOpsPending(), geoRep.getDataOpsPending()) &&
                Objects.equals(getMetaOpsPending(), geoRep.getMetaOpsPending()) &&
                Objects.equals(getEntryOpsPending(), geoRep.getEntryOpsPending()) &&
                Objects.equals(getCheckPointCompletedAt(), geoRep.getCheckPointCompletedAt()) &&
                Objects.equals(getCheckPointTime(), geoRep.getCheckPointTime()) &&
                Objects.equals(getLastSyncedAt(), geoRep.getLastSyncedAt()) &&
                Objects.equals(getUpdatedAt(), geoRep.getUpdatedAt()) &&
                Objects.equals(getFailures(), geoRep.getFailures()) &&
                Objects.equals(getMasterBrickHostName(), geoRep.getMasterBrickHostName()) &&
                isCheckpointCompleted() == geoRep.isCheckpointCompleted();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        result = prime * result + ((masterBrickId == null) ? 0 : masterBrickId.hashCode());
        result = prime * result + ((slaveNodeUuid == null) ? 0 : slaveNodeUuid.hashCode());
        result = prime * result + ((slaveHostName == null) ? 0 : slaveHostName.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        result = prime * result + ((checkPointStatus == null) ? 0 : checkPointStatus.hashCode());
        result = prime * result + ((crawlStatus == null) ? 0 : crawlStatus.hashCode());
        result = prime * result + ((dataOpsPending == null) ? 0 : dataOpsPending.hashCode());
        result = prime * result + ((metaOpsPending == null) ? 0 : metaOpsPending.hashCode());
        result = prime * result + ((entryOpsPending == null) ? 0 : entryOpsPending.hashCode());
        result = prime * result + ((checkPointCompletedAt == null) ? 0 : checkPointCompletedAt.hashCode());
        result = prime * result + ((checkPointTime == null) ? 0 : checkPointTime.hashCode());
        result = prime * result + (checkpointCompleted ? 1 : 0);
        result = prime * result + ((lastSyncedAt == null) ? 0 : lastSyncedAt.hashCode());
        result = prime * result + ((failures == null) ? 0 : failures.hashCode());
        result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
        result = prime * result + ((masterBrickHostName == null) ? 0 : masterBrickHostName.hashCode());
        return result;
    }
}
