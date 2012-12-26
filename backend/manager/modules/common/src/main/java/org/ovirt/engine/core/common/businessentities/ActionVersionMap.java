package org.ovirt.engine.core.common.businessentities;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.TypeDef;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.mapping.GuidType;
import org.ovirt.engine.core.compat.Version;

@Entity
@Table(name = "action_version_map")
@TypeDef(name = "guid", typeClass = GuidType.class)
public class ActionVersionMap implements Serializable {
    private static final long serialVersionUID = -212222175662336097L;

    public ActionVersionMap() {
    }

    public ActionVersionMap(VdcActionType actionType, String clusterMinimalVersion, String storagePoolMinimalVersion) {
        setaction_type(actionType);
        setcluster_minimal_version(clusterMinimalVersion);
        setstorage_pool_minimal_version(storagePoolMinimalVersion);
    }

    public ActionVersionMap(VdcActionType actionType, Version clusterMinimalVersion, Version storagePoolMinimalVersion) {
        this(actionType, clusterMinimalVersion.toString(), storagePoolMinimalVersion.toString());
    }

    @Id
    @Column(name = "action_type")
    private int actionType = VdcActionType.Unknown.getValue();

    public VdcActionType getaction_type() {
        return VdcActionType.forValue(actionType);
    }

    public void setaction_type(VdcActionType value) {
        actionType = value.getValue();
    }

    @Column(name = "cluster_minimal_version", length = 40, nullable = false)
    private String clusterMinimalVersion;

    public String getcluster_minimal_version() {
        return clusterMinimalVersion;
    }

    public void setcluster_minimal_version(String value) {
        clusterMinimalVersion = value;
    }

    @Column(name = "storage_pool_minimal_version", length = 40, nullable = false)
    private String storagePoolMinimalVersion;

    public String getstorage_pool_minimal_version() {
        return storagePoolMinimalVersion;
    }

    public void setstorage_pool_minimal_version(String value) {
        storagePoolMinimalVersion = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + actionType * prime;
        result =
                prime * result
                        + ((clusterMinimalVersion == null) ? 0 : clusterMinimalVersion.hashCode());
        result =
                prime
                        * result
                        + ((storagePoolMinimalVersion == null) ? 0
                                : storagePoolMinimalVersion.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ActionVersionMap other = (ActionVersionMap) obj;
        if (actionType != other.actionType)
            return false;
        if (clusterMinimalVersion == null) {
            if (other.clusterMinimalVersion != null)
                return false;
        } else if (!clusterMinimalVersion.equals(other.clusterMinimalVersion))
            return false;
        if (storagePoolMinimalVersion == null) {
            if (other.storagePoolMinimalVersion != null)
                return false;
        } else if (!storagePoolMinimalVersion.equals(other.storagePoolMinimalVersion))
            return false;
        return true;
    }
}
