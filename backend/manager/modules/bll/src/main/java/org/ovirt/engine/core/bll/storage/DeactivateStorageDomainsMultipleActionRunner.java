package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import org.ovirt.engine.core.bll.*;
import org.ovirt.engine.core.common.action.*;

public class DeactivateStorageDomainsMultipleActionRunner extends SortedMultipleActionsRunnerBase {
    public DeactivateStorageDomainsMultipleActionRunner(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters, boolean isInternal) {
        super(actionType, parameters, isInternal);
    }

    @Override
    protected void SortCommands() {
        Collections.sort(getCommands(), Collections.reverseOrder(new StorageDomainsByTypeComparer()));
    }
}
