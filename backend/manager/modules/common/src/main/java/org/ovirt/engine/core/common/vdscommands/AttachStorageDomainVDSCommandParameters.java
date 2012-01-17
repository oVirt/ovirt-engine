package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "AttachStorageDomainVDSCommandParameters")
public class AttachStorageDomainVDSCommandParameters extends ActivateStorageDomainVDSCommandParameters {
    public AttachStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId, storageDomainId);
    }

    public AttachStorageDomainVDSCommandParameters() {
    }
}
