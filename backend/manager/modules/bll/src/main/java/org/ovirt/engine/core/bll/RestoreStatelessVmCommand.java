package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.List;

import org.ovirt.engine.core.common.action.RestoreAllSnapshotsParameters;
import org.ovirt.engine.core.common.action.UpdateVmVersionParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.action.VmOperationParameterBase;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.SnapshotActionEnum;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.SnapshotDao;

@InternalCommandAttribute
public class RestoreStatelessVmCommand<T extends VmOperationParameterBase> extends VmCommand<T> {

    /**
     * Constructor for command creation when compensation is applied on startup
     *
     * @param commandId
     */
    protected RestoreStatelessVmCommand(Guid commandId) {
        super(commandId);
    }

    public RestoreStatelessVmCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        VdcReturnValueBase result =
                runInternalActionWithTasksContext(
                        VdcActionType.UpdateVmVersion,
                        new UpdateVmVersionParameters(getVmId()),
                        getLock()
                );

        // if it fail because of canDoAction, its safe to restore the snapshot
        // and the vm will still be usable with previous version
        if (!result.getSucceeded() && !result.getCanDoAction()) {
            log.warnFormat("Couldn't update VM {0} ({1}) version from it's template, continue with restoring stateless snapshot.",
                    getVm().getName(),
                    getVmId());

            setSucceeded(restoreInitialState());
        }
        else {
            setSucceeded(result.getSucceeded());
        }
    }

    private boolean restoreInitialState() {
        Guid snapshotId = getSnapshotDao().getId(getVmId(), SnapshotType.STATELESS);
        if (snapshotId == null) {
            return true;
        }

        List<DiskImage> imagesList = getDiskImageDao().getAllSnapshotsForVmSnapshot(snapshotId);
        if (imagesList == null || imagesList.isEmpty()) {
            return true;
        }

        // restore all snapshots
        return runInternalActionWithTasksContext(VdcActionType.RestoreAllSnapshots,
                        buildRestoreAllSnapshotsParameters(imagesList),
                getLock()).getSucceeded();
    }

    private RestoreAllSnapshotsParameters buildRestoreAllSnapshotsParameters(List<DiskImage> imagesList) {
        RestoreAllSnapshotsParameters restoreParameters = new RestoreAllSnapshotsParameters(getVm().getId(), SnapshotActionEnum.RESTORE_STATELESS);
        restoreParameters.setShouldBeLogged(false);
        restoreParameters.setImages(imagesList);
        return restoreParameters;
    }

    private SnapshotDao getSnapshotDao() {
        return DbFacade.getInstance().getSnapshotDao();
    }
}
