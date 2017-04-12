package org.ovirt.engine.core.bll.storage.domain;

import static org.ovirt.engine.core.common.constants.StorageConstants.STEP_DEVICE_TYPE;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.connection.StorageHelperDirector;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MoveStorageDomainDeviceCommandParameters;
import org.ovirt.engine.core.common.action.ReduceStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.RemoveDeviceFromSANStorageDomainCommandParameters;
import org.ovirt.engine.core.common.action.RemoveDeviceFromSANStorageDomainCommandParameters.OperationStage;
import org.ovirt.engine.core.common.job.StepEnum;

@NonTransactiveCommandAttribute
@InternalCommandAttribute
public class RemoveDeviceFromSANStorageDomainCommand<T extends RemoveDeviceFromSANStorageDomainCommandParameters> extends CommandBase<T> implements SerialChildExecutingCommand {

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    @Inject
    private StorageHelperDirector storageHelperDirector;

    public RemoveDeviceFromSANStorageDomainCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected void executeCommand() {
        movePV();
        setSucceeded(true);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.emptyList();
    }

    private void movePV() {
        MoveStorageDomainDeviceCommandParameters p = new MoveStorageDomainDeviceCommandParameters(getParameters()
                .getStorageDomainId(), getParameters().getDeviceId(), getParameters().getDestinationDevices());
        p.setParentCommand(getActionType());
        p.setParentParameters(getParameters());
        p.setStoragePoolId(getStoragePoolId());
        p.setVdsRunningOn(getParameters().getVdsId());
        p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runInternalAction(ActionType.MoveStorageDomainDevice, p, null);
    }

    private void reduceDomain() {
        ReduceStorageDomainCommandParameters p = new ReduceStorageDomainCommandParameters(getParameters()
                .getStorageDomainId(), getParameters().getDeviceId());
        p.setParentCommand(getActionType());
        p.setParentParameters(getParameters());
        p.setStoragePoolId(getStoragePoolId());
        p.setVdsRunningOn(getParameters().getVdsId());
        p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        runInternalAction(ActionType.ReduceStorageDomain, p, null);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        if (getParameters().getOperationStage() == OperationStage.MOVE) {
            getParameters().setOperationStage(OperationStage.REDUCE);
            persistCommandIfNeeded();
            reduceDomain();
            return true;
        }

        return false;
    }

    protected void endSuccessfully() {
        storageHelperDirector.getItem(getStorageDomain().getStorageType())
                .removeLunFromStorageDomain(getParameters().getDeviceId());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        addCustomValue("LunId", getParameters().getDeviceId());
        switch (getActionState()) {
        case EXECUTE:
            return AuditLogType.USER_REMOVE_DEVICE_FROM_STORAGE_DOMAIN_STARTED;
        case END_SUCCESS:
            return AuditLogType.USER_REMOVE_DEVICE_FROM_STORAGE_DOMAIN;

        default:
            return AuditLogType.USER_REMOVE_DEVICE_FROM_STORAGE_DOMAIN_FAILED;
        }
    }

    @Override
    public StepEnum getCommandStep() {
        return StepEnum.REMOVE_DEVICE_FROM_DOMAIN;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
        }

        jobProperties.put(STEP_DEVICE_TYPE, getParameters().getDeviceId());
        return jobProperties;
    }
}
