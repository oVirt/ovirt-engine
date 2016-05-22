package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.BaseImagesCommand;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.action.RemoveCinderDiskVolumeParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateCinderSnapshotCommand<T extends CreateCinderSnapshotParameters> extends BaseImagesCommand<T> {

    private CinderDisk disk;

    public CreateCinderSnapshotCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    private CinderDisk cloneDisk() {
        boolean isStateless = isStatelessSnapshot();
        CinderDisk cinderDependentVolume = getDisk();
        initCinderDependentVolume(cinderDependentVolume);
        cinderDependentVolume.setActive(isStateless);
        String volumeId = isStateless ?
                getCinderBroker().cloneDisk(cinderDependentVolume) :
                getCinderBroker().createSnapshot(cinderDependentVolume, getParameters().getDescription());
        cinderDependentVolume.setVolumeClassification(isStateless ?
                VolumeClassification.Volume : VolumeClassification.Snapshot);
        Guid destinationImageId = Guid.createGuidFromString(volumeId);
        getParameters().setDestinationImageId(destinationImageId);
        cinderDependentVolume.setImageId(destinationImageId);
        return cinderDependentVolume;
    }

    private void initCinderDependentVolume(CinderDisk newCinderVolume) {
        // override volume type and volume format to Unassigned and unassigned for Cinder.
        newCinderVolume.setVolumeType(VolumeType.Unassigned);
        newCinderVolume.setVolumeFormat(VolumeFormat.Unassigned);
        newCinderVolume.setImageStatus(ImageStatus.LOCKED);
        newCinderVolume.setCreationDate(new Date());
        newCinderVolume.setLastModifiedDate(new Date());
        newCinderVolume.setQuotaId(getParameters().getQuotaId());
        newCinderVolume.setDiskProfileId(getParameters().getDiskProfileId());
        newCinderVolume.setQuotaId(getParameters().getQuotaId());

        // Get the last snapshot to be the parent of the new volume.
        DiskImage leaf = ImagesHandler.getSnapshotLeaf(getDiskImage().getId());
        newCinderVolume.setParentId(leaf.getImageId());
    }

    /**
     * By default old image must be replaced by new one
     */
    protected void processOldImageFromDb() {
        getCompensationContext().snapshotEntity(getDiskImage().getImage());
        getParameters().setOldLastModifiedValue(getDiskImage().getLastModified());
        getDiskImage().setLastModified(new Date());
        getDiskImage().setVmSnapshotId(getParameters().getVmSnapshotId());
        if (isStatelessSnapshot()) {
            getDiskImage().setActive(Boolean.FALSE);
        }
        getImageDao().update(getDiskImage().getImage());
        getCompensationContext().stateChanged();
    }

    @Override
    protected void executeCommand() {
        final CinderDisk newCinderVolume;
        newCinderVolume = cloneDisk();
        if (!isStatelessSnapshot()) {
            getDiskImage().setVmSnapshotId(getParameters().getVmSnapshotId());
        }
        TransactionSupport.executeInNewTransaction(() -> {
            processOldImageFromDb();
            addDiskImageToDb(newCinderVolume, getCompensationContext(), isStatelessSnapshot());
            setActionReturnValue(newCinderVolume);
            setSucceeded(true);
            return null;
        });

        getReturnValue().setActionReturnValue(newCinderVolume.getImageId());
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(true);
    }

    private boolean isStatelessSnapshot() {
        return getParameters().getSnapshotType() == Snapshot.SnapshotType.STATELESS;
    }

    @Override
    protected void endSuccessfully() {
        if (!getParameters().isLeaveLocked()) {
            getDestinationDiskImage().setImageStatus(ImageStatus.OK);
            getImageDao().update(getDestinationDiskImage().getImage());
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        revertCinderVolume((CinderDisk) getDestinationDiskImage());
        if (isDestinationImageExists(getDestinationDiskImage().getId()) &&
                isImageSnapshot(getDestinationDiskImage())) {
            updateLastModifiedInParent(getDestinationDiskImage().getParentId());
        }
        super.endWithFailure();
        if (getParameters().getSnapshotType().equals(Snapshot.SnapshotType.STATELESS)) {
            updateOldImageAsActive(Snapshot.SnapshotType.ACTIVE, true);
        }
    }

    private void revertCinderVolume(CinderDisk diskVolumeVolume) {
        RemoveCinderDiskVolumeParameters removeDiskVolumeParam =
                new RemoveCinderDiskVolumeParameters(diskVolumeVolume);

        Future<VdcReturnValueBase> future = CommandCoordinatorUtil.executeAsyncCommand(
                VdcActionType.RemoveCinderDiskVolume,
                removeDiskVolumeParam,
                null);
        try {
            future.get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Fail to revert snapshot id '{}' for disk id '{}'. Exception: {}",
                    diskVolumeVolume.getImageId(),
                    diskVolumeVolume.getId(),
                    e);
        }
    }

    protected CinderDisk getDisk() {
        if (disk == null) {
            disk = (CinderDisk) getDiskImageDao().get(getImageId());
        }
        return disk;
    }

    @Override
    public Guid getStorageDomainId() {
        return getParameters().getStorageDomainId();
    }

    @Override
    protected void lockImage() {
        ImagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.LOCKED);
    }

    private void updateLastModifiedInParent(Guid parentId) {
        DiskImage previousSnapshot = getDiskImageDao().getSnapshotById(parentId);

        // If the old description of the snapshot got overriden, we should restore the previous description
        if (getParameters().getOldLastModifiedValue() != null) {
            previousSnapshot.setLastModified(getParameters().getOldLastModifiedValue());
        }
        getImageDao().update(previousSnapshot.getImage());
    }

    private boolean isImageSnapshot(DiskImage destinationImage) {
        // Empty Guid, means new disk rather than snapshot, so no need to add a map to the db for new disk.
        return !destinationImage.getParentId().equals(Guid.Empty)
                && !destinationImage.getParentId().equals(destinationImage.getImageTemplateId());
    }

    private boolean isDestinationImageExists(Guid destinationId) {
        return getDestinationDiskImage() != null
                && !getVmDao().getVmsListForDisk(destinationId, false).isEmpty();
    }

    @Override
    public CommandCallback getCallback() {
        return new CreateCinderSnapshotCommandCallback();
    }

}
