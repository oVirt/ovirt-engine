package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.TransferDiskImageParameters;
import org.ovirt.engine.core.compat.Guid;

@Typed(TransferImageCommandCallback.class)
public class TransferImageCommandCallback implements CommandCallback {

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).proceedCommandExecution(childCmdIds.isEmpty() ? null : childCmdIds.get(0));
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).endAction();
        commandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).getParameters().setTaskGroupSuccess(false);
        getCommand(cmdId).endAction();
        commandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
    }

    private TransferDiskImageCommand<TransferDiskImageParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }

}
