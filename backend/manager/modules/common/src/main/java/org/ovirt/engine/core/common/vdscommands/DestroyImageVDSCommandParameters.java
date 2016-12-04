package org.ovirt.engine.core.common.vdscommands;

import java.util.List;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DestroyImageVDSCommandParameters
        extends AllStorageAndImageIdVDSCommandParametersBase implements PostDeleteAction {
    public DestroyImageVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            List<Guid> imageList, boolean postZero, boolean discard, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId, Guid.Empty);
        setPostZero(postZero);
        setDiscard(discard);
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

    @Override
    public boolean getPostZero() {
        return privatePostZero;
    }

    @Override
    public void setPostZero(boolean postZero) {
        privatePostZero = postZero;
    }

    private boolean discard;

    @Override
    public boolean isDiscard() {
        return discard;
    }

    @Override
    public void setDiscard(boolean discard) {
        this.discard = discard;
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
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("imageList", getImageList())
                .append("postZero", getPostZero())
                .append("force", getForce());
    }
}
