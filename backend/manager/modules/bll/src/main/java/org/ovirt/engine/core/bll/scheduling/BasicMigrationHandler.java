package org.ovirt.engine.core.bll.scheduling;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.job.ExecutionHandler;
import org.ovirt.engine.core.common.action.MigrateVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.compat.Guid;

public class BasicMigrationHandler implements MigrationHandler {

    @Inject
    private BackendInternal backendInternal;

    @Override
    public void migrateVM(List<Guid> initialHosts, Guid vmToMigrate) {
        MigrateVmParameters parameters = new MigrateVmParameters(false, vmToMigrate);
        parameters.setInitialHosts(new ArrayList<>(initialHosts));
        backendInternal.runInternalAction(VdcActionType.MigrateVm,
                parameters,
                ExecutionHandler.createInternalJobContext());
    }

}
