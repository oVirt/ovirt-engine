package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ConvertVmParameters;
import org.ovirt.engine.core.common.businessentities.V2VJobInfo;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConvertVmCallback extends CommandCallback {
    private static final Logger log = LoggerFactory.getLogger(ConvertVmCallback.class);
    private static final String JOB_DOES_NOT_EXIST_MSG = "Lost contact with the conversion process";

    private Guid cmdId;
    private ConvertVmCommand<ConvertVmParameters> cachedCommand;

    protected ConvertVmCallback(Guid cmdId) {
        this.cmdId = cmdId;
    }

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        V2VJobInfo jobInfo = getV2VJobInfo();
        switch (jobInfo.getStatus()) {
        case STARTING:
        case COPYING_DISK:
            break;
        case DONE:
            getCommand().setCommandStatus(CommandStatus.SUCCEEDED);
            break;
        case NOT_EXIST:
            jobInfo.setDescription(JOB_DOES_NOT_EXIST_MSG);
        case ERROR:
            log.info("Conversion of VM from exteral enironment failed: {}", jobInfo.getDescription());
        case ABORTED:
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
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        getCommand().getParameters().setTaskGroupSuccess(true);
        getCommand().endAction();
    }

    private V2VJobInfo getV2VJobInfo() {
        ConvertVmCommand<?> command = getCommand();
        return command.getVdsManager().getV2VJobInfoForVm(command.getVmId());
    }

    private ConvertVmCommand<ConvertVmParameters> getCommand() {
        if (cachedCommand == null) {
            cachedCommand = CommandCoordinatorUtil.retrieveCommand(cmdId);
        }
        return cachedCommand;
    }

}
