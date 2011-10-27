package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ActivateStorageDomainVDSCommandParameters")
public class ActivateStorageDomainVDSCommandParameters extends StorageDomainIdParametersBase {
    public ActivateStorageDomainVDSCommandParameters(Guid storagePoolId, Guid storageDomainId) {
        super(storagePoolId);
        setStorageDomainId(storageDomainId);
    }

    public ActivateStorageDomainVDSCommandParameters() {
    }
}
