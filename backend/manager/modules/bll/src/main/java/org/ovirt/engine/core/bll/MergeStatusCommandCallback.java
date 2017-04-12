package org.ovirt.engine.core.bll;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.compat.Guid;

@Typed(MergeStatusCommandCallback.class)
public class MergeStatusCommandCallback implements CommandCallback {
    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        getCommand(cmdId).attemptResolution();
    }

    private MergeStatusCommand<MergeParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
