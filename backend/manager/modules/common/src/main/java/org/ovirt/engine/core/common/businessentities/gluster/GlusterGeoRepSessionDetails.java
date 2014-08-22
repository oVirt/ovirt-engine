package org.ovirt.engine.core.common.businessentities.gluster;

import java.io.Serializable;
import java.util.Date;

import org.ovirt.engine.core.common.utils.ObjectUtils;
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
    private Long filesSynced;
    private Long filesPending;
    private Long bytesPending;
    private Long deletesPending;
    private Long filesSkipped;
    private Date updatedAt;

    public Long getFilesSynced() {
        return filesSynced;
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

    public void setFilesSynced(Long filesSynced) {
        this.filesSynced = filesSynced;
    }

    public Long getFilesPending() {
        return filesPending;
    }

    public void setFilesPending(Long filesPending) {
        this.filesPending = filesPending;
    }

    public Long getBytesPending() {
        return bytesPending;
    }

    public void setBytesPending(Long bytesPending) {
        this.bytesPending = bytesPending;
    }

    public Long getDeletesPending() {
        return deletesPending;
    }

    public void setDeletesPending(Long deletesPending) {
        this.deletesPending = deletesPending;
    }

    public Long getFilesSkipped() {
        return filesSkipped;
    }

    public void setFilesSkipped(Long filesSkipped) {
        this.filesSkipped = filesSkipped;
    }

    public Guid getSessionId() {
        return sessionId;
    }

    public void setSessionId(Guid id) {
        this.sessionId = id;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) &&
                (obj instanceof GlusterGeoRepSessionDetails) &&
                ObjectUtils.objectsEqual(getSessionId(), ((GlusterGeoRepSessionDetails) obj).getSessionId()) &&
                ObjectUtils.objectsEqual(getMasterBrickId(), ((GlusterGeoRepSessionDetails) obj).getMasterBrickId()) &&
                ObjectUtils.objectsEqual(getSlaveNodeUuid(), ((GlusterGeoRepSessionDetails) obj).getSlaveNodeUuid()) &&
                ObjectUtils.objectsEqual(getSlaveHostName(), ((GlusterGeoRepSessionDetails) obj).getSlaveHostName()) &&
                ObjectUtils.objectsEqual(getStatus(), ((GlusterGeoRepSessionDetails) obj).getStatus()) &&
                ObjectUtils.objectsEqual(getCheckPointStatus(), ((GlusterGeoRepSessionDetails) obj).getCheckPointStatus()) &&
                ObjectUtils.objectsEqual(getCrawlStatus(), ((GlusterGeoRepSessionDetails) obj).getCrawlStatus()) &&
                ObjectUtils.objectsEqual(getFilesSynced(), ((GlusterGeoRepSessionDetails) obj).getFilesSynced()) &&
                ObjectUtils.objectsEqual(getFilesPending(), ((GlusterGeoRepSessionDetails) obj).getFilesPending()) &&
                ObjectUtils.objectsEqual(getFilesSkipped(), ((GlusterGeoRepSessionDetails) obj).getFilesSkipped()) &&
                ObjectUtils.objectsEqual(getBytesPending(), ((GlusterGeoRepSessionDetails) obj).getBytesPending()) &&
                ObjectUtils.objectsEqual(getDeletesPending(), ((GlusterGeoRepSessionDetails) obj).getDeletesPending()) &&
                ObjectUtils.objectsEqual(getUpdatedAt(), ((GlusterGeoRepSessionDetails) obj).getUpdatedAt());
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
        result = prime * result + ((filesSynced == null) ? 0 : filesSynced.hashCode());
        result = prime * result + ((filesPending == null) ? 0 : filesPending.hashCode());
        result = prime * result + ((filesSkipped == null) ? 0 : filesSkipped.hashCode());
        result = prime * result + ((bytesPending == null) ? 0 : bytesPending.hashCode());
        result = prime * result + ((deletesPending == null) ? 0 : deletesPending.hashCode());
        result = prime * result + ((updatedAt == null) ? 0 : updatedAt.hashCode());
        return result;
    }
}
