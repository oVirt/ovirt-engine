package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDomainParameters;
import org.ovirt.engine.core.common.action.StorageDomainManagementParameter;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CinderStorageDao;

public class AddManagedBlockStorageDomainCommand<T extends AddManagedBlockStorageDomainParameters> extends AddStorageDomainCommand {

    @Inject
    private CinderStorageDao cinderStorageDao;

    public AddManagedBlockStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        super.init();
        AddManagedBlockStorageDomainParameters parameters = (AddManagedBlockStorageDomainParameters) getParameters();
        parameters.getStorageDomain().setStorage(Guid.Empty.toString());
        parameters.getStorageDomain().setStorageFormat(StorageFormatType.V1);
        parameters.getStorageDomain().setDiscardAfterDelete(false);
        parameters.getStorageDomain().setBackup(false);
        parameters.getStorageDomain().setWipeAfterDelete(false);
    }

    @Override
    protected boolean validate() {
        return canAddDomain();
    }

    public AddManagedBlockStorageDomainCommand(StorageDomainManagementParameter parameters,
            CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        initializeStorageDomain();
        addStorageDomainInDb();
        AddManagedBlockStorageDomainParameters parameters = (AddManagedBlockStorageDomainParameters) getParameters();
        ManagedBlockStorage managedBlockStorage = new ManagedBlockStorage();
        managedBlockStorage.setId(getStorageDomainId());
        managedBlockStorage.setDriverOptions(parameters.getDriverOptions());
        cinderStorageDao.save(managedBlockStorage);
        setSucceeded(true);
    }

    @Override
    protected boolean canAddDomain() {
        return true;
    }

    @Override
    public void setPropertiesForAuditLog(AuditLog auditLog) {

    }

    @Override
    public void updateCallStackFromThrowable(Throwable throwable) {

    }
}
