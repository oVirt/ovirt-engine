package org.ovirt.engine.core.common.businessentities.gluster;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.BusinessEntityWithStatus;
import org.ovirt.engine.core.common.businessentities.IVdcQueryable;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Guid;

public class GlusterGeoRepSession extends IVdcQueryable implements BusinessEntityWithStatus<Guid, GeoRepSessionStatus>{

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

    @Override
    public Object getQueryableId() {
        return sessionId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((masterVolumeId == null) ? 0 : masterVolumeId.hashCode());
        result = prime * result + ((sessionId == null) ? 0 : sessionId.hashCode());
        result = prime * result + ((sessionKey == null) ? 0 : sessionKey.hashCode());
        result = prime * result + ((slaveHostName == null) ? 0 : slaveHostName.hashCode());
        result = prime * result + ((slaveNodeUuid == null) ? 0 : slaveNodeUuid.hashCode());
        result = prime * result + ((slaveVolumeId == null) ? 0 : slaveVolumeId.hashCode());
        result = prime * result + ((slaveVolumeName == null) ? 0 : slaveVolumeName.hashCode());
        result = prime * result + ((userName == null) ? 0 : userName.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof GlusterGeoRepSession)) {
            GlusterGeoRepSession session = (GlusterGeoRepSession) obj;
            if (ObjectUtils.objectsEqual(getId(), session.getId())
                    && (ObjectUtils.objectsEqual(getMasterVolumeId(), session.getMasterVolumeId()))
                    && (ObjectUtils.objectsEqual(getSessionKey(), session.getSessionKey()))
                    && (ObjectUtils.objectsEqual(getSlaveHostName(), session.getSlaveHostName()))
                    && (ObjectUtils.objectsEqual(getSlaveNodeUuid(), session.getSlaveNodeUuid()))
                    && (ObjectUtils.objectsEqual(getSlaveVolumeId(), session.getSlaveVolumeId()))
                    && (ObjectUtils.objectsEqual(getSlaveVolumeName(), session.getSlaveVolumeName()))
                    && (ObjectUtils.objectsEqual(getUserName(), session.getUserName()))
                    && (ObjectUtils.objectsEqual(getStatus(), session.getStatus()))) {
                return true;
            }
        }
        return false;
    }
}
