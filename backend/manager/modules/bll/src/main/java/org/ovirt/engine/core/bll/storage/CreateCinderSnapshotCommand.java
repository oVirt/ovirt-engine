package org.ovirt.engine.core.bll.storage;

import java.util.Date;

import org.ovirt.engine.core.bll.BaseImagesCommand;
import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.CommandCoordinatorUtil;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute
public class CreateCinderSnapshotCommand<T extends ImagesContainterParametersBase> extends BaseImagesCommand<T> {

    private CinderDisk disk;

    public CreateCinderSnapshotCommand(T parameters) {
        this(parameters, null);
    }

    public CreateCinderSnapshotCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    private CinderDisk cloneDiskForSnapshot() {
        CinderDisk cinderDependentVolume = getDisk();
        initCinderDependentVolume(cinderDependentVolume);
        String snapshotId = getCinderBroker().createSnapshot(cinderDependentVolume, getParameters().getDescription());
        Guid destinationImageId = Guid.createGuidFromString(snapshotId);
        getParameters().setDestinationImageId(destinationImageId);
        cinderDependentVolume.setImageId(destinationImageId);
        return cinderDependentVolume;
    }

    private void initCinderDependentVolume(CinderDisk newCinderVolume) {
        // override volume type and volume format to Unassigned and unassigned for Cinder.
        newCinderVolume.setVolumeType(VolumeType.Unassigned);
        newCinderVolume.setvolumeFormat(VolumeFormat.Unassigned);
        newCinderVolume.setImageStatus(ImageStatus.LOCKED);
        newCinderVolume.setCreationDate(new Date());
        newCinderVolume.setLastModifiedDate(new Date());
        newCinderVolume.setQuotaId(getParameters().getQuotaId());
        newCinderVolume.setDiskProfileId(getParameters().getDiskProfileId());
        newCinderVolume.setActive(Boolean.FALSE);
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
        getImageDao().update(getDiskImage().getImage());
        getCompensationContext().stateChanged();
    }

    @Override
    protected void executeCommand() {
        final CinderDisk newCinderVolume = cloneDiskForSnapshot();
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                processOldImageFromDb();
                newCinderVolume.setVolumeClassification(VolumeClassification.Snapshot);
                addDiskImageToDb(newCinderVolume, getCompensationContext(), Boolean.FALSE);
                setActionReturnValue(newCinderVolume);
                setSucceeded(true);
                return null;
            }
        });

        getReturnValue().setActionReturnValue(newCinderVolume.getImageId());
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(true);
        return;
    }

    @Override
    protected void endSuccessfully() {
        if (!getParameters().isLeaveLocked()) {
            getDestinationDiskImage().setImageStatus(ImageStatus.OK);
            getImageDao().update(getDestinationDiskImage().getImage());
        }
        if (!getParameters().isParentHasTasks() && CommandCoordinatorUtil.getChildCommandIds(
                getParentParameters(getParameters().getParentCommand()).getCommandId()).size() == 1) {
            getBackend().endAction(getParameters().getParentCommand(),
                    getParameters().getParentParameters(),
                    getContext().clone().withoutCompensationContext().withoutLock());
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        // TODO: Add revert tasks for Cinder
        if (isDestinationImageExists(getDestinationDiskImage().getId()) &&
                (isImageSnapshot(getDestinationDiskImage()))) {
            updateLastModifiedInParent(getDestinationDiskImage().getParentId());
        }
        super.endWithFailure();
        if (!getParameters().isParentHasTasks()) {
            getParameters().getParentParameters().setTaskGroupSuccess(false);
            getBackend().endAction(getParameters().getParentCommand(),
                    getParameters().getParentParameters(),
                    getContext().clone().withoutCompensationContext().withoutLock());
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
