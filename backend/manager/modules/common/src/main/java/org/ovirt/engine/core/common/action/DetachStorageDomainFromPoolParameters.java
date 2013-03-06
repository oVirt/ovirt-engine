package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class DetachStorageDomainFromPoolParameters extends StorageDomainPoolParametersBase {
    private static final long serialVersionUID = 375203524805933936L;
    private boolean privateRemoveLast;

    public boolean getRemoveLast() {
        return privateRemoveLast;
    }

    public void setRemoveLast(boolean value) {
        privateRemoveLast = value;
    }

    private boolean privateDestroyingPool;

    public boolean getDestroyingPool() {
        return privateDestroyingPool;
    }

    public void setDestroyingPool(boolean value) {
        privateDestroyingPool = value;
    }

    public DetachStorageDomainFromPoolParameters(Guid storageId, Guid storagePoolId) {
        super(storageId, storagePoolId);
    }

    public DetachStorageDomainFromPoolParameters() {
    }
}
