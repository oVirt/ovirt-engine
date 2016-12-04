package org.ovirt.engine.core.common.vdscommands;

import org.ovirt.engine.core.common.utils.ToStringBuilder;
import org.ovirt.engine.core.compat.Guid;

public class DeleteImageGroupVDSCommandParameters
        extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters implements PostDeleteAction {
    private boolean postZero;
    private boolean forceDelete;
    private boolean discard;

    public DeleteImageGroupVDSCommandParameters(Guid storagePoolId,
            Guid storageDomainId, Guid imageGroupId, boolean postZero, boolean discard, boolean force) {
        super(storagePoolId, storageDomainId, imageGroupId);
        setPostZero(postZero);
        setDiscard(discard);
        setForceDelete(force);
    }

    public DeleteImageGroupVDSCommandParameters() { }

    @Override
    public boolean getPostZero() {
        return postZero;
    }

    @Override
    public void setPostZero(boolean postZero) {
        this.postZero = postZero;
    }

    public boolean getForceDelete() {
        return forceDelete;
    }

    public void setForceDelete(boolean value) {
        forceDelete = value;
    }

    public boolean isDiscard() {
        return discard;
    }

    public void setDiscard(boolean discard) {
        this.discard = discard;
    }

    @Override
    protected ToStringBuilder appendAttributes(ToStringBuilder tsb) {
        return super.appendAttributes(tsb)
                .append("postZeros", getPostZero())
                .append("discard", isDiscard())
                .append("forceDelete", getForceDelete());
    }
}
