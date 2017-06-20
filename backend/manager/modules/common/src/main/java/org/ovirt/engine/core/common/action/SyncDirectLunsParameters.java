package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class SyncDirectLunsParameters extends SyncLunsParameters {

    private static final long serialVersionUID = -8932897991614156001L;

    private Guid directLunId;

    public SyncDirectLunsParameters() {
        this(null);
    }

    public SyncDirectLunsParameters(Guid storagePoolId) {
        super(storagePoolId);
    }

    public SyncDirectLunsParameters(Guid vdsId, Guid directLunId) {
        this(Guid.Empty);
        setVdsId(vdsId);
        setDirectLunId(directLunId);
    }

    public Guid getDirectLunId() {
        return directLunId;
    }

    public void setDirectLunId(Guid directLunId) {
        this.directLunId = directLunId;
    }
}
