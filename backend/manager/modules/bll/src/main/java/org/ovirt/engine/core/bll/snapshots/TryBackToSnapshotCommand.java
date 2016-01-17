package org.ovirt.engine.core.bll.snapshots;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;

/**
 * This command responsible to creating new snapshot from non leaf snapshot. Its
 * makes currently active snapshot to be inactive and makes new created snapshot
 * active.
 *
 * Parameters: Guid snapshotId - id of source snapshot Guid containerId - id of
 * container VM string drive - mapping of new snapshot in Vm
 */
@InternalCommandAttribute
public class TryBackToSnapshotCommand<T extends ImagesContainterParametersBase> extends CreateSnapshotCommand<T> {
    public TryBackToSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        super.setVmId(parameters.getContainerId());
    }

    /**
     * Remove old image vm map.
     */
    @Override
    protected void processOldImageFromDb() {
        updateOldImageAsActive(SnapshotType.PREVIEW, false);
    }

    @Override
    protected void executeCommand() {
        super.executeCommand();
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        updateOldImageAsActive(SnapshotType.ACTIVE, true);

        // Remove destination, unlock source:
        undoActionOnSourceAndDestination();

        setSucceeded(true);
    }
}
