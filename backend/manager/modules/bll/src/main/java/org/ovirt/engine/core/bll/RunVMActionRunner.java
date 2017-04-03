package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmStaticDao;

public class RunVMActionRunner extends SortedMultipleActionsRunnerBase {

    @Inject
    private VmStaticDao vmStaticDao;

    public RunVMActionRunner(VdcActionType actionType, ArrayList<VdcActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected void sortCommands() {

        ArrayList<CommandBase<?>> commandsList = getCommands();
        HashMap<Guid, RunVmCommandBase<?>> runVmCommandsMap = new HashMap<>();
        for (CommandBase<?> command : commandsList) {
            RunVmCommandBase<?> runVMCommandBase = (RunVmCommandBase<?>) command;
            runVmCommandsMap.put(runVMCommandBase.getVmId(), runVMCommandBase);
        }

        List<Guid> guids = new ArrayList<>();
        guids.addAll(runVmCommandsMap.keySet());
        List<Guid> orderedGuids = vmStaticDao.getOrderedVmGuidsForRunMultipleActions(guids);

        commandsList.clear();

        for (Guid guid : orderedGuids) {
            commandsList.add(runVmCommandsMap.get(guid));
        }

    }

}
