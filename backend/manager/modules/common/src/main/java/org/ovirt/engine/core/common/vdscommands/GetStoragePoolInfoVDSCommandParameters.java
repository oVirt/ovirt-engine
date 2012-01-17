package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetStoragePoolInfoVDSCommandParameters")
public class GetStoragePoolInfoVDSCommandParameters extends GetStorageDomainsListVDSCommandParameters {
    public GetStoragePoolInfoVDSCommandParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public GetStoragePoolInfoVDSCommandParameters() {
    }
}
