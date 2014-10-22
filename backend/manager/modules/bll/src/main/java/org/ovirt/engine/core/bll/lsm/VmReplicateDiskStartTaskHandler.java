package org.ovirt.engine.core.bll.lsm;

import org.ovirt.engine.core.bll.AbstractSPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.common.vdscommands.SyncImageGroupDataVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.common.vdscommands.VmReplicateDiskParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.ResourceManager;

public class VmReplicateDiskStartTaskHandler extends AbstractSPMAsyncTaskHandler<TaskHandlerCommand<? extends LiveMigrateDiskParameters>> {

    public VmReplicateDiskStartTaskHandler(TaskHandlerCommand<? extends LiveMigrateDiskParameters> cmd) {
        super(cmd);
    }

    @Override
    protected void beforeTask() {
        if (Guid.Empty.equals(getEnclosingCommand().getParameters().getVdsId())) {
            throw new VdcBLLException(VdcBllErrors.down,
                    "VM " + getEnclosingCommand().getParameters().getVmId() + " is not running on any VDS");
        }

        // Start disk migration
        VmReplicateDiskParameters migrationStartParams = new VmReplicateDiskParameters
                (getEnclosingCommand().getParameters().getVdsId(),
                        getEnclosingCommand().getParameters().getVmId(),
                        getEnclosingCommand().getParameters().getStoragePoolId(),
                        getEnclosingCommand().getParameters().getSourceStorageDomainId(),
                        getEnclosingCommand().getParameters().getTargetStorageDomainId(),
                        getEnclosingCommand().getParameters().getImageGroupID(),
                        getEnclosingCommand().getParameters().getDestinationImageId()
                );
        VDSReturnValue ret =
                ResourceManager.getInstance().runVdsCommand(VDSCommandType.VmReplicateDiskStart, migrationStartParams);

        if (!ret.getSucceeded()) {
            log.error("Failed VmReplicateDiskStart (Disk '{}' , VM '{}')",
                    getEnclosingCommand().getParameters().getImageGroupID(),
                    getEnclosingCommand().getParameters().getVmId());
            throw new VdcBLLException(ret.getVdsError().getCode(), ret.getVdsError().getMessage());
        }
    }

    @Override
    protected VDSCommandType getVDSCommandType() {
        return VDSCommandType.SyncImageGroupData;
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.syncImageData;
    }

    @Override
    protected VDSParametersBase getVDSParameters() {
        return new SyncImageGroupDataVDSCommandParameters(
                getEnclosingCommand().getParameters().getStoragePoolId(),
                getEnclosingCommand().getParameters().getSourceStorageDomainId(),
                getEnclosingCommand().getParameters().getImageGroupID(),
                getEnclosingCommand().getParameters().getTargetStorageDomainId(),
                SyncImageGroupDataVDSCommandParameters.SYNC_TYPE_INTERNAL);
    }

    @Override
    protected void revertTask() {
        if (Guid.Empty.equals(getEnclosingCommand().getParameters().getVdsId())) {
            log.error("VM '{}' is not running on any VDS, skipping VmReplicateDiskFinish",
                    getEnclosingCommand().getParameters().getVmId());
            return;
        }

        // Undo the replicateStart - use replicateFinish back to the source
        VmReplicateDiskParameters migrationStartParams = new VmReplicateDiskParameters
                (getEnclosingCommand().getParameters().getVdsId(),
                        getEnclosingCommand().getParameters().getVmId(),
                        getEnclosingCommand().getParameters().getStoragePoolId(),
                        getEnclosingCommand().getParameters().getSourceStorageDomainId(),
                        getEnclosingCommand().getParameters().getSourceStorageDomainId(),
                        getEnclosingCommand().getParameters().getImageGroupID(),
                        getEnclosingCommand().getParameters().getDestinationImageId()
                );

        try {
            VDSReturnValue ret = ResourceManager.getInstance().runVdsCommand(
                    VDSCommandType.VmReplicateDiskFinish, migrationStartParams);
            if (!ret.getSucceeded()) {
                getEnclosingCommand().preventRollback();
            }
        } catch (RuntimeException e) {
            getEnclosingCommand().preventRollback();
        }
    }

    @Override
    protected VDSCommandType getRevertVDSCommandType() {
        // VDSM handles the failure, so no action required here.
        return null;
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        // VDSM handles the failure, so no action required here.
        return null;
    }

    @Override
    protected VDSParametersBase getRevertVDSParameters() {
        // VDSM handles the failure, so no action required here.
        return null;
    }

    @Override
    public void endWithFailure() {
        super.endWithFailure();
        revertTask();
    }

    @Override
    protected VdcObjectType getTaskObjectType() {
        return VdcObjectType.VM;
    }

    @Override
    protected Guid[] getTaskObjects() {
        return new Guid[] { getEnclosingCommand().getParameters().getVmId() };
    }
}
