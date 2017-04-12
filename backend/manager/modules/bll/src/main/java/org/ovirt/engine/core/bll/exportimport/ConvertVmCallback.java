package org.ovirt.engine.core.bll.exportimport;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ConvertVmParameters;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Typed(ConvertVmCallback.class)
public class ConvertVmCallback implements CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(ConvertVmCallback.class);
    private static final String JOB_DOES_NOT_EXIST_MSG = "Lost contact with the conversion process";

    private Guid cmdId;
    private ConvertVmCommand<? extends ConvertVmParameters> cachedCommand;

    @Inject
    private CommandCoordinatorUtil commandCoordinatorUtil;

    protected ConvertVmCallback(Guid cmdId) {
        this.cmdId = cmdId;
    }

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        V2VJobInfo jobInfo = getV2VJobInfo();
        switch (jobInfo.getStatus()) {
        case STARTING:
        case COPYING_DISK:
            updateProgress(jobInfo);
            break;
        case DONE:
            updateProgress("Finalizing", 100);
            getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
            break;
        case NOT_EXIST:
            jobInfo.setDescription(JOB_DOES_NOT_EXIST_MSG);
        case ERROR:
            log.info("Conversion of VM from external environment failed: {}", jobInfo.getDescription());
        case ABORTED:
            updateProgress("Canceling", 0);
            getCommand().setCommandStatus(CommandStatus.FAILED);
            break;
        case UNKNOWN:
        case WAIT_FOR_START:
        default:
        }
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        getCommand().getParameters().setTaskGroupSuccess(false);
        getCommand().endAction();
        clearProgress();
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand().getParameters().setTaskGroupSuccess(true);
        getCommand().endAction();
        clearProgress();
    }

    private V2VJobInfo getV2VJobInfo() {
        ConvertVmCommand<?> command = getCommand();
        return command.getVdsManager().getV2VJobInfoForVm(command.getVmId());
    }

    private void updateProgress(V2VJobInfo jobInfo) {
        updateProgress(
                StringUtils.EMPTY.equals(jobInfo.getDescription()) ? "Initializing" : jobInfo.getDescription(),
                jobInfo.getProgress());
    }

    private void clearProgress() {
        updateProgress(null, -1);
    }

    private void updateProgress(String description, int progress) {
        getCommand().getVmManager().updateConvertOperation(description, progress);
    }

    private ConvertVmCommand<? extends ConvertVmParameters> getCommand() {
        if (cachedCommand == null) {
            cachedCommand = commandCoordinatorUtil.retrieveCommand(cmdId);
        }
        return cachedCommand;
    }

}
