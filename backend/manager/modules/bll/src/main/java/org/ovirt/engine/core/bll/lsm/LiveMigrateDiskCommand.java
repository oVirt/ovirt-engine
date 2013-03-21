package org.ovirt.engine.core.bll.lsm;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.bll.MoveOrCopyDiskCommand;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute
public class LiveMigrateDiskCommand<T extends LiveMigrateDiskParameters> extends MoveOrCopyDiskCommand<T> implements TaskHandlerCommand<LiveMigrateDiskParameters> {
    private static final long serialVersionUID = -6216729539906812205L;

    /* Constructors */

    public LiveMigrateDiskCommand(T parameters) {
        super(parameters);

        setStoragePoolId(getVm().getStoragePoolId());
        getParameters().setStoragePoolId(getStoragePoolId().getValue());

        getParameters().setVdsId(getVdsId());
        getParameters().setDiskAlias(getDiskAlias());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setCommandType(getActionType());
        getParameters().setTaskGroupSuccess(getParameters().getTaskGroupSuccess()
                && getVm().getStatus().isRunningOrPaused());
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
        return true;
    }

    @Override
    public VM getVm() {
        VM vm = super.getVm();
        if (vm == null) {
            vm = getVmDAO().getVmsListForDisk(getImageGroupId()).get(0);
            setVm(vm);
            setVmId(vm.getId());
        }
        return vm;
    }

    @Override
    public Guid getVdsId() {
        return getVm().getRunOnVds() != null ? getVm().getRunOnVds().getValue() : Guid.Empty;
    }

    /* Overridden stubs declared as public in order to implement ITaskHandlerCommand */

    @Override
    public T getParameters() {
        return super.getParameters();
    }

    @Override
    public Guid createTask(AsyncTaskCreationInfo asyncTaskCreationInfo,
            VdcActionType parentCommand,
            VdcObjectType entityType,
            Guid... entityIds) {
        return super.createTask(asyncTaskCreationInfo, parentCommand, entityType, entityIds);
    }

    @Override
    public VdcActionType getActionType() {
        return super.getActionType();
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
        ExecutionHandler.setAsyncJob(getExecutionContext(), false);
    }

    private boolean isFirstTaskHandler() {
        return getParameters().getExecutionIndex() == 0;
    }

    private boolean isLastTaskHandler() {
        return getParameters().getExecutionIndex() == getTaskHandlers().size() - 1;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        switch (getActionState()) {
        case EXECUTE:
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
}
