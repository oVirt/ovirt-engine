package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VmDao;

public class MigrateVMActionRunner extends SortedMultipleActionsRunnerBase {

    @Inject
    private VmDao vmDao;

    public MigrateVMActionRunner(ActionType actionType,
            List<ActionParametersBase> parameters,
            CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected void sortCommands() {
        List<CommandBase<?>> commands = getCommands();
        final Map<Guid, VM> vms = new HashMap<>(commands.size());
        for (CommandBase<?> cmd : commands) {
            vms.put(cmd.getVmId(), vmDao.get(cmd.getVmId()));
        }

        Collections.sort(commands,
                Comparator.comparing((CommandBase<?> c) -> vms.get(c.getVmId()), new VmsComparer()).reversed());
    }
}
