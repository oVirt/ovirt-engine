package org.ovirt.engine.core.bll.snapshots;

import java.util.List;

import org.ovirt.engine.core.bll.SerialChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.compat.Guid;

public class RemoveSnapshotSingleDiskLiveCommandCallback extends SerialChildCommandsExecutionCallback {


    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getRemoveSnapshotCommand(cmdId).onFailed();
        super.onFailed(cmdId, childCmdIds);
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getRemoveSnapshotCommand(cmdId).onSucceeded();
        super.onSucceeded(cmdId, childCmdIds);
    }

    protected RemoveSnapshotSingleDiskLiveCommand<RemoveSnapshotSingleDiskParameters> getRemoveSnapshotCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
