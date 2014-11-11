package org.ovirt.engine.core.bll.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.CreateOvfStoresForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.ProcessOvfUpdateForStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.StorageDomainPoolParametersBase;
import org.ovirt.engine.core.common.action.StoragePoolParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.StorageDomainType;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMap;
import org.ovirt.engine.core.common.businessentities.StoragePoolIsoMapId;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class DeactivateStorageDomainWithOvfUpdateCommand<T extends StorageDomainPoolParametersBase> extends
        DeactivateStorageDomainCommand<T> {

    public DeactivateStorageDomainWithOvfUpdateCommand(T parameters) {
        this(parameters, null);
    }

    public DeactivateStorageDomainWithOvfUpdateCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setCommandShouldBeLogged(false);
    }


    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */

    protected DeactivateStorageDomainWithOvfUpdateCommand(Guid commandId) {
        super(commandId);

    }
    @Override
    protected void executeCommand() {
        StoragePoolIsoMap map =
                getStoragePoolIsoMapDAO().get
                        (new StoragePoolIsoMapId(getParameters().getStorageDomainId(),
                                getParameters().getStoragePoolId()));
        changeDomainStatusWithCompensation(map, StorageDomainStatus.Unknown, StorageDomainStatus.Locked);

        if (shouldPerformOvfUpdate()) {
            runInternalAction(VdcActionType.ProcessOvfUpdateForStoragePool, new StoragePoolParametersBase(getStoragePoolId()), null);

            VdcReturnValueBase tmpRetValue = runInternalActionWithTasksContext(VdcActionType.ProcessOvfUpdateForStorageDomain,
                    createProcessOvfUpdateForDomainParams(), null);

            getReturnValue().getVdsmTaskIdList().addAll(tmpRetValue.getInternalVdsmTaskIdList());
        }

        if (getReturnValue().getVdsmTaskIdList().isEmpty()) {
            executeDeactivateCommnad(true);
        } else {
            setCommandShouldBeLogged(false);
        }

        setSucceeded(true);
    }

    protected boolean shouldPerformOvfUpdate() {
        return !getParameters().isInactive() && ovfOnAnyDomainSupported() && getStorageDomain().getStatus() == StorageDomainStatus.Active
                && getStorageDomain().getStorageDomainType().isDataDomain();
    }

    private boolean ovfOnAnyDomainSupported() {
        return FeatureSupported.ovfStoreOnAnyDomain(getStoragePool().getcompatibility_version());
    }

    private ProcessOvfUpdateForStorageDomainCommandParameters createProcessOvfUpdateForDomainParams() {
        ProcessOvfUpdateForStorageDomainCommandParameters params = new ProcessOvfUpdateForStorageDomainCommandParameters(getStoragePoolId(), getStorageDomainId());
        params.setParentCommand(getActionType());
        params.setParentParameters(getParameters());
        params.setSkipDomainChecks(true);
        return params;
    }

    @Override
    protected void endSuccessfully() {
        endCommand();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        endCommand();
        setSucceeded(true);
    }

    public boolean hasExecutionEnded() {
        return CommandCoordinatorUtil.getCommandExecutionStatus(getParameters().getCommandId()) == CommandExecutionStatus.EXECUTED;
    }

    private void endCommand() {
        if (!hasExecutionEnded()) {
            return;
        }

        List<Guid> createdTasks = new LinkedList<>();

        for (VdcActionParametersBase parametersBase : getParameters().getImagesParameters()) {
            if (parametersBase.getCommandType() == VdcActionType.AddImageFromScratch) {
                CreateOvfStoresForStorageDomainCommandParameters parameters = new CreateOvfStoresForStorageDomainCommandParameters(getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(), 0);
                parameters.getImagesParameters().addAll(getParameters().getImagesParameters());
                parameters.setParentParameters(getParameters());
                parameters.setParentCommand(getActionType());
                parameters.setSkipDomainChecks(true);
                VdcReturnValueBase vdsReturnValue = getBackend().endAction(VdcActionType.CreateOvfStoresForStorageDomain, parameters, null);
                createdTasks.addAll(vdsReturnValue.getInternalVdsmTaskIdList());
                break;
            }
            createdTasks.addAll(getBackend().endAction(parametersBase.getCommandType(), parametersBase, null).getInternalVdsmTaskIdList());
        }

        if (!createdTasks.isEmpty()) {
            setSucceeded(true);
            startPollingAsyncTasks(createdTasks);
            return;
        }

        deactivateStorageDomainAfterTaskExecution();
    }

    private boolean executeDeactivateCommnad(boolean passContext) {
        final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(getStorageDomainId(), getStoragePoolId());
        params.setSkipChecks(true);
        params.setSkipLock(true);
        CommandContext context = passContext ? cloneContext() : null;
        return getBackend().runInternalAction(VdcActionType.DeactivateStorageDomain, params, context).getSucceeded();
    }

    protected void deactivateStorageDomainAfterTaskExecution() {
        final StorageDomainPoolParametersBase params = new StorageDomainPoolParametersBase(getStorageDomainId(), getStoragePoolId());
        params.setSkipChecks(true);
        boolean newThread = getStorageDomain().getStorageDomainType() == StorageDomainType.Master && getNewMaster(false) == null;
        if (newThread) {
            ThreadPoolUtil.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        waitForTasksToBeCleared();
                        executeDeactivateCommnad(false);
                    } catch (Exception e) {
                        setSucceeded(false);
                        log.errorFormat("Error when attempting to deactivate storage domain {0}", getStorageDomainId(), e);
                        compensate();
                    }
                }
            });
        } else {
            executeDeactivateCommnad(false);
        }
    }

    protected void waitForTasksToBeCleared() throws InterruptedException {
        log.infoFormat("waiting for all tasks related to domain {0} to be cleared (if exist) before attempting to deactivate", getStorageDomainId());
        while (true) {
            TimeUnit.SECONDS.sleep(3);
            List<Guid> tasks = getDbFacade().getAsyncTaskDao().getAsyncTaskIdsByEntity(getStorageDomainId());
            if (tasks.isEmpty()) {
                log.infoFormat("no tasks for the deactivated domain {0}, proceeding with deactivation", getStorageDomainId());
                return;
            } else {
                log.infoFormat("tasks {0} were found for domain {1}, waiting before attempting to deactivate", tasks, getStorageDomainId());
            }
        }
    }
}
