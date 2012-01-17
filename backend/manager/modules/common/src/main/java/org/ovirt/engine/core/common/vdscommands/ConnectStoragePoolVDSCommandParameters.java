package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ConnectStoragePoolVDSCommandParameters")
public class ConnectStoragePoolVDSCommandParameters extends GetStorageConnectionsListVDSCommandParameters {
    public ConnectStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, int vds_spm_id, Guid masterDomainId,
            int masterVersion) {
        super(vdsId, storagePoolId);
        this.setvds_spm_id(vds_spm_id);
        setMasterDomainId(masterDomainId);
        setMasterVersion(masterVersion);
    }

    @XmlElement(name = "vds_spm_id")
    private int privatevds_spm_id;

    public int getvds_spm_id() {
        return privatevds_spm_id;
    }

    private void setvds_spm_id(int value) {
        privatevds_spm_id = value;
    }

    @XmlElement
    private Guid privateMasterDomainId = new Guid();

    public Guid getMasterDomainId() {
        return privateMasterDomainId;
    }

    private void setMasterDomainId(Guid value) {
        privateMasterDomainId = value;
    }

    @XmlElement
    private int privateMasterVersion;

    public int getMasterVersion() {
        return privateMasterVersion;
    }

    private void setMasterVersion(int value) {
        privateMasterVersion = value;
    }

    public ConnectStoragePoolVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, vds_spm_id = %s, masterDomainId = %s, masterVersion = %s", super.toString(), getvds_spm_id(), getMasterDomainId(), getMasterVersion());
    }
}
