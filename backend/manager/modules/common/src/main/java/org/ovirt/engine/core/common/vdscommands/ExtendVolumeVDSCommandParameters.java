package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.*;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "ExtendVolumeVDSCommandParameters")
public class ExtendVolumeVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    @XmlElement
    private int _newSize;

    public ExtendVolumeVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId, Guid imageId,
            int newSize) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        _newSize = newSize;
    }

    public int getNewSize() {
        return _newSize;
    }

    public ExtendVolumeVDSCommandParameters() {
    }

    @Override
    public String toString() {
        return String.format("%s, newSize = %s", super.toString(), getNewSize());
    }
}
