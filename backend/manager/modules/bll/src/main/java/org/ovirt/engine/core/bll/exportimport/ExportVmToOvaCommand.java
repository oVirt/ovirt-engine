package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.LockMessage;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.ExportVmToOvaParameters;
import org.ovirt.engine.core.common.action.ExportVmToOvaParameters.Phase;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Nameable;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.VmDeviceId;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.SnapshotDao;

@NonTransactiveCommandAttribute
public class ExportVmToOvaCommand<T extends ExportVmToOvaParameters> extends ExportOvaCommand<T> implements SerialChildExecutingCommand {

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;
    @Inject
    private SnapshotDao snapshotDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private DiskVmElementDao diskVmElementDao;
    private List<DiskImage> cachedDisks;
    private String cachedVmIsBeingExportedMessage;

    public ExportVmToOvaCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void init() {
        setVmId(getParameters().getEntityId());
        if (getVm() != null) {
            setStoragePoolId(getVm().getStoragePoolId());
        }
        super.init();
    }

    @Override
    protected Nameable getEntity() {
        return getVm();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionSubjects = new ArrayList<>();
        permissionSubjects.add(new PermissionSubject(
                getParameters().getEntityId(),
                VdcObjectType.VM,
                getActionType().getActionGroup()));
        return permissionSubjects;
    }

    @Override
    protected boolean validate() {
        if (getEntity() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }
        return super.validate();
    }

    @Override
    protected void executeCommand() {
        getParameters().setPhase(Phase.CREATE_SNAPSHOT);
        Guid snapshotId = createVmSnapshot();
        getParameters().setSnapshotId(snapshotId);
        setSucceeded(true);
    }

    private Guid createVmSnapshot() {
        Snapshot activeSnapshot = snapshotDao.get(getVmId(), Snapshot.SnapshotType.ACTIVE);
        ActionReturnValue returnValue = runInternalAction(
                ActionType.CreateSnapshotForVm,
                buildCreateSnapshotParameters(),
                ExecutionHandler.createDefaultContextForTasks(getContext()));

        if (!returnValue.getSucceeded()) {
            log.error("Failed to create VM snapshot");
            throw new EngineException(returnValue.getFault().getError(), returnValue.getFault().getMessage());
        }

        return activeSnapshot.getId();
    }

    private CreateSnapshotForVmParameters buildCreateSnapshotParameters() {
        CreateSnapshotForVmParameters parameters = new CreateSnapshotForVmParameters(
                getVm().getId(),
                StorageConstants.OVA_AUTO_GENERATED_SNAPSHOT_DESCRIPTION,
                false);
        parameters.setShouldBeLogged(false);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setSaveMemory(false);
        parameters.setNeedsLocking(false);
        return parameters;
    }

    private void removeVmSnapshot() {
        ActionReturnValue returnValue = runInternalAction(
                ActionType.RemoveSnapshot,
                createRemoveSnapshotParameters(),
                cloneContextAndDetachFromParent());

        if (!returnValue.getSucceeded()) {
            log.error("Failed to remove VM snapshot");
        }
    }

    private RemoveSnapshotParameters createRemoveSnapshotParameters() {
        RemoveSnapshotParameters parameters = new RemoveSnapshotParameters(getParameters().getSnapshotId(), getVmId());
        parameters.setEndProcedure(EndProcedure.COMMAND_MANAGED);
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setNeedsLocking(false);
        parameters.setShouldBeLogged(false);
        return parameters;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_STARTING_EXPORT_VM_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;

        case END_SUCCESS:
            return getSucceeded() ?
                    AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA
                    : AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;

        default:
            return AuditLogType.IMPORTEXPORT_EXPORT_VM_TO_OVA_FAILED;
        }
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return Collections.singletonMap(getParameters().getEntityId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.VM, getVmIsBeingExportedMessage()));
    }

    private String getVmIsBeingExportedMessage() {
        if (cachedVmIsBeingExportedMessage == null) {
            cachedVmIsBeingExportedMessage = new LockMessage(EngineMessage.ACTION_TYPE_FAILED_VM_IS_BEING_EXPORTED)
                    .withOptional("VmName", getVm() != null ? getVm().getName() : null)
                    .toString();
        }
        return cachedVmIsBeingExportedMessage;
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        switch(getParameters().getPhase()) {
        case CREATE_SNAPSHOT:
            getParameters().setPhase(Phase.CREATE_OVA);
            break;

        case CREATE_OVA:
            getParameters().setPhase(Phase.REMOVE_SNAPSHOT);
            break;

        case REMOVE_SNAPSHOT:
            return false;

        default:
        }

        persistCommandIfNeeded();
        executeNextOperation();
        return true;
    }

    @SuppressWarnings("incomplete-switch")
    private void executeNextOperation() {
        switch (getParameters().getPhase()) {
            case CREATE_OVA:
                createOva();
                break;

            case REMOVE_SNAPSHOT:
                removeVmSnapshot();
                break;
        }
    }

    protected List<DiskImage> getDisks() {
        if (cachedDisks == null) {
            cachedDisks = diskImageDao.getAllSnapshotsForVmSnapshot(getParameters().getSnapshotId());
            cachedDisks.forEach(disk -> disk.setDiskVmElements(Collections.singleton(
                    diskVmElementDao.get(new VmDeviceId(disk.getId(), getParameters().getEntityId())))));
            for (DiskImage disk : cachedDisks) {
                disk.getImage().setVolumeFormat(VolumeFormat.COW);
            }
        }
        return cachedDisks;
    }

    @Override
    protected void endWithFailure() {
        if (getParameters().getSnapshotId() != null) {
            removeVmSnapshot();
        }
        super.endWithFailure();
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__EXPORT);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected CommandContext createOvaCreationStepContext() {
        CommandContext commandCtx = null;
        StepEnum step = StepEnum.CREATING_OVA;
        try {
            Step ovaCreationStep = executionHandler.addSubStep(getExecutionContext(),
                    getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                    step,
                    ExecutionMessageDirector.resolveStepMessage(step, Collections.emptyMap()));

            ExecutionContext ctx = new ExecutionContext();
            ctx.setStep(ovaCreationStep);
            ctx.setMonitored(true);

            commandCtx = cloneContext().withoutCompensationContext().withExecutionContext(ctx).withoutLock();

        } catch (RuntimeException e) {
            log.error("Failed to create command context of creating OVA '{}': {}", getVmName(), e.getMessage());
            log.debug("Exception", e);
        }

        return commandCtx;
    }
}
