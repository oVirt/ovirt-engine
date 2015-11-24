package org.ovirt.engine.core.bll.storage.domain;

import java.util.ArrayList;
import java.util.Collections;

import org.ovirt.engine.core.bll.SortedMultipleActionsRunnerBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

public class DeactivateStorageDomainsMultipleActionRunner extends SortedMultipleActionsRunnerBase {
    public DeactivateStorageDomainsMultipleActionRunner(VdcActionType actionType,
            ArrayList<VdcActionParametersBase> parameters, CommandContext commandContext, boolean isInternal) {
        super(actionType, parameters, commandContext, isInternal);
    }

    @Override
    protected void sortCommands() {
        Collections.sort(getCommands(), Collections.reverseOrder(new StorageDomainsByTypeComparer()));
    }
}
