package org.ovirt.engine.core.bll.snapshots;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.RemoveSnapshotSingleDiskParameters;
import org.ovirt.engine.core.compat.Guid;

public class RemoveSnapshotSingleDiskLiveCommandCallback extends CommandCallback {
    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).proceedCommandExecution();
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).onFailed();
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).onSucceeded();
    }

    private RemoveSnapshotSingleDiskLiveCommand<RemoveSnapshotSingleDiskParameters> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
