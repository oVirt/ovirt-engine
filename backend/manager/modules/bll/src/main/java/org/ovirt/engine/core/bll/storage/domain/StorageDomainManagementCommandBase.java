package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

public abstract class StorageDomainManagementCommandBase<T extends StorageDomainManagementParameter> extends
        StorageDomainCommandBase<T> {

    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;

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
        return storageDomainStaticDao.getByName(getStorageDomain().getStorageName()) != null;
    }
}
