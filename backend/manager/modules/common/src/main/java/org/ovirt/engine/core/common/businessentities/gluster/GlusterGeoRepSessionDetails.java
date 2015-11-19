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
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterGeoRepSessionDetails)) {
            return false;
        }
        GlusterGeoRepSessionDetails geoRep = (GlusterGeoRepSessionDetails) obj;
        return Objects.equals(sessionId, geoRep.sessionId) &&
                Objects.equals(masterBrickId, geoRep.masterBrickId) &&
                Objects.equals(slaveNodeUuid, geoRep.slaveNodeUuid) &&
                Objects.equals(slaveHostName, geoRep.slaveHostName) &&
                Objects.equals(status, geoRep.status) &&
                Objects.equals(checkPointStatus, geoRep.checkPointStatus) &&
                Objects.equals(crawlStatus, geoRep.crawlStatus) &&
                Objects.equals(dataOpsPending, geoRep.dataOpsPending) &&
                Objects.equals(metaOpsPending, geoRep.metaOpsPending) &&
                Objects.equals(entryOpsPending, geoRep.entryOpsPending) &&
                Objects.equals(checkPointCompletedAt, geoRep.checkPointCompletedAt) &&
                Objects.equals(checkPointTime, geoRep.checkPointTime) &&
                Objects.equals(lastSyncedAt, geoRep.lastSyncedAt) &&
                Objects.equals(updatedAt, geoRep.updatedAt) &&
                Objects.equals(failures, geoRep.failures) &&
                Objects.equals(masterBrickHostName, geoRep.masterBrickHostName) &&
                checkpointCompleted == geoRep.checkpointCompleted;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sessionId,
                masterBrickId,
                slaveNodeUuid,
                slaveHostName,
                status,
                checkPointStatus,
                crawlStatus,
                dataOpsPending,
                metaOpsPending,
                entryOpsPending,
                checkPointCompletedAt,
                checkPointTime,
                checkpointCompleted,
                lastSyncedAt,
                failures,
                updatedAt,
                masterBrickHostName
        );
    }
}
