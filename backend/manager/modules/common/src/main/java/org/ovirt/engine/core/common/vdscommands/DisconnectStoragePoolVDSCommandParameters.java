package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "DisconnectStoragePoolVDSCommandParameters")
public class DisconnectStoragePoolVDSCommandParameters extends ConnectStoragePoolVDSCommandParameters {
    public DisconnectStoragePoolVDSCommandParameters(Guid vdsId, Guid storagePoolId, int vds_spm_id) {
        super(vdsId, storagePoolId, vds_spm_id, Guid.Empty, 0);
    }

    public DisconnectStoragePoolVDSCommandParameters() {
    }
}
