package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

/**
 * Base parameters class adding a storage domain id field.
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "StorageDomainIdParametersBase")
public class StorageDomainIdParametersBase extends IrsBaseVDSCommandParameters {

    @XmlElement(name = "StorageDomainId")
    private Guid privateStorageDomainId = new Guid();

    protected StorageDomainIdParametersBase(Guid storagePoolId) {
        super(storagePoolId);
    }

    protected StorageDomainIdParametersBase() {
        super();
    }

    public Guid getStorageDomainId() {
        return privateStorageDomainId;
    }

    public void setStorageDomainId(Guid value) {
        privateStorageDomainId = value;
    }

    @Override
    public String toString() {
        return String.format("%s, storageDomainId = %s", super.toString(), getStorageDomainId());
    }
}
