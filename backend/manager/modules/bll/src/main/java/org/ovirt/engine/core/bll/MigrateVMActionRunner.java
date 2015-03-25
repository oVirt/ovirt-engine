package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.comparators.VmsComparer;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class MigrateVMActionRunner extends SortedMultipleActionsRunnerBase {

    public MigrateVMActionRunner(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters,
            CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected void sortCommands() {
        ArrayList<CommandBase<?>> commands = getCommands();
        final Map<Guid, VM> vms = new HashMap<>(commands.size());
        for (CommandBase<?> cmd : commands) {
            vms.put(cmd.getVmId(), DbFacade.getInstance().getVmDao().get(cmd.getVmId()));
        }

        Collections.sort(commands, Collections.reverseOrder(new Comparator<CommandBase<?>>() {

            private final VmsComparer vmComparator = new VmsComparer();

            @Override
            public int compare(CommandBase<?> o1, CommandBase<?> o2) {
                VM vm1 = vms.get(o1.getVmId());
                VM vm2 = vms.get(o2.getVmId());

                return vmComparator.compare(vm1, vm2);
            }

        }));
    }
}
