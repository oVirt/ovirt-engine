package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "GetStorageConnectionsListVDSCommandParameters")
public class GetStorageConnectionsListVDSCommandParameters extends VdsIdVDSCommandParametersBase {
    private Guid privateStoragePoolId = new Guid();

    public Guid getStoragePoolId() {
        return privateStoragePoolId;
    }

    private void setStoragePoolId(Guid value) {
        privateStoragePoolId = value;
    }

    public GetStorageConnectionsListVDSCommandParameters(Guid vdsId, Guid storagePoolId) {
        super(vdsId);
        setStoragePoolId(storagePoolId);
    }

    public GetStorageConnectionsListVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, storagePoolId = %s", super.toString(), getStoragePoolId());
    }
}
