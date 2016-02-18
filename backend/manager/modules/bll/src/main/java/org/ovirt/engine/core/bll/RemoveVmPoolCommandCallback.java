package org.ovirt.engine.core.bll;

import java.util.List;

import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RemoveVmPoolCommandCallback extends ConcurrentChildCommandsExecutionCallback {

    @Override
    protected void childCommandsExecutionEnded(CommandBase<?> command,
            boolean anyFailed,
            List<Guid> childCmdIds,
            CommandExecutionStatus status,
            int completedChildren) {

        if (anyFailed) {
            setCommandEndStatus(command, true, status, childCmdIds);
        } else {
            RemoveVmPoolCommand<? extends VmPoolParametersBase> removeVmPoolCommand =
                    (RemoveVmPoolCommand<? extends VmPoolParametersBase>) command;
            VmPool pool = DbFacade.getInstance().getVmPoolDao().get(removeVmPoolCommand.getVmPoolId());
            if (pool == null || pool.getRunningVmsCount() == 0) {
                setCommandEndStatus(command, false, status, childCmdIds);
            }
        }
    }
}
