package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetImageInfoVDSCommandParameters")
public class GetImageInfoVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    public GetImageInfoVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
    }

    public GetImageInfoVDSCommandParameters() {
    }
}
