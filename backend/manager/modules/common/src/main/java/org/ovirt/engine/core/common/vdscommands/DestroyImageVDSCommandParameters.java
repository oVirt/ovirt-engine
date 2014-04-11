package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public class DestroyImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    public DestroyImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            List<Guid> imageList, boolean postZero, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId, Guid.Empty);
        setPostZero(postZero);
        setImageList(imageList);
        setForce(force);
    }

    private List<Guid> privateImageList;

    public List<Guid> getImageList() {
        return privateImageList;
    }

    private void setImageList(List<Guid> value) {
        privateImageList = value;
    }

    private boolean privatePostZero;

    public boolean getPostZero() {
        return privatePostZero;
    }

    protected void setPostZero(boolean value) {
        privatePostZero = value;
    }

    private boolean privateForce;

    public boolean getForce() {
        return privateForce;
    }

    protected void setForce(boolean value) {
        privateForce = value;
    }

    public DestroyImageVDSCommandParameters() {
    }
    @Override
    public String toString() {
        return String.format("%s, imageList = %s, postZero = %s, force = %s",
                super.toString(),
                getImageList(),
                getPostZero(),
                getForce());
    }
}
