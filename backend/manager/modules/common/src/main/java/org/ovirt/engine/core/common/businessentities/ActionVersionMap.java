package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;
import java.util.Objects;

import org.ovirt.engine.core.common.action.VdcActionType;

public class ActionVersionMap implements Serializable {
    private static final long serialVersionUID = -212222175662336097L;

    public ActionVersionMap() {
        this(false);
    }

    public ActionVersionMap(boolean nullValue) {
        this.nullValue = nullValue;
        this.actionType = VdcActionType.Unknown.getValue();
    }

    private int actionType;

    public VdcActionType getActionType() {
        return VdcActionType.forValue(actionType);
    }

    public void setActionType(VdcActionType value) {
        actionType = value.getValue();
    }

    private String clusterMinimalVersion;

    public String getClusterMinimalVersion() {
        return clusterMinimalVersion;
    }

    public void setClusterMinimalVersion(String value) {
        clusterMinimalVersion = value;
    }

    private String storagePoolMinimalVersion;

    public String getStoragePoolMinimalVersion() {
        return storagePoolMinimalVersion;
    }

    public void setStoragePoolMinimalVersion(String value) {
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
        return Objects.hash(
                actionType,
                clusterMinimalVersion,
                storagePoolMinimalVersion
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ActionVersionMap)) {
            return false;
        }
        ActionVersionMap other = (ActionVersionMap) obj;
        return actionType == other.actionType
                && Objects.equals(clusterMinimalVersion, other.clusterMinimalVersion)
                && Objects.equals(storagePoolMinimalVersion, other.storagePoolMinimalVersion);
    }
}
