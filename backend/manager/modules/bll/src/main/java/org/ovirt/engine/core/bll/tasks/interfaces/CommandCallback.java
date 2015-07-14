package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.compat.Guid;

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
