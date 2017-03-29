package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateCinderSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.SubjectEntity;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.di.Injector;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
public class TryBackToCinderSnapshotCommand<T extends CreateCinderSnapshotParameters> extends CreateCinderSnapshotCommand<T> {

    @Inject
    private DiskDao diskDao;
    @Inject
    private ImageDao imageDao;

    private CinderDisk oldActiveDisk;

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
        TransactionSupport.executeInNewTransaction(() -> {
            processOldImageFromDb();
            addDiskImageToDb(newCinderVolume, getCompensationContext(), Boolean.TRUE);
            return null;
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
        CinderDisk clonedDiskFromSnapshot = (CinderDisk) diskDao.get(getParameters().getContainerId());

        // override volume type and volume format to Unassigned and unassigned for Cinder.
        clonedDiskFromSnapshot.setVolumeType(VolumeType.Unassigned);
        clonedDiskFromSnapshot.setVolumeFormat(VolumeFormat.Unassigned);
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
            oldActiveDisk = (CinderDisk) diskDao.get(getParameters().getContainerId());
        }
        return oldActiveDisk;
    }

    @Override
    public Guid getStorageDomainId() {
        return getOldActiveDisk().getStorageIds().get(0);
    }

    @Override
    protected void lockImage() {
        imagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.LOCKED);
    }

    @Override
    public CommandCallback getCallback() {
        return Injector.injectMembers(new CloneSingleCinderDiskCommandCallback());
    }

    @Override
    protected void endSuccessfully() {
        imagesHandler.updateImageStatus(getParameters().getDestinationImageId(), ImageStatus.OK);
        imagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.OK);
        if (!getParameters().isLeaveLocked()) {
            getDestinationDiskImage().setImageStatus(ImageStatus.OK);
            imageDao.update(getDestinationDiskImage().getImage());
        }
        setSucceeded(true);
    }

    @Override
    protected void endWithFailure() {
        imagesHandler.updateImageStatus(getParameters().getDestinationImageId(), ImageStatus.ILLEGAL);
        imagesHandler.updateImageStatus(getParameters().getImageId(), ImageStatus.OK);
        updateOldImageAsActive(Snapshot.SnapshotType.ACTIVE, true);

        // Remove destination, unlock source:
        undoActionOnSourceAndDestination();

        setSucceeded(true);
    }

    @Override
    protected Collection<SubjectEntity> getSubjectEntities() {
        return Collections.singleton(new SubjectEntity(VdcObjectType.Storage,
                getStorageDomainId()));
    }
}
