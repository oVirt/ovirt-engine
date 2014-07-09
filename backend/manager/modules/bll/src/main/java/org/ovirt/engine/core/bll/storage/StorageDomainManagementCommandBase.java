package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.Guid;

public abstract class StorageDomainManagementCommandBase<T extends StorageDomainManagementParameter> extends
        StorageDomainCommandBase<T> {
    public StorageDomainManagementCommandBase(T parameters) {
        super(parameters);
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected StorageDomainManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    @Override
    public StorageDomain getStorageDomain() {
        if (super.getStorageDomain() == null) {
            super.setStorageDomain(new StorageDomain());
        }
        super.getStorageDomain().setStorageStaticData(getParameters().getStorageDomain());
        return super.getStorageDomain();
    }

    protected boolean isStorageWithSameNameExists() {
        return getStorageDomainStaticDAO().getByName(getStorageDomain().getStorageName()) != null;
    }
}
