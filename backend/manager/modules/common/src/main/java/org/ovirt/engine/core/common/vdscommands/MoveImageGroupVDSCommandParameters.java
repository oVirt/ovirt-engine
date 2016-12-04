package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class MoveImageGroupVDSCommandParameters
        extends TargetDomainImageGroupVDSCommandParameters implements PostDeleteAction {
    private Guid privateVmId;

    public Guid getVmId() {
        return privateVmId;
    }

    private void setVmId(Guid value) {
        privateVmId = value;
    }

    private ImageOperation privateOp;

    public ImageOperation getOp() {
        return privateOp;
    }

    private void setOp(ImageOperation value) {
        privateOp = value;
    }

    public MoveImageGroupVDSCommandParameters(Guid storagePoolId, Guid storageDomainId, Guid imageGroupId,
            Guid dstStorageDomainId, Guid vmId, ImageOperation op, boolean postZero, boolean discard, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId, dstStorageDomainId);
        setVmId(vmId);
        setOp(op);
        setPostZero(postZero);
        setDiscard(discard);
        setForce(force);
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

    public void setForce(boolean value) {
        privateForce = value;
    }

    public MoveImageGroupVDSCommandParameters() {
        privateVmId = Guid.Empty;
        privateOp = ImageOperation.Unassigned;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("vmId", getVmId())
                .append("op", getOp())
                .append("postZero", getPostZero())
                .append("discard", isDiscard())
                .append("force", getForce());
    }
}
