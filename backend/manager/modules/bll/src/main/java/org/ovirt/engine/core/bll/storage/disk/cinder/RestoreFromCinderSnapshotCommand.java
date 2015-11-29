package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.RemoveCinderDiskParameters;
import org.ovirt.engine.core.common.action.RestoreFromSnapshotParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.Snapshot.SnapshotType;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;

@InternalCommandAttribute
public class RestoreFromCinderSnapshotCommand<T extends RestoreFromSnapshotParameters> extends BaseImagesCommand<T> {

    public RestoreFromCinderSnapshotCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    @Override
    protected void executeCommand() {
        setStorageDomainId(getParameters().getStorageDomainId());
        if (isRemoveEntireDisk()) {
            removeEntireDisk();
        } else {
            removeCinderVolume();
        }
        setSucceeded(true);
    }

    private void removeEntireDisk() {
        CinderDisk cinderDisk = (CinderDisk) getDiskImageDao().getSnapshotById(getParameters().getImageId());
        removeCinderDisk(cinderDisk);
    }

    private boolean isRemoveEntireDisk() {
        return (getParameters().getRemovedSnapshotId() == null);
    }

    private void removeCinderVolume() {
        CinderDisk cinderSnapshot = getRelevantCinderDisk();
        switch (getParameters().getSnapshot().getType()) {
        case REGULAR:
            List<DiskImage> cinderSnapshots = getDiskImageDao().getAllSnapshotsForParent(getParameters().getImageId());
            // Find snapshots which rely on the snapshot and need to be removed.
            for (DiskImage descendantSnapshot : cinderSnapshots) {
                removeDecendedSnapshots(cinderSnapshot, descendantSnapshot);
            }
            break;
        case PREVIEW:
        case STATELESS:
            deletePreviewedVolume(cinderSnapshot);
            break;
        }
    }

    private void deletePreviewedVolume(CinderDisk cinderSnapshot) {
        getParameters().setDestinationImageId(cinderSnapshot.getImageId());
        getParameters().setCinderDiskToBeRemoved(cinderSnapshot);
        getParameters().setRemoveParent(false);
        getCinderBroker().deleteVolumeByClassificationType(cinderSnapshot);
    }

    private void removeDecendedSnapshots(CinderDisk cinderSnapshot, DiskImage descendantSnapshot) {
        Snapshot snap = getSnapshotDao().get(descendantSnapshot.getVmSnapshotId());
        if (snap.getType() == SnapshotType.REGULAR) {
            CinderDisk lastParentToRemove =
                    (CinderDisk) ImagesHandler.getSnapshotLeaf(descendantSnapshot.getImageId());
            if (lastParentToRemove != null) {
                getParameters().setCinderDiskToBeRemoved(lastParentToRemove);
                getParameters().setDestinationImageId(lastParentToRemove.getImageId());
                getCinderBroker().deleteVolumeByClassificationType(lastParentToRemove);
                if (!cinderSnapshot.getImageId().equals(lastParentToRemove.getImageId())) {
                    getParameters().setRemoveParent(true);
                }
            }
        }
    }

    private CinderDisk getRelevantCinderDisk() {
        Guid imageToRemoveId = findImageForSameDrive(getParameters().getRemovedSnapshotId());
        return (CinderDisk) getDiskImageDao().getSnapshotById(imageToRemoveId);
    }

    private VdcReturnValueBase removeCinderDisk(CinderDisk cinderDisk) {
        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RemoveCinderDisk,
                buildRemoveDiskChildCommandParameters(cinderDisk),
                cloneContextAndDetachFromParent(),
                new SubjectEntity(VdcObjectType.Storage, cinderDisk.getStorageIds().get(0)));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error removing Cinder disk '{}': {}",
                    getDiskImage().getDiskAlias(),
                    e.getMessage());
            log.debug("Exception", e);
            e.printStackTrace();
        }
        return null;
    }

    private RemoveCinderDiskParameters buildRemoveDiskChildCommandParameters(CinderDisk cinderDisk) {
        RemoveCinderDiskParameters removeDiskParams = new RemoveCinderDiskParameters(cinderDisk.getId());
        removeDiskParams.setShouldBeLogged(false);
        removeDiskParams.setParentCommand(getActionType());
        removeDiskParams.setParentParameters(getParameters());
        return removeDiskParams;
    }

    protected void endSuccessfully() {
        if (!isRemoveEntireDisk()) {
            endRemoveCinderDisk();
        }
    }

    @Override
    protected void endWithFailure() {
        if (!isRemoveEntireDisk()) {
            DiskImage lastParentToRemove = ImagesHandler.getSnapshotLeaf(getParameters().getImageId());
            log.error("Could not remove volume id {} from Cinder which is related to disk {}",
                    lastParentToRemove.getDiskAlias(),
                    lastParentToRemove.getImageId());
            endRemoveCinderDisk();
        }
    }

    private void endRemoveCinderDisk() {
        // In case we didn't set CinderDiskToBeRemoved (Since there were no relied snapshots on the volume), don't
        // remove the snapshot.
        if (getParameters().getCinderDiskToBeRemoved() != null) {
            removeSnapshot(getParameters().getCinderDiskToBeRemoved());
        }
        if (getParameters().isRemoveParent()) {
            RestoreFromSnapshotParameters removeParams =
                    new RestoreFromSnapshotParameters(getParameters().getImageId(),
                            getParameters().getContainerId(),
                            getParameters().getSnapshot(),
                            getParameters().getRemovedSnapshotId());
            removeParams.setStorageDomainId(getStorageDomainId());
            Future<VdcReturnValueBase> future =
                    CommandCoordinatorUtil.executeAsyncCommand(VdcActionType.RestoreFromCinderSnapshot,
                            removeParams,
                            cloneContext(),
                            new SubjectEntity[0]);
            try {
                setReturnValue(future.get());
                setSucceeded(getReturnValue().getSucceeded());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error removing Cinder volume '{}': {}",
                        getDiskImage().getDiskAlias(),
                        e.getMessage());
                log.debug("Exception", e);
            }
        }
    }

    @Override
    public CommandCallback getCallback() {
        return new RestoreFromCinderSnapshotCommandCallback();
    }
}
