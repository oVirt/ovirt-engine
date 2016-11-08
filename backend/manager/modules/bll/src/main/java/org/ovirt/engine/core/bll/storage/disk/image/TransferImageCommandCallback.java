package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.TransferImageParameters;
import org.ovirt.engine.core.compat.Guid;

public class TransferImageCommandCallback implements CommandCallback {
    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).proceedCommandExecution(childCmdIds.isEmpty() ? null : childCmdIds.get(0));
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).onSucceeded();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).onFailed();
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    private TransferImageCommand<TransferImageParameters> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }

}
