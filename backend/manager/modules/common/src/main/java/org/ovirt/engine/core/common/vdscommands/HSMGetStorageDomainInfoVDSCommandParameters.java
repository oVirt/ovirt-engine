package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "HSMGetStorageDomainInfoVDSCommandParameters")
public class HSMGetStorageDomainInfoVDSCommandParameters extends ValidateStorageDomainVDSCommandParameters {
    public HSMGetStorageDomainInfoVDSCommandParameters(Guid vdsId, Guid storageDomainId) {
        super(vdsId, storageDomainId);
    }

    public HSMGetStorageDomainInfoVDSCommandParameters() {
    }
}
