package org.ovirt.engine.core.bll.aaa;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.JobDao;

@Singleton
public class SsoSessionUtils {

    public static final long EMPTY_SESSION_SEQ_ID = -1L;

    @Inject
    private JobDao jobDao;

    @Inject
    private Instance<CommandCoordinatorUtil> commandCoordinatorUtil;

    public boolean isSessionInUse(long sessionSeqId) {
        CommandStatus cmdStatus;
        for (Guid cmdId : commandCoordinatorUtil.get().getCommandIdsBySessionSeqId(sessionSeqId)) {
            cmdStatus = commandCoordinatorUtil.get().getCommandStatus(cmdId);
            if (cmdStatus == CommandStatus.NOT_STARTED || cmdStatus == CommandStatus.ACTIVE) {
                return true;
            }
        }

        return !jobDao.getJobsBySessionSeqIdAndStatus(sessionSeqId, JobExecutionStatus.STARTED).isEmpty();
    }
}
