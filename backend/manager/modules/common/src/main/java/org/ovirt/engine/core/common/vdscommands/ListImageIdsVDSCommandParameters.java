package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ListImageIdsVDSCommandParameters")
public class ListImageIdsVDSCommandParameters extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters {
    public ListImageIdsVDSCommandParameters() {
        this(Guid.Empty, Guid.Empty, Guid.Empty);
    }

    public ListImageIdsVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId) {
        super(storagePoolId, storageDomainId, imageGroupId);
    }

}
