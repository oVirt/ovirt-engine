package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.compat.Guid;

public class BasicMigrationHandler implements MigrationHandler {

    @Inject
    private BackendInternal backendInternal;

    @Override
    public boolean migrateVM(List<Guid> initialHosts, Guid vmToMigrate, String reason) {
        MigrateVmParameters parameters = new MigrateVmParameters(false, vmToMigrate);
        parameters.setInitialHosts(new ArrayList<>(initialHosts));
        parameters.setReason(reason);
        ActionReturnValue res = backendInternal.runInternalAction(ActionType.BalanceVm,
                parameters,
                ExecutionHandler.createInternalJobContext());

        return res.getSucceeded();
    }

}
