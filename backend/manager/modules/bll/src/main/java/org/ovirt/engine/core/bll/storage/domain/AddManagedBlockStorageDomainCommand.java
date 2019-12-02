package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.storage.ManagedBlockStorageDomainValidator;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddManagedBlockStorageDomainParameters;
import org.ovirt.engine.core.common.businessentities.AuditLog;
import org.ovirt.engine.core.common.businessentities.StorageFormatType;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CinderStorageDao;

public class AddManagedBlockStorageDomainCommand<T extends AddManagedBlockStorageDomainParameters> extends AddStorageDomainCommand<T> {

    @Inject
    private CinderStorageDao cinderStorageDao;

    public AddManagedBlockStorageDomainCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    public void init() {
        super.init();
        AddManagedBlockStorageDomainParameters parameters = getParameters();
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

    public AddManagedBlockStorageDomainCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    @Override
    protected void executeCommand() {
        initializeStorageDomain();
        addStorageDomainInDb();
        AddManagedBlockStorageDomainParameters parameters = getParameters();
        ManagedBlockStorage managedBlockStorage = new ManagedBlockStorage();
        managedBlockStorage.setId(getStorageDomainId());
        managedBlockStorage.setDriverOptions(parameters.getDriverOptions());
        managedBlockStorage.setDriverSensitiveOptions(parameters.getDriverSensitiveOptions());
        cinderStorageDao.save(managedBlockStorage);
        setSucceeded(true);
    }

    @Override
    protected boolean canAddDomain() {
        if (!validate(ManagedBlockStorageDomainValidator.isOperationSupportedByManagedBlockStorage(getActionType()))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CINDERLIB_DATA_BASE_REQUIRED);
        }
        return testStorageConnection();
    }

    private boolean testStorageConnection() {
        ActionReturnValue actionReturnValue = runInternalAction(ActionType.GetManagedBlockStorageStats, getParameters());
        return (actionReturnValue.getSucceeded() && actionReturnValue.getActionReturnValue() != null) ||
                failValidation(EngineMessage.FAILED_TO_CONNECT_MANAGED_BLOCK_DOMAIN);
    }

    @Override
    public void setPropertiesForAuditLog(AuditLog auditLog) {

    }

    @Override
    public void updateCallStackFromThrowable(Throwable throwable) {

    }
}
