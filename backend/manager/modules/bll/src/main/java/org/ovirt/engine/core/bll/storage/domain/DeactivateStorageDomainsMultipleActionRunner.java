package org.ovirt.engine.core.bll.storage.domain;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.SortedMultipleActionsRunnerBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;

public class DeactivateStorageDomainsMultipleActionRunner extends SortedMultipleActionsRunnerBase {
    public DeactivateStorageDomainsMultipleActionRunner(ActionType actionType,
            List<VdcActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected void sortCommands() {
        Collections.sort(getCommands(), Collections.reverseOrder(new StorageDomainsByTypeComparer()));
    }
}
