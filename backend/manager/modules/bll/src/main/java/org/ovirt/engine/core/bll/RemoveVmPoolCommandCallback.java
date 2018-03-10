package org.ovirt.engine.core.bll;

import java.util.List;

import javax.enterprise.inject.Typed;
import javax.inject.Inject;

import org.ovirt.engine.core.common.action.VmPoolParametersBase;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.backendcompat.CommandExecutionStatus;
import org.ovirt.engine.core.dao.VmPoolDao;

@Typed(RemoveVmPoolCommandCallback.class)
public class RemoveVmPoolCommandCallback extends ConcurrentChildCommandsExecutionCallback {
    @Inject
    private VmPoolDao vmPoolDao;

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
            VmPool pool = vmPoolDao.get(removeVmPoolCommand.getVmPoolId());
            if (pool == null || pool.getRunningVmsCount() == 0) {
                setCommandEndStatus(command, false, status, childCmdIds);
            }
        }
    }
}
