package org.ovirt.engine.core.common.vdscommands;

import java.util.ArrayList;

import org.ovirt.engine.core.compat.Guid;

public class DestroyImageVDSCommandParameters extends AllStorageAndImageIdVDSCommandParametersBase {
    public DestroyImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            ArrayList<Guid> imageList, boolean postZero, boolean force, String compatibilityVersion) {
        super(storagePoolId, storageDomainId, imageGroupId, Guid.Empty);
        setPostZero(postZero);
        setImageList(imageList);
        setForce(force);
        setCompatibilityVersion(compatibilityVersion);
    }

    private ArrayList<Guid> privateImageList;

    public ArrayList<Guid> getImageList() {
        return privateImageList;
    }

    private void setImageList(ArrayList<Guid> value) {
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
