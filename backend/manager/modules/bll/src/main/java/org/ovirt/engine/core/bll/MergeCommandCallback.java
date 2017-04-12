package org.ovirt.engine.core.bll;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.MergeParameters;
import org.ovirt.engine.core.common.businessentities.VmJob;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.vdsbroker.monitoring.VmJobsMonitoring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Typed(MergeCommandCallback.class)
public class MergeCommandCallback implements CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(MergeCommandCallback.class);

    @Inject
    private VmJobsMonitoring vmJobsMonitoring;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        MergeCommand<MergeParameters> command = getCommand(cmdId);
        Guid jobId = command.getParameters().getVmJobId();
        VmJob vmJob = vmJobsMonitoring.getJobById(jobId);
        // If the VM Job exists, the command is still active
        if (vmJob != null) {
            log.info("Waiting on merge command to complete (jobId = {})", jobId);
            return;
        }

        // It finished; a command will be called later to determine the status.
        command.setSucceeded(true);
        command.setCommandStatus(CommandStatus.SUCCEEDED);
        command.persistCommand(command.getParameters().getParentCommand(), true);
        log.info("Merge command (jobId = {}) has completed for images '{}'..'{}'",
                jobId,
                command.getParameters().getBaseImage().getImageId(),
                command.getParameters().getTopImage().getImageId());
    }

    private MergeCommand<MergeParameters> getCommand(Guid cmdId) {
        return commandCoordinatorUtil.retrieveCommand(cmdId);
    }
}
