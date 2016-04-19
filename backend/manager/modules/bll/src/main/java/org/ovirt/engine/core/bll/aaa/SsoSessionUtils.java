package org.ovirt.engine.core.bll.aaa;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;

public class SsoSessionUtils {

    public static final long EMPTY_SESSION_SEQ_ID = -1L;

    @Inject
    private JobDao jobDao;

    public boolean isSessionInUse(long sessionSeqId) {
        CommandStatus cmdStatus;
        for (Guid cmdId : CommandCoordinatorUtil.getCommandIdsBySessionSeqId(sessionSeqId)) {
            cmdStatus = CommandCoordinatorUtil.getCommandStatus(cmdId);
            if (cmdStatus == CommandStatus.NOT_STARTED || cmdStatus == CommandStatus.ACTIVE) {
                return true;
            }
        }

        return !jobDao.getJobsBySessionSeqIdAndStatus(sessionSeqId, JobExecutionStatus.STARTED).isEmpty();
    }
}
