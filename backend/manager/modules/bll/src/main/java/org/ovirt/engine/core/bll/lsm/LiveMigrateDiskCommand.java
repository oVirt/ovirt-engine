package org.ovirt.engine.core.bll.lsm;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.MoveOrCopyDiskCommand;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.bll.tasks.SPMAsyncTaskHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogField;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.CustomLogFields;

@CustomLogFields({ @CustomLogField("DiskAlias") })
@NonTransactiveCommandAttribute
public class LiveMigrateDiskCommand<T extends LiveMigrateDiskParameters> extends MoveOrCopyDiskCommand<T> implements TaskHandlerCommand<LiveMigrateDiskParameters> {
    private static final long serialVersionUID = -6216729539906812205L;

    private Map<String, String> exclusiveLockMap;

    /* Constructors */

    public LiveMigrateDiskCommand(T parameters) {
        super(parameters);

        if (VMStatus.Up != getVm().getstatus()) {
            log.infoFormat("Rollback LiveMigrateDiskCommand - VM {0} is not UP", getVm().getvm_name());
            handleVmNotUp();
            return;
        }

        setVdsId(getVm().getrun_on_vds().getValue());
        getParameters().setVdsId(getVdsId());

        setStoragePoolId(getVm().getstorage_pool_id());
        getParameters().setStoragePoolId(getStoragePoolId().getValue());

        getParameters().setDiskAlias(getDiskAlias());
        getParameters().setImageGroupID(getImageGroupId());
        getParameters().setCommandType(getActionType());
    }

    /* Overridden CommandBase Methods */

    @Override
    protected List<SPMAsyncTaskHandler> initTaskHandlers() {
        return Arrays.<SPMAsyncTaskHandler> asList(
                new LiveSnapshotTaskHandler(this),
                new CreateImagePlaceholderTaskHandler(this),
                new VmReplicateDiskStartTaskHandler(this),
                new VmReplicateDiskFinishTaskHandler(this)
                );
    }


    @Override
    protected boolean checkCanBeMoveInVm() {
        if (!Config.<Boolean> GetValue(
                ConfigValues.LiveStorageMigrationEnabled,
                getStoragePool().getcompatibility_version().toString())) {
            return failCanDoAction(VdcBllMessages.ACTION_NOT_SUPPORTED_FOR_CLUSTER_POOL_LEVEL);
        }

        List<VM> vmsForDisk = getVmDAO().getForDisk(getImageGroupId()).get(Boolean.TRUE);

        if (vmsForDisk.size() == 0) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_FLOATING_DISK_NOT_SUPPORTED);
        }

        if (vmsForDisk.size() > 1) {
            return failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_SHARED_DISK_NOT_SUPPORTED);
        }

        // Cache for future use
        VM vm = vmsForDisk.get(0);
        setVmId(vm.getId());
        setVm(vm);

        if (VMStatus.Up != vm.getstatus()) {
            failCanDoAction(VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_UP);
        }
        return true;
    }

    /** Live migration is not allowed if any of the VM's disks are locked, since we need to perform a live snapshot*/
    @Override
    protected boolean isImageIsNotLocked() {
        return ImagesHandler.checkImagesLocked(getVm(), getReturnValue().getCanDoActionMessages());
    }

    @Override
    protected void lockForMove(Map<String, String> lockMap) {
        exclusiveLockMap = lockMap;
    }

    @Override
    protected Map<String, String> getExclusiveLocks() {
        return exclusiveLockMap;
    }

    @Override
    public VM getVm() {
        VM vm = super.getVm();
        if (vm == null) {
            vm = getVmDAO().getForDisk(getImageGroupId()).get(Boolean.TRUE).get(0);
            setVm(vm);
            setVmId(vm.getId());
        }
        return vm;
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

    protected void handleVmNotUp() {
        if (isEndSuccessfully()) {
            getParameters().incrementExecutionIndex();
        }
        getParameters().setTaskGroupSuccess(false);
        setSucceeded(true);
    }
}
