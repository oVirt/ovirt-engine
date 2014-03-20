package org.ovirt.engine.core.bll.lsm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.MoveOrCopyDiskCommand;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.validator.DiskValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;

@NonTransactiveCommandAttribute
public class LiveMigrateDiskCommand<T extends LiveMigrateDiskParameters> extends MoveOrCopyDiskCommand<T> implements TaskHandlerCommand<LiveMigrateDiskParameters> {

    public LiveMigrateDiskCommand(T parameters) {
        super(parameters);

        if (isLastTaskHandler()) {
            // No need to initialize values if all tasks have been completed
            // (prevent fetching un-needed/stale data after last task completion)
            return;
        }

        setStoragePoolId(getVm().getStoragePoolId());
        getParameters().setStoragePoolId(getStoragePoolId());

        getParameters().setVdsId(getVdsId());
        getParameters().setDiskAlias(getDiskAlias());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setCommandType(getActionType());
    }

    public LiveMigrateDiskCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);

        if (isLastTaskHandler()) {
            // No need to initialize values if all tasks have been completed
            // (prevent fetching un-needed/stale data after last task completion)
            return;
        }

        setStoragePoolId(getVm().getStoragePoolId());
        getParameters().setStoragePoolId(getStoragePoolId());

        getParameters().setVdsId(getVdsId());
        getParameters().setDiskAlias(getDiskAlias());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setCommandType(getActionType());
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Command);
    }

    /* Overridden CommandBase Methods */

    @Override
    protected List<SPMAsyncTaskHandler> initTaskHandlers() {
        return Arrays.<SPMAsyncTaskHandler> asList(
                new CreateImagePlaceholderTaskHandler(this),
                new VmReplicateDiskStartTaskHandler(this),
                new VmReplicateDiskFinishTaskHandler(this)
                );
    }

    @Override
    protected boolean checkCanBeMoveInVm() {
        return validate(createDiskValidator(getDiskImage()).isDiskPluggedToVmsThatAreNotDown(true, getVmsWithVmDeviceInfoForDiskId()));
    }

    @Override
    public VM getVm() {
        VM vm = super.getVm();
        if (vm == null) {
            vm = getVmDAO().getVmsListForDisk(getImageGroupId(), false).get(0);
            setVm(vm);
            setVmId(vm.getId());
        }
        return vm;
    }

    @Override
    public Guid getVdsId() {
        return getVm().getRunOnVds() != null ? getVm().getRunOnVds() : Guid.Empty;
    }

    /* Overridden stubs declared as public in order to implement ITaskHandlerCommand */

    @Override
    public T getParameters() {
        return super.getParameters();
    }

    @Override
    public Guid createTask(Guid taskId,
            AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    @Override
    public VdcActionType getActionType() {
        return super.getActionType();
    }

    @Override
    public Guid createTask(Guid taskId, AsyncTaskCreationInfo asyncTaskCreationInfo, VdcActionType parentCommand) {
        return super.createTask(taskId, asyncTaskCreationInfo, parentCommand);
    }

    @Override
    public ArrayList<Guid> getTaskIdList() {
        return super.getTaskIdList();
    }

    @Override
    public void taskEndSuccessfully() {
        // Not implemented
    }

    public Guid persistAsyncTaskPlaceHolder() {
        return super.persistAsyncTaskPlaceHolder(getActionType());
    }

    public Guid persistAsyncTaskPlaceHolder(String taskKey) {
        return super.persistAsyncTaskPlaceHolder(getActionType(), taskKey);
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        if (isLastTaskHandler()) {
            ExecutionHandler.setAsyncJob(getExecutionContext(), false);
        }
    }

    @Override
    protected void endWithFailure() {
        super.endWithFailure();
    }

    @Override
    public void preventRollback() {
        getParameters().setExecutionIndex(0);

        // We should always unlock the disk
        ImagesHandler.updateAllDiskImageSnapshotsStatus(getParameters().getImageGroupID(), ImageStatus.OK);
    }

    private boolean isFirstTaskHandler() {
        return getParameters().getExecutionIndex() == 0;
    }

    private boolean isLastTaskHandler() {
        return getParameters().getExecutionIndex() == getTaskHandlers().size() - 1;
    }

    @Override
    protected boolean canDoAction() {
        boolean canDoAction = super.canDoAction();

        if (!canDoAction) {
            AuditLogDirector.log(this, AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE);
        }

        return canDoAction;
    }

    @Override
    protected boolean isImageNotLocked() {
        // During LSM the disks are being locked prior to the snapshot phase
        // therefore returning true here.
        return true;
    }

    protected DiskValidator createDiskValidator(DiskImage disk) {
        return new DiskValidator(disk);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
            if (!getParameters().getTaskGroupSuccess()) {
                return AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE;
            }
            if (isFirstTaskHandler() && getSucceeded()) {
                return AuditLogType.USER_MOVED_VM_DISK;
            }
            break;

        case END_SUCCESS:
            return AuditLogType.USER_MOVED_VM_DISK_FINISHED_SUCCESS;

        case END_FAILURE:
            return AuditLogType.USER_MOVED_VM_DISK_FINISHED_FAILURE;
        }

        return AuditLogType.UNASSIGNED;
    }

    @Override
    protected Map<String, Pair<String, String>> getExclusiveLocks() {
        return null;
    }
}
