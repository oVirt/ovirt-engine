package org.ovirt.engine.core.bll.tasks.interfaces;

import java.util.List;

import org.ovirt.engine.core.compat.Guid;

public abstract class CommandCallback {

    public CommandCallback() {}

    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
    }

    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
    }

    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
    }
}
