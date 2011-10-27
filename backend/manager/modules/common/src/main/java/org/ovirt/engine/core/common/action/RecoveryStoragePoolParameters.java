package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "RecoveryStoragePoolParameters")
public class RecoveryStoragePoolParameters extends ReconstructMasterParameters {
    private static final long serialVersionUID = -1967845549935626938L;
    @XmlElement(name = "NewMasterDomainId")
    private Guid privateNewMasterDomainId = new Guid();

    public Guid getNewMasterDomainId() {
        return privateNewMasterDomainId;
    }

    public void setNewMasterDomainId(Guid value) {
        privateNewMasterDomainId = value;
    }

    public RecoveryStoragePoolParameters(Guid storagePoolId, Guid newMasterDomainId) {
        super(storagePoolId, Guid.Empty, false);
        setNewMasterDomainId(newMasterDomainId);
    }

    public RecoveryStoragePoolParameters() {
    }
}
