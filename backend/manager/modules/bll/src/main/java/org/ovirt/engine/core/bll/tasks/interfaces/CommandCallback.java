package org.ovirt.engine.core.bll.tasks.interfaces;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Guid;

import java.util.List;

public abstract class CommandCallback {

    public CommandCallback() {}

    public void executed(VdcReturnValueBase result) {
        return;
    }

    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        return;
    }

    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        return;
    }

    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        return;
    }

}
