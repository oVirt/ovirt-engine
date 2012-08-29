package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.storage.AttachStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.bll.storage.DeactivateStorageDomainsMultipleActionRunner;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;

public final class MultipleActionsRunnersFactory {
    public static MultipleActionsRunner CreateMultipleActionsRunner(VdcActionType actionType,
            java.util.ArrayList<VdcActionParametersBase> parameters,
            boolean isInternal) {
        MultipleActionsRunner runner;
        switch (actionType) {
        case DeactivateStorageDomain: {
            runner = new DeactivateStorageDomainsMultipleActionRunner(actionType, parameters, isInternal);
            break;
        }
        case AttachStorageDomainToPool: {
            runner = new AttachStorageDomainsMultipleActionRunner(actionType, parameters, isInternal);
            break;
        }

        case RunVm: {
            runner = new RunVMActionRunner(actionType, parameters, isInternal);
            break;
        }
        case MigrateVm: {
            runner = new MigrateVMActionRunner(actionType, parameters, isInternal);
            break;
        }
        case RemoveVmFromPool: {
            runner = new RemoveVmFromPoolRunner(actionType, parameters, isInternal);
            break;
        }
        default: {
            runner = new MultipleActionsRunner(actionType, parameters, isInternal);
            break;
        }
        }
        return runner;
    }
}
