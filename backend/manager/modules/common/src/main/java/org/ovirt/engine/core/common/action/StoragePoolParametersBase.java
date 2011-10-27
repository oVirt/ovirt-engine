package org.ovirt.engine.core.common.action;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StoragePoolParametersBase")
public class StoragePoolParametersBase extends VdcActionParametersBase implements java.io.Serializable {
    private static final long serialVersionUID = -5018623647865122548L;
    @XmlElement(name = "VdsId")
    private Guid privateVdsId;

    public Guid getVdsId() {
        return privateVdsId;
    }

    public void setVdsId(Guid value) {
        privateVdsId = value;
    }

    public StoragePoolParametersBase(Guid storagePoolId) {
        setStoragePoolId(storagePoolId);
    }

    @XmlElement(name = "StoragePoolId")
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    public void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    private boolean privateSuppressCheck;

    public boolean getSuppressCheck() {
        return privateSuppressCheck;
    }

    public void setSuppressCheck(boolean value) {
        privateSuppressCheck = value;
    }

    @XmlElement(name="ForceDelete")
    private boolean forceDelete;

    public boolean getForceDelete() {
        return forceDelete;
    }

    public void setForceDelete(boolean forceDelete) {
        this.forceDelete = forceDelete;
    }

    public StoragePoolParametersBase() {
    }
}
