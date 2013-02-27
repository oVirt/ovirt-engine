package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.core.compat.Version;

public class ActionVersionMap implements Serializable {
    private static final long serialVersionUID = -212222175662336097L;

    public ActionVersionMap() {
    }

    public ActionVersionMap(boolean nullValue) {
        this.nullValue = nullValue;
    }

    public ActionVersionMap(VdcActionType actionType, String clusterMinimalVersion, String storagePoolMinimalVersion) {
        setaction_type(actionType);
        setcluster_minimal_version(clusterMinimalVersion);
        setstorage_pool_minimal_version(storagePoolMinimalVersion);
    }

    public ActionVersionMap(VdcActionType actionType, Version clusterMinimalVersion, Version storagePoolMinimalVersion) {
        this(actionType, clusterMinimalVersion.toString(), storagePoolMinimalVersion.toString());
    }

    private int actionType = VdcActionType.Unknown.getValue();

    public VdcActionType getaction_type() {
        return VdcActionType.forValue(actionType);
    }

    public void setaction_type(VdcActionType value) {
        actionType = value.getValue();
    }

    private String clusterMinimalVersion;

    public String getcluster_minimal_version() {
        return clusterMinimalVersion;
    }

    public void setcluster_minimal_version(String value) {
        clusterMinimalVersion = value;
    }

    private String storagePoolMinimalVersion;

    public String getstorage_pool_minimal_version() {
        return storagePoolMinimalVersion;
    }

    public void setstorage_pool_minimal_version(String value) {
        storagePoolMinimalVersion = value;
    }

    private transient boolean nullValue;

    public boolean isNullValue() {
        return nullValue;
    }

    public void setNullValue(boolean nullValue) {
        this.nullValue = nullValue;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + actionType;
        result = prime * result + ((clusterMinimalVersion == null) ? 0 : clusterMinimalVersion.hashCode());
        result = prime * result + ((storagePoolMinimalVersion == null) ? 0 : storagePoolMinimalVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActionVersionMap other = (ActionVersionMap) obj;
        return (actionType == other.actionType
                && ObjectUtils.objectsEqual(clusterMinimalVersion, other.clusterMinimalVersion)
                && ObjectUtils.objectsEqual(storagePoolMinimalVersion, other.storagePoolMinimalVersion));
    }
}
