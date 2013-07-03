package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class RunVMActionRunner extends SortedMultipleActionsRunnerBase {

    public RunVMActionRunner(VdcActionType actionType, ArrayList<VdcActionParametersBase> parameters, boolean isInternal) {
        super(actionType, parameters, isInternal);
    }

    @Override
    protected void SortCommands() {

        ArrayList<CommandBase<?>> commandsList = getCommands();
        HashMap<Guid, RunVmCommandBase<?>> runVmCommandsMap = new HashMap<Guid, RunVmCommandBase<?>>();
        for (CommandBase<?> command : commandsList) {
            RunVmCommandBase<?> runVMCommandBase = (RunVmCommandBase<?>) command;
            runVmCommandsMap.put(runVMCommandBase.getVmId(), runVMCommandBase);
        }

        List<Guid> guids = new ArrayList<Guid>();
        guids.addAll(runVmCommandsMap.keySet());
        List<Guid> orderedGuids = DbFacade.getInstance().getVmStaticDao().getOrderedVmGuidsForRunMultipleActions(guids);

        commandsList.clear();

        for (Guid guid : orderedGuids) {
            commandsList.add(runVmCommandsMap.get(guid));
        }

    }

}
