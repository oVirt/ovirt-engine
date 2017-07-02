package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.ArrayList;
import java.util.Objects;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.Queryable;
import org.ovirt.engine.core.compat.Guid;

public class GlusterGeoRepSession implements Queryable, BusinessEntityWithStatus<Guid, GeoRepSessionStatus>{

    private static final long serialVersionUID = 1L;

    private Guid masterVolumeId;
    private String masterVolumeName;
    private String sessionKey;
    private String slaveHostName;
    private Guid slaveNodeUuid;
    private String slaveVolumeName;
    private Guid sessionId;
    private Guid slaveVolumeId;
    private GeoRepSessionStatus status;
    private String userName;
    private ArrayList<GlusterGeoRepSessionDetails> sessionDetails;

    public Guid getMasterVolumeId() {
        return masterVolumeId;
    }

    public void setMasterVolumeId(Guid masterVolumeId) {
        this.masterVolumeId = masterVolumeId;
    }

    public String getMasterVolumeName() {
        return masterVolumeName;
    }

    public void setMasterVolumeName(String masterVolumeName) {
        this.masterVolumeName = masterVolumeName;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public String getSlaveHostName() {
        return slaveHostName;
    }

    public void setSlaveHostName(String slaveHostName) {
        this.slaveHostName = slaveHostName;
    }

    public Guid getSlaveNodeUuid() {
        return slaveNodeUuid;
    }

    public void setSlaveNodeUuid(Guid slaveNodeUuid) {
        this.slaveNodeUuid = slaveNodeUuid;
    }

    public String getSlaveVolumeName() {
        return slaveVolumeName;
    }

    public void setSlaveVolumeName(String slaveVolumeName) {
        this.slaveVolumeName = slaveVolumeName;
    }

    public Guid getSlaveVolumeId() {
        return slaveVolumeId;
    }

    public void setSlaveVolumeId(Guid slaveVolumeId) {
        this.slaveVolumeId = slaveVolumeId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public GeoRepSessionStatus getStatus() {
        return status;
    }

    @Override
    public void setStatus(GeoRepSessionStatus status) {
        this.status = status;
    }

    @Override
    public Guid getId() {
        return sessionId;
    }

    @Override
    public void setId(Guid id) {
        this.sessionId = id;
    }

    public ArrayList<GlusterGeoRepSessionDetails> getSessionDetails() {
        return sessionDetails;
    }

    public void setSessionDetails(ArrayList<GlusterGeoRepSessionDetails> sessionDetails) {
        this.sessionDetails = sessionDetails;
    }

    public boolean isCheckPointCompleted(){
        for (GlusterGeoRepSessionDetails details : getSessionDetails()) {
            if (details.getStatus() == GeoRepSessionStatus.ACTIVE && !details.isCheckpointCompleted()) {
                return false;
            }
        }
        return this.status != GeoRepSessionStatus.FAULTY;
    }

    @Override
    public Object getQueryableId() {
        return sessionId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                sessionId,
                masterVolumeId,
                sessionKey,
                slaveHostName,
                slaveNodeUuid,
                slaveVolumeId,
                slaveVolumeName,
                userName,
                status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof GlusterGeoRepSession)) {
            return false;
        }
        GlusterGeoRepSession other = (GlusterGeoRepSession) obj;
        return Objects.equals(sessionId, other.sessionId)
                && Objects.equals(masterVolumeId, other.masterVolumeId)
                && Objects.equals(sessionKey, other.sessionKey)
                && Objects.equals(slaveHostName, other.slaveHostName)
                && Objects.equals(slaveNodeUuid, other.slaveNodeUuid)
                && Objects.equals(slaveVolumeId, other.slaveVolumeId)
                && Objects.equals(slaveVolumeName, other.slaveVolumeName)
                && Objects.equals(userName, other.userName)
                && Objects.equals(status, other.status);
    }
}
