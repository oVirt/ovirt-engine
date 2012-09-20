package org.ovirt.engine.core.bll.lsm;

import org.ovirt.engine.core.bll.AbstractSPMAsyncTaskHandler;
import org.ovirt.engine.core.bll.tasks.TaskHandlerCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.LiveMigrateDiskParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.vdscommands.TargetDomainImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSParametersBase;
import org.ovirt.engine.core.compat.Guid;

public class CreateImagePlaceholderTaskHandler extends AbstractSPMAsyncTaskHandler<TaskHandlerCommand<? extends LiveMigrateDiskParameters>> {

    public CreateImagePlaceholderTaskHandler(TaskHandlerCommand<? extends LiveMigrateDiskParameters> cmd) {
        super(cmd);
    }

    @Override
    protected void beforeTask() {
        // No-op before the sync image command in the database side
    }

    @Override
    protected VDSCommandType getVDSCommandType() {
        return VDSCommandType.CloneImageGroupStructure;
    }

    @Override
    public AsyncTaskType getTaskType() {
        return AsyncTaskType.cloneImageStructure;
    }

    @Override
    protected VDSParametersBase getVDSParameters() {
        return new TargetDomainImageGroupVDSCommandParameters(
                getEnclosingCommand().getParameters().getStoragePoolId().getValue(),
                getEnclosingCommand().getParameters().getSourceStorageDomainId(),
                getEnclosingCommand().getParameters().getImageGroupID(),
                getEnclosingCommand().getParameters().getTargetStorageDomainId());
    }

    @Override
    protected VdcObjectType getTaskObjectType() {
        return VdcObjectType.VM;
    }

    @Override
    protected Guid[] getTaskObjects() {
        return new Guid[] { getEnclosingCommand().getParameters().getVmId() };
    }

    @Override
    protected void revertTask() {
        // No op reverting in the database side
    }

    @Override
    protected VDSCommandType getRevertVDSCommandType() {
        // VDSM handles the failed cloneImageGroupStructure, so no action required here.
        return null;
    }

    @Override
    public AsyncTaskType getRevertTaskType() {
        // VDSM handles the failed cloneImageGroupStructure, so no action required here.
        return null;
    }

    @Override
    protected VDSParametersBase getRevertVDSParameters() {
        // VDSM handles the failed cloneImageGroupStructure, so no action required here.
        return null;
    }
}
