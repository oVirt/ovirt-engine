package org.ovirt.engine.core.bll.storage;

import java.util.Date;

import org.ovirt.engine.core.bll.ImagesHandler;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class TryBackToCinderSnapshotCommand<T extends CreateCinderSnapshotParameters> extends CreateCinderSnapshotCommand<T> {

    private CinderBroker cinderBroker;
    private CinderDisk oldActiveDisk;

    public TryBackToCinderSnapshotCommand(T parameters) {
        this(parameters, null);
    }

    public TryBackToCinderSnapshotCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    /**
     * Remove old image vm map.
     */
    @Override
    protected void processOldImageFromDb() {
        updateOldImageAsActive(Snapshot.SnapshotType.PREVIEW, false);
    }

    @Override
    protected void executeCommand() {
        final CinderDisk newCinderVolume = createVolumeFromSnapshotInCinder();
        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                processOldImageFromDb();
                addDiskImageToDb(newCinderVolume, getCompensationContext(), Boolean.TRUE);
                return null;
            }
        });

        getParameters().setDestinationImageId(newCinderVolume.getImageId());
        getParameters().setImageId(newCinderVolume.getImageId());
        getReturnValue().setActionReturnValue(newCinderVolume);
        persistCommand(getParameters().getParentCommand(), true);
        setSucceeded(true);
        return;
    }

    private CinderDisk createVolumeFromSnapshotInCinder() {
        Guid newVolumeId = cinderCloneDiskFromSnapshot();
        CinderDisk clonedDiskFromSnapshot = initializeNewCinderVolumeDisk(newVolumeId);

        // Setting this for the callback from coco.
        getParameters().setDestinationImageId(newVolumeId);
        return clonedDiskFromSnapshot;
    }

    private CinderDisk initializeNewCinderVolumeDisk(Guid newVolumeId) {
        CinderDisk clonedDiskFromSnapshot = (CinderDisk) getDiskDao().get(getParameters().getContainerId());

        // override volume type and volume format to Unassigned and unassigned for Cinder.
        clonedDiskFromSnapshot.setVolumeType(VolumeType.Unassigned);
        clonedDiskFromSnapshot.setvolumeFormat(VolumeFormat.Unassigned);
        clonedDiskFromSnapshot.setImageStatus(ImageStatus.LOCKED);
        clonedDiskFromSnapshot.setCreationDate(new Date());
        clonedDiskFromSnapshot.setVmSnapshotId(getParameters().getVmSnapshotId());
        clonedDiskFromSnapshot.setLastModifiedDate(new Date());
        clonedDiskFromSnapshot.setQuotaId(getParameters().getQuotaId());
        clonedDiskFromSnapshot.setDiskProfileId(getParameters().getDiskProfileId());
        clonedDiskFromSnapshot.setImageId(newVolumeId);
        clonedDiskFromSnapshot.setParentId(getImageId());
        return clonedDiskFromSnapshot;
    }

    private Guid cinderCloneDiskFromSnapshot() {
        CinderDisk activeCinderVolume = getOldActiveDisk();
        String newVolumeId = getCinderBroker().cloneVolumeFromSnapshot(activeCinderVolume, getImageId());
        return Guid.createGuidFromString(newVolumeId);
    }

    protected CinderDisk getOldActiveDisk() {
        if (oldActiveDisk == null) {
            oldActiveDisk = (CinderDisk) getDiskDao().get(getParameters().getContainerId());
        }
        return oldActiveDisk;
    }

    @Override
    public Guid getStorageDomainId() {
        return getOldActiveDisk().getStorageIds().get(0);
    }

    @Override
    protected void lockImage() {
        ImagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.LOCKED);
    }

    @Override
    public CommandCallback getCallback() {
        return new CloneSingleCinderDiskCommandCallback();
    }

    @Override
    protected void endSuccessfully() {
        if (!getParameters().isLeaveLocked()) {
            getDestinationDiskImage().setImageStatus(ImageStatus.OK);
            getImageDao().update(getDestinationDiskImage().getImage());
        }
        if (!getParameters().isParentHasTasks()) {
            getBackend().endAction(getParameters().getParentCommand(),
                    getParameters().getParentParameters(),
                    getContext().clone().withoutCompensationContext().withoutExecutionContext().withoutLock());
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        updateOldImageAsActive(Snapshot.SnapshotType.ACTIVE, true);

        // Remove destination, unlock source:
        undoActionOnSourceAndDestination();

        setSucceeded(true);
    }

    public CinderBroker getCinderBroker() {
        if (cinderBroker == null) {
            cinderBroker = new CinderBroker(getStorageDomainId(), getReturnValue().getExecuteFailedMessages());
        }
        return cinderBroker;
    }
}
