package org.ovirt.engine.core.bll.storage.backup;

import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_ACTIVE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_NOT_SHAREABLE;
import static org.ovirt.engine.core.bll.storage.disk.image.DisksFilter.ONLY_SNAPABLE;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.ovirt.engine.core.bll.LockMessagesMatchUtil;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.SerialChildExecutingCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.FeatureSupported;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateSnapshotForVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.action.VmBackupParameters;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmBackup;
import org.ovirt.engine.core.common.businessentities.VmBackupPhase;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.VmBackupType;
import org.ovirt.engine.core.common.constants.StorageConstants;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.job.Step;
import org.ovirt.engine.core.common.job.StepEnum;
import org.ovirt.engine.core.common.locks.LockingGroup;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.job.ExecutionMessageDirector;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.SnapshotDao;
import org.ovirt.engine.core.dao.VmBackupDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class HybridBackupCommand<T extends VmBackupParameters> extends StartVmBackupCommand<T> implements SerialChildExecutingCommand {

    @Inject
    private VmBackupDao vmBackupDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private SnapshotDao snapshotDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    @Typed(SerialChildCommandsExecutionCallback.class)
    private Instance<SerialChildCommandsExecutionCallback> callbackProvider;

    public HybridBackupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    public void init() {
        setVmId(getParameters().getVmBackup().getVmId());
    }

    @Override
    protected boolean validate() {
        final VM vm = getVm();

        if (vm == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!FeatureSupported.isHybridBackupSupported(vm)) {
            return false;
        }

        return super.validate();
    }

    @Override
    protected void executeCommand() {
        VmBackup vmBackup = getParameters().getVmBackup();
        Guid vmBackupId = createVmBackup();
        log.info("Created VmBackup entity '{}' for VM '{}'", vmBackupId, vmBackup.getVmId());

        Guid toCheckpointId = Guid.newGuid();
        ActionReturnValue returnValue = runInternalAction(
                ActionType.CreateSnapshotForVm,
                buildCreateSnapshotParameters(toCheckpointId),
                createStepsContext(StepEnum.CREATING_SNAPSHOTS));
        if (!returnValue.getSucceeded()) {
            auditLog(AuditLogType.USER_FAILED_CREATE_SNAPSHOT);
            getReturnValue().setFault(returnValue.getFault());
            setCommandStatus(CommandStatus.FAILED);
            updateVmBackupPhase(VmBackupPhase.FAILED);
            return;
        }

        auditLog(AuditLogType.USER_CREATE_SNAPSHOT);
        Guid snapshotId = returnValue.getActionReturnValue();
        setVmBackupSnapshot(snapshotId);
        getParameters().setAutoGeneratedSnapshotId(snapshotId);
        vmBackup.setDisks(getDisks());
        getParameters().setToCheckpointId(toCheckpointId);
        updateVmBackupPhase(VmBackupPhase.STARTING);
        setActionReturnValue(vmBackupId);

        setSucceeded(true);
    }

    @Override
    public boolean performNextOperation(int completedChildCount) {
        restoreCommandState();
        VmBackupPhase phase = getParameters().getVmBackup().getPhase();

        if (phase == VmBackupPhase.STARTING) {
            auditLog(AuditLogType.USER_CREATE_SNAPSHOT_FINISHED_SUCCESS);
            VmBackup vmBackup = getParameters().getVmBackup();
            Map<Guid, Guid> diskIdToSnapshot =
                    diskImageDao.getAllSnapshotsForVmSnapshot(getParameters().getAutoGeneratedSnapshotId())
                            .stream()
                            .collect(Collectors.toMap(DiskImage::getId, DiskImage::getImageId));
            TransactionSupport.executeInNewTransaction(() -> {
                getDisks().forEach(
                        disk -> {
                            setDiskBackupModeIfSupported(disk);
                            vmBackupDao.addDiskToVmBackup(vmBackup.getId(), disk.getId(),
                                    diskIdToSnapshot.get(disk.getId()));
                        });

                return null;
            });

            Guid toCheckpointId = createVmCheckpoint(getParameters().getToCheckpointId());
            vmBackup.setToCheckpointId(toCheckpointId);

            updateVmBackupPhase(VmBackupPhase.READY);
            return true;
        }

        if (phase == VmBackupPhase.READY) {
            if (getParameters().getVmBackup().isStopped()) {
                removeAutoGeneratedSnapshot(createStepsContext(StepEnum.MERGE_SNAPSHOTS), getActionType(), getParameters());
                updateVmBackupPhase(VmBackupPhase.REMOVING_SNAPSHOT);
            }
            return true;
        }

        return false;
    }

    @Override
    protected void endSuccessfully() {
        updateVmBackupPhase(VmBackupPhase.SUCCEEDED);
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        if (getParameters().getVmBackup().getPhase() == VmBackupPhase.REMOVING_SNAPSHOT) {
            auditLog(AuditLogType.VM_BACKUP_SNAPSHOT_POSSIBLE_LEFTOVER);
            endSuccessfully();
            return;
        }

        // Unset snapshot id, to avoid preventing snapshot removal
        // or creation
        VmBackup vmBackup = getParameters().getVmBackup();
        vmBackup.setSnapshotId(null);

        // The call to vmBackupDao#update is done here
        updateVmBackupPhase(VmBackupPhase.FAILED);

        removeAutoGeneratedSnapshotIfExistsDetached();

        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return callbackProvider.get();
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        Map<String, Pair<String, String>> locks = new HashMap<>();
        List<Disk> vmDisks = diskDao.getAllForVm(getVmId());
        List<DiskImage> diskImages = DisksFilter.filterImageDisks(vmDisks,
                ONLY_NOT_SHAREABLE, ONLY_SNAPABLE, ONLY_ACTIVE);
        diskImages.forEach(disk -> locks.put(disk.getId().toString(),
                LockMessagesMatchUtil.makeLockingPair(LockingGroup.DISK, EngineMessage.ACTION_TYPE_FAILED_DISK_IS_LOCKED)));
        return locks;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(LockProperties.Scope.Command);
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put("vm", getVmName());
        }

        return super.getJobMessageProperties();
    }

    private void auditLog(AuditLogType type) {
        addCustomValue("VmName", getVm().getName());
        addCustomValue("SnapshotName", StorageConstants.BACKUP_VM_AUTO_GENERATED_SNAPSHOT_DESCRIPTION);
        auditLogDirector.log(this, type);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        VmBackup vmBackup = getParameters().getVmBackup();
        addCustomValue("backupId", vmBackup.getId().toString());
        switch (getActionState()) {
            case EXECUTE:
                return AuditLogType.VM_BACKUP_STARTED;
            case END_FAILURE:
                if (vmBackup.getPhase() == VmBackupPhase.SUCCEEDED) {
                    return AuditLogType.VM_BACKUP_SUCCEEDED;
                }

                return AuditLogType.VM_BACKUP_FAILED;
            case END_SUCCESS:
                return AuditLogType.VM_BACKUP_SUCCEEDED;
        }
        return null;
    }

    @Override
    protected void restoreCommandState() {
        Guid backupId = getParameters().getVmBackup().getId();
        VmBackup vmBackup = vmBackupDao.get(backupId);
        if (CollectionUtils.isEmpty(vmBackup.getDisks())) {
            vmBackup.setDisks(getDisks());
        }

        getParameters().setVmBackup(vmBackup);
    }

    @Override
    protected List<DiskImage> getDisks() {
        return diskImageDao.getAllSnapshotsForVmSnapshot(getParameters().getAutoGeneratedSnapshotId());
    }

    private void removeAutoGeneratedSnapshotIfExistsDetached() {
        if (snapshotDao.exists(getVmId(), getParameters().getAutoGeneratedSnapshotId())) {
            // Try to remove the snapshot
            removeAutoGeneratedSnapshot(cloneContextAndDetachFromParent(), null, null);
        }
    }

    private void removeAutoGeneratedSnapshot(CommandContext commandContext, ActionType actionType, VmBackupParameters parameters) {
        RemoveSnapshotParameters removeSnapshotParameters =
                new RemoveSnapshotParameters(getParameters().getAutoGeneratedSnapshotId(), getVmId());
        removeSnapshotParameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        if (actionType != null) {
            removeSnapshotParameters.setParentCommand(actionType);
        }

        removeSnapshotParameters.setParentParameters(parameters);
        removeSnapshotParameters.setCorrelationId(getCorrelationId());

        runInternalAction(ActionType.RemoveSnapshot,
                removeSnapshotParameters,
                commandContext);
    }

    private CreateSnapshotForVmParameters buildCreateSnapshotParameters(Guid bitmapId) {
        CreateSnapshotForVmParameters parameters = new CreateSnapshotForVmParameters(
                getParameters().getVmId(),
                StorageConstants.BACKUP_VM_AUTO_GENERATED_SNAPSHOT_DESCRIPTION,
                false);
        parameters.setShouldBeLogged(false);
        parameters.setParentCommand(getActionType());
        parameters.setParentParameters(getParameters());
        parameters.setEntityInfo(getParameters().getEntityInfo());
        parameters.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);
        parameters.setBitmap(bitmapId);
        parameters.setDiskIds(getParameters().getVmBackup().getDisks()
                .stream()
                .map(DiskImage::getId)
                .collect(Collectors.toSet()));
        return parameters;
    }

    private CommandContext createStepsContext(StepEnum step) {
        Step addedStep = executionHandler.addSubStep(getExecutionContext(),
                getExecutionContext().getJob().getStep(StepEnum.EXECUTING),
                step,
                ExecutionMessageDirector.resolveStepMessage(step, Collections.emptyMap()));
        ExecutionContext ctx = new ExecutionContext();
        ctx.setStep(addedStep);
        ctx.setMonitored(true);
        return ExecutionHandler.createInternalJobContext(getContext(), null)
                .withExecutionContext(ctx);
    }

    private Guid createVmBackup() {
        VmBackup vmBackup = getParameters().getVmBackup();
        Guid backupId = vmBackup.getId() != null ? vmBackup.getId() : getCommandId();
        vmBackup.setId(backupId);
        vmBackup.setPhase(VmBackupPhase.INITIALIZING);
        Date now = new Date();
        vmBackup.setCreationDate(now);
        vmBackup.setModificationDate(now);
        vmBackup.setBackupType(VmBackupType.Hybrid);
        getParameters().setVmBackup(vmBackup);
        vmBackupDao.save(vmBackup);
        persistCommandIfNeeded();
        return vmBackup.getId();
    }

    private void setVmBackupSnapshot(Guid snapshotId) {
        VmBackup vmBackup = getParameters().getVmBackup();
        vmBackup.setSnapshotId(snapshotId);
        getParameters().setVmBackup(vmBackup);
        vmBackupDao.save(vmBackup);
        persistCommandIfNeeded();
    }
}
