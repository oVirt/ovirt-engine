package org.ovirt.engine.core.bll.storage.domain;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute.CommandCompensationPhase;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.DeactivateStorageDomainWithOvfUpdateParameters;
import org.ovirt.engine.core.common.action.DeactivateStorageDomainWithOvfUpdateStep;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.StorageDomainParametersBase;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.StoragePoolIsoMapDao;

@NonTransactiveCommandAttribute(forceCompensation = true, compensationPhase = CommandCompensationPhase.END_COMMAND)
public class DeactivateStorageDomainWithOvfUpdateCommand<T extends DeactivateStorageDomainWithOvfUpdateParameters> extends
        DeactivateStorageDomainCommand<T> implements SerialChildExecutingCommand {

    @Inject
    private AuditLogDirector auditLogDirector;
    @Inject
    private StoragePoolIsoMapDao storagePoolIsoMapDao;
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;
    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public DeactivateStorageDomainWithOvfUpdateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    private boolean shouldUseCallback() {
        CommandEntity commandEntity = commandCoordinatorUtil.getCommandEntity(getCommandId());
        return (commandEntity != null && commandEntity.isCallbackEnabled()) || shouldPerformOvfUpdate();
    }

    @Override
    public CommandCallback getCallback() {
        return shouldUseCallback() ? callbackProvider.get() : null;
    }

    /**
     * Constructor for command creation when compensation is applied on startup
     */

    public DeactivateStorageDomainWithOvfUpdateCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        StoragePoolIsoMap map = loadStoragePoolIsoMap();
        changeDomainStatusWithCompensation(map, StorageDomainStatus.Unknown, StorageDomainStatus.Locked, getCompensationContext());

        if (shouldPerformOvfUpdate()) {
            getParameters().setNextCommandStep(DeactivateStorageDomainWithOvfUpdateStep.UPDATE_OVF_STORE);
        } else if (noAsyncOperations()) {
            getParameters().setNextCommandStep(DeactivateStorageDomainWithOvfUpdateStep.DEACTIVATE_STORAGE_DOMAIN);
        }
        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        log.info("Command '{}' id '{}' executing step '{}'", getActionType(), getCommandId(),
                getParameters().getNextCommandStep());
        switch (getParameters().getNextCommandStep()) {
        case UPDATE_OVF_STORE:
            getParameters().setCommandStep(DeactivateStorageDomainWithOvfUpdateStep.UPDATE_OVF_STORE);
            runInternalAction(ActionType.UpdateOvfStoreForStorageDomain,
                    createUpdateOvfStoreParams(),
                    cloneContext().withoutCompensationContext());
            getParameters().setNextCommandStep(
                    DeactivateStorageDomainWithOvfUpdateStep.DEACTIVATE_STORAGE_DOMAIN
            );
            break;
        case DEACTIVATE_STORAGE_DOMAIN:
            getParameters().setCommandStep(DeactivateStorageDomainWithOvfUpdateStep.DEACTIVATE_STORAGE_DOMAIN);
            executeDeactivateCommand();
            getParameters().setNextCommandStep(DeactivateStorageDomainWithOvfUpdateStep.COMPLETE);
            break;
        case COMPLETE:
            getParameters().setCommandStep(DeactivateStorageDomainWithOvfUpdateStep.COMPLETE);
            setCommandStatus(CommandStatus.SUCCEEDED);
            return false;
        }
        persistCommandIfNeeded();
        return true;
    }

    @Override
    public void handleFailure() {
        log.error("Command '{}' id '{}' failed executing step '{}'", getActionType(), getCommandId(),
                getParameters().getCommandStep());
    }

    private StorageDomainParametersBase createUpdateOvfStoreParams() {
        StorageDomainParametersBase params = new StorageDomainParametersBase(getStorageDomainId());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setEndProcedure(EndProcedure.PARENT_MANAGED);
        return params;
    }

    protected boolean shouldPerformOvfUpdate() {
        return !getParameters().isInactive() &&
                StorageConstants.monitoredDomainStatuses.contains(getStorageDomain().getStatus())
                && getStorageDomain().getStorageDomainType().isDataDomain();
    }

    private StoragePoolIsoMap loadStoragePoolIsoMap() {
        return  storagePoolIsoMapDao.get
                        (new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
    }

    private void executeDeactivateCommand() {
        final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(getStorageDomainId(), getStoragePoolId());
        params.setSkipChecks(true);
        params.setSkipLock(true);
        params.setShouldBeLogged(true);
        runInternalAction(ActionType.DeactivateStorageDomain, params,
                ExecutionHandler.createInternalJobContext(getContext()));
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return AuditLogType.UNASSIGNED;
    }

    @Override
    protected void endSuccessfully() {
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        if (commandCoordinatorUtil.getCommandExecutionStatus(getCommandId()) != CommandExecutionStatus.EXECUTED) {
            changeStorageDomainStatusInTransaction(loadStoragePoolIsoMap(), StorageDomainStatus.Unknown);
            auditLogDirector.log(this, AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_OVF_UPDATE_INCOMPLETE);
        } else if (getParameters().isForceMaintenance()) {
            executeDeactivateCommand();
        } else {
            auditLogDirector.log(this, AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_FAILED);
        }

        setSucceeded(true);
    }
}
