package org.ovirt.engine.core.bll.storage;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractCinderDiskCommandCallback<C extends CommandBase<?>> extends CommandCallback {
    protected static final Logger log = LoggerFactory.getLogger(AbstractCinderDiskCommandCallback.class);

    private Guid cmdId;

    private C command;

    protected CinderDisk disk;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        this.cmdId = cmdId;
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        this.cmdId = cmdId;
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        this.cmdId = cmdId;
    }

    protected C getCommand() {
        if (command == null) {
            command = CommandCoordinatorUtil.retrieveCommand(cmdId);
        }
        return command;
    }

    protected abstract CinderBroker getCinderBroker();

    protected abstract Guid getDiskId();

    protected abstract CinderDisk getDisk();
}
