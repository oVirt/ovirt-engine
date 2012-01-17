package org.ovirt.engine.core.common.vdscommands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.ovirt.engine.core.compat.Guid;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RefreshStoragePoolVDSCommandParameters")
public class RefreshStoragePoolVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public RefreshStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, Guid masterStorageDomainId,
            int masterVersion) {
        super(vdsId, storagePoolId);
        setMasterStorageDomainId(masterStorageDomainId);
        setMasterVersion(masterVersion);
    }

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

    public RefreshStoragePoolVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, masterStorageDomainId=%s, msterVersion=%s",
                super.toString(),
                getMasterStorageDomainId(),
                getMasterVersion());
    }
}
