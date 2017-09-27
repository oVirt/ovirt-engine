package org.ovirt.engine.core.bll;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.storage.disk.image.DestroyImageCommand;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.DestroyImageParameters;
import org.ovirt.engine.core.compat.Guid;

@Typed(DestroyImageCommandCallback.class)
public class DestroyImageCommandCallback implements CommandCallback {
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).executeCommand();
    }

    private DestroyImageCommand<DestroyImageParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
