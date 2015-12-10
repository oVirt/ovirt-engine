package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.CommandEntity;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.CommandStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmPoolCommandCallback extends CommandCallback {

    @Override
    public void doPolling(Guid cmdId, List<Guid> childCmdIds) {
        RemoveVmPoolCommand<? extends VmPoolParametersBase> command = getCommand(cmdId);

        boolean anyFailed = false;
        for (Guid childCmdId : childCmdIds) {
            CommandEntity entity = CommandCoordinatorUtil.getCommandEntity(childCmdId);
            switch (entity.getCommandStatus()) {
                case ENDED_WITH_FAILURE:
                case FAILED:
                case FAILED_RESTARTED:
                case UNKNOWN:
                    anyFailed = true;
                    break;

                default:
                    break;
            }
        }

        if (anyFailed) {
            command.setCommandStatus(CommandStatus.FAILED);
        } else {
            VmPool pool = DbFacade.getInstance().getVmPoolDao().get(command.getVmPoolId());
            if (pool == null || pool.getRunningVmsCount() == 0) {
                command.setCommandStatus(CommandStatus.SUCCEEDED);
            }
        }
    }

    @Override
    public void onFailed(Guid cmdId, List<Guid> childCmdIds) {
        RemoveVmPoolCommand<? extends VmPoolParametersBase> cmd = getCommand(cmdId);
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
        cmd.getParameters().setTaskGroupSuccess(false);
        cmd.endAction();
    }

    @Override
    public void onSucceeded(Guid cmdId, List<Guid> childCmdIds) {
        RemoveVmPoolCommand<? extends VmPoolParametersBase> cmd = getCommand(cmdId);
        CommandCoordinatorUtil.removeAllCommandsInHierarchy(cmdId);
        cmd.getParameters().setTaskGroupSuccess(true);
        cmd.endAction();
    }

    private RemoveVmPoolCommand<? extends VmPoolParametersBase> getCommand(Guid cmdId) {
        return CommandCoordinatorUtil.retrieveCommand(cmdId);
    }

}
