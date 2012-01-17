package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DeactivateStorageDomainVDSCommandParameters")
public class DeactivateStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    @XmlElement(name = "MasterStorageDomainId")
    private Guid privateMasterStorageDomainId = new Guid();

    public Guid getMasterStorageDomainId() {
        return privateMasterStorageDomainId;
    }

    private void setMasterStorageDomainId(Guid value) {
        privateMasterStorageDomainId = value;
    }

    @XmlElement(name = "MasterVersion")
    private int privateMasterVersion;

    public int getMasterVersion() {
        return privateMasterVersion;
    }

    private void setMasterVersion(int value) {
        privateMasterVersion = value;
    }

    public DeactivateStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            Guid masterStorageDomainId, int masterVersion) {
        super(storagePoolId, storageDomainId);
        setMasterStorageDomainId(masterStorageDomainId);
        setMasterVersion(masterVersion);
    }

    public DeactivateStorageDomainVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, masterDomainId = %s, masterVersion = %s", super.toString(),
                getMasterStorageDomainId(), getMasterVersion());
    }
}
