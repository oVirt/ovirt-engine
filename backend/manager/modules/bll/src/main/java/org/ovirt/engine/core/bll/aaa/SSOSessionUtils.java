package org.ovirt.engine.core.bll.aaa;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;

public class SSOSessionUtils {

    public static final long EMPTY_SESSION_SEQ_ID = -1L;

    public boolean isSessionInUse(long sessionSeqId) {
        CommandStatus cmdStatus;
        for (Guid cmdId : CommandCoordinatorUtil.getCommandIdsBySessionSeqId(sessionSeqId)) {
            cmdStatus = CommandCoordinatorUtil.getCommandStatus(cmdId);
            if (cmdStatus == CommandStatus.NOT_STARTED || cmdStatus == CommandStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }
}
