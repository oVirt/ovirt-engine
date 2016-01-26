package org.ovirt.engine.core.common.action;

import org.ovirt.engine.core.compat.Guid;

public class RemoveStorageDomainParameters extends StorageDomainParametersBase {
    private static final long serialVersionUID = -4687251701380479912L;

    private transient boolean destroyingPool;

    public RemoveStorageDomainParameters(Guid storageDomainId) {
        super(storageDomainId);
    }

    private boolean privateDoFormat;

    public boolean getDoFormat() {
        return privateDoFormat;
    }

    public void setDoFormat(boolean value) {
        privateDoFormat = value;
    }

    public RemoveStorageDomainParameters() {
    }

    public void setDestroyingPool(boolean destroyingPool) {
        this.destroyingPool = destroyingPool;
    }

    public boolean getDestroyingPool() {
        return destroyingPool;
    }
}
