package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.PermissionSubject;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;

@InternalCommandAttribute
public class MigrateIrsSnapshotsToVdcCommand<T extends VdcActionParametersBase> extends CommandBase<T> {
    public MigrateIrsSnapshotsToVdcCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        // IrsSnapshotsToVdcMigrator.Migrate();
    }

    // TODO this command should be removed - AI Ofrenkel
    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyList();
    }
}
