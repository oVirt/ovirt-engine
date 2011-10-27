package org.ovirt.engine.core.bll;

import java.util.Collections;
import java.util.Map;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.compat.Guid;

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
    public Map<Guid, VdcObjectType> getPermissionCheckSubjects() {
        // Not needed for admin operations.
        return Collections.emptyMap();
    }

}
