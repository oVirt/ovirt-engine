package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class MergeSnapshotsVDSCommandParameters
        extends AllStorageAndImageIdVDSCommandParametersBase implements PostZero {
    private Guid _imageId2;

    public MergeSnapshotsVDSCommandParameters(Guid storagePoolId, Guid storageDomainId,
            Guid vmId, Guid imageGroupId, Guid imageId, Guid imageId2, boolean postZero) {
        super(storagePoolId, storageDomainId, imageGroupId, imageId);
        _imageId2 = imageId2;
        setVmId(vmId);
        setPostZero(postZero);
    }

    public Guid getImageId2() {
        return _imageId2;
    }

    private Guid privateVmId;

    public Guid getVmId() {
        return privateVmId;
    }

    public void setVmId(Guid value) {
        privateVmId = value;
    }

    private boolean privatePostZero;

    @Override
    public boolean getPostZero() {
        return privatePostZero;
    }

    @Override
    public void setPostZero(boolean postZero) {
        privatePostZero = postZero;
    }

    public MergeSnapshotsVDSCommandParameters() {
        _imageId2 = Guid.Empty;
        privateVmId = Guid.Empty;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageId2", getImageId2())
                .append("vmId", getVmId())
                .append("postZero", getPostZero());
    }
}
