package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MergeExtendCommandCallback extends CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(MergeExtendCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        boolean failed = false;
        for (Guid childCmdId : childCmdIds) {
            switch (CommandCoordinatorUtil.getCommandStatus(childCmdId)) {
            case ACTIVE:
                log.info("Waiting on disk extension child commands to complete");
                return;
            case ENDED_WITH_FAILURE:
            case FAILED:
            case EXECUTION_FAILED:
            case UNKNOWN:
                failed = true;
                break;
            default:
                break;
            }
        }

        MergeExtendCommand<MergeParameters> command = getCommand(cmdId);
        command.setCommandStatus(failed ? CommandStatus.FAILED : CommandStatus.SUCCEEDED);
        log.info("Disk extension complete, status '{}'", command.getCommandStatus());
    }

    private MergeExtendCommand<MergeParameters> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
