package org.ovirt.engine.core.bll.storage.domain;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateForStoragePoolParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class DeactivateStorageDomainWithOvfUpdateCommand<T extends StorageDomainPoolParametersBase> extends
        DeactivateStorageDomainCommand<T> {

    @Inject
    private AuditLogDirector auditLogDirector;

    public DeactivateStorageDomainWithOvfUpdateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setCommandShouldBeLogged(false);
    }

    private boolean shouldUseCallback() {
        CommandEntity commandEntity = CommandCoordinatorUtil.getCommandEntity(getCommandId());
        return (commandEntity != null && commandEntity.isCallbackEnabled()) || shouldPerformOvfUpdate();
    }

    @Override
    public CommandCallback getCallback() {
        if (shouldUseCallback()) {
            return new ConcurrentChildCommandsExecutionCallback();
        }

        return null;
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
            ProcessOvfUpdateForStoragePoolParameters parameters = new ProcessOvfUpdateForStoragePoolParameters(getStoragePoolId());
            parameters.setUpdateStorage(false);
            runInternalAction(VdcActionType.ProcessOvfUpdateForStoragePool, parameters, null);

            runInternalActionWithTasksContext(VdcActionType.ProcessOvfUpdateForStorageDomain,
                    createProcessOvfUpdateForDomainParams(), null);
        }

        if (noAsyncOperations()) {
            executeDeactivateCommand();
        }

        setSucceeded(true);
    }

    protected boolean shouldPerformOvfUpdate() {
        return !getParameters().isInactive() && getStorageDomain().getStatus() == StorageDomainStatus.Active
                && getStorageDomain().getStorageDomainType().isDataDomain();
    }

    private ProcessOvfUpdateForStorageDomainCommandParameters createProcessOvfUpdateForDomainParams() {
        ProcessOvfUpdateForStorageDomainCommandParameters params = new ProcessOvfUpdateForStorageDomainCommandParameters(getStoragePoolId(), getStorageDomainId());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setSkipDomainChecks(true);
        params.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        return params;
    }

    private StoragePoolIsoMap loadStoragePoolIsoMap() {
        return  getStoragePoolIsoMapDao().get
                        (new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
    }

    private void executeDeactivateCommand() {
        final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(getStorageDomainId(), getStoragePoolId());
        params.setSkipChecks(true);
        params.setSkipLock(true);
        params.setShouldBeLogged(true);
        getBackend().runInternalAction(VdcActionType.DeactivateStorageDomain, params,
                cloneContext().withoutLock());
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
        executeDeactivateCommand();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        if (CommandCoordinatorUtil.getCommandExecutionStatus(getCommandId()) != CommandExecutionStatus.EXECUTED) {
            changeStorageDomainStatusInTransaction(loadStoragePoolIsoMap(), StorageDomainStatus.Unknown);
            auditLogDirector.log(this, AuditLogType.USER_DEACTIVATE_STORAGE_DOMAIN_OVF_UPDATE_INCOMPLETE);
        } else {
            executeDeactivateCommand();
        }

        setSucceeded(true);
    }
}
