package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.VmCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.action.RestoreAllCinderSnapshotsParameters;
import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class RestoreAllCinderSnapshotsCommand<T extends RestoreAllCinderSnapshotsParameters> extends VmCommand<T> {

    public RestoreAllCinderSnapshotsCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeVmCommand() {
        for (CinderDisk cinderDisk : getParameters().getCinderDisks()) {
            ImagesContainterParametersBase params = getRestoreFromSnapshotParams(cinderDisk);
            restoreCinderDisk(cinderDisk, params);

            // In case we want to undo the previewed snapshot.
            if (getParameters().getSnapshot().getType() != Snapshot.SnapshotType.REGULAR) {
                cinderDisk.setActive(true);
                getImageDao().update(cinderDisk.getImage());
            }
        }
        setSucceeded(true);
    }

    private ImagesContainterParametersBase getRestoreFromSnapshotParams(CinderDisk cinderDisk) {
        ImagesContainterParametersBase params =
                new RestoreFromSnapshotParameters(cinderDisk.getImageId(),
                        getParameters().getVmId(),
                        getParameters().getSnapshot(),
                        getParameters().getRemovedSnapshotId());
        params.setParentCommand(getActionType());
        params.setStorageDomainId(cinderDisk.getStorageIds().get(0));
        params.setParentParameters(getParameters());
        return params;
    }

    private VdcReturnValueBase restoreCinderDisk(CinderDisk cinderDisk, ImagesContainterParametersBase params) {
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RestoreFromCinderSnapshot,
                params,
                cloneContextAndDetachFromParent(),
                new SubjectEntity(VdcObjectType.Storage, cinderDisk.getStorageIds().get(0)));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            log.error("Error restoring snapshot");
        }
        return null;
    }

    @Override
    protected void endSuccessfully() {
        if (!getParameters().isParentHasTasks()) {
            getBackend().endAction(getParameters().getParentCommand(),
                    getParameters().getParentParameters(),
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
    }

    @Override
    protected void endWithFailure() {
        if (!getParameters().isParentHasTasks()) {
            getParameters().getParentParameters().setTaskGroupSuccess(false);
            getBackend().endAction(getParameters().getParentCommand(),
                    getParameters().getParentParameters(),
                    cloneContextAndDetachFromParent());
        }
        setSucceeded(true);
    }

    @Override
    public CommandCallback getCallback() {
        return new RemoveAllCinderDisksCommandCallBack<>();
    }
}
