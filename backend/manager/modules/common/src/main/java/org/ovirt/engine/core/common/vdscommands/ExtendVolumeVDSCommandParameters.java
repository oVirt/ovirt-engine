package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.compat.Guid;

public class ExtendVolumeVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
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
