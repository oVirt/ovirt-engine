package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.TaskManagerUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallBack;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

public class MergeCommandCallback extends CommandCallBack {
    private static final Log log = LogFactory.getLog(MergeCommandCallback.class);

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        // If the VM Job exists, the command is still active
        boolean isRunning = false;
        MergeCommand<MergeParameters> command = getCommand(cmdId);
        List<VmJob> vmJobs = DbFacade.getInstance().getVmJobDao().getAllForVmDisk(
                command.getParameters().getVmId(),
                command.getParameters().getImageGroupId());
        for (VmJob vmJob : vmJobs) {
            if (vmJob.getId().equals(command.getParameters().getVmJobId())) {
                isRunning = true;
                log.info("Waiting on merge command to complete");
                break;
            }
        }

        if (!isRunning) {
            // It finished; a command will be called later to determine the status.
            command.setSucceeded(true);
            command.setCommandStatus(CommandStatus.SUCCEEDED);
            command.persistCommandWithContext(command.getParameters().getParentCommand(), true);
        }
        log.infoFormat("Merge command has completed for images {0}..{1}",
                command.getParameters().getBaseImage().getImageId(),
                command.getParameters().getTopImage().getImageId());
    }

    private MergeCommand<MergeParameters> getCommand(Guid cmdId) {
        return (MergeCommand<MergeParameters>) TaskManagerUtil.retrieveCommand(cmdId);
    }
}
