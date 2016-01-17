package org.ovirt.engine.core.bll.storage.domain;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.Guid;

public abstract class StorageDomainManagementCommandBase<T extends StorageDomainManagementParameter> extends
        StorageDomainCommandBase<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     */
    protected StorageDomainManagementCommandBase(Guid commandId) {
        super(commandId);
    }

    public StorageDomainManagementCommandBase(T parameters,
            CommandContext commandContext) {
        super(parameters, commandContext);
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
        return getStorageDomainStaticDao().getByName(getStorageDomain().getStorageName()) != null;
    }
}
