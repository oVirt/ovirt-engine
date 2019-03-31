package org.ovirt.engine.core.bll.storage.disk.managedblock;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.CommandBase;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.ImagesHandler;
import org.ovirt.engine.core.bll.storage.disk.managedblock.util.ManagedBlockStorageDiskUtil;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.action.CreateManagedBlockStorageDiskSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibCommandParameters;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibExecutor;
import org.ovirt.engine.core.common.utils.cinderlib.CinderlibReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.CinderStorageDao;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class TryBackToManagedBlockSnapshotCommand<T extends CreateManagedBlockStorageDiskSnapshotParameters>
        extends CommandBase<T> {
    @Inject
    private CinderlibExecutor cinderlibExecutor;

    @Inject
    private CinderStorageDao cinderStorageDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private DiskDao diskDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private ManagedBlockStorageDiskUtil managedBlockStorageDiskUtil;

    @Inject
    private ImagesHandler imagesHandler;

    public TryBackToManagedBlockSnapshotCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public TryBackToManagedBlockSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = cinderStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getVolumeId().toString());
        extraParams.add(getParameters().getImageId().toString());
        CinderlibReturnValue returnValue;

        try {
            CinderlibCommandParameters params =
                    new CinderlibCommandParameters(JsonHelper.mapToJson(
                            managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());
            returnValue =
                    cinderlibExecutor.runCommand(CinderlibExecutor.CinderlibCommand.CREATE_VOLUME_FROM_SNAPSHOT,
                            params);
            if (!returnValue.getSucceed()) {
                return;
            }

            Guid clonedVolumeGuid = Guid.createGuidFromString(returnValue.getOutput());
            getParameters().setDestinationImageId(clonedVolumeGuid);
        } catch (Exception e) {
            log.error("Failed executing creating volume from snapshot", e);
            return;
        }

        createVolumeFromSnapshotInDB();
        setSucceeded(true);
        persistCommandIfNeeded();
    }

    private void createVolumeFromSnapshotInDB() {
        TransactionSupport.executeInNewTransaction(() -> {
            ManagedBlockStorageDisk newVolume =
                    initNewVolume(getParameters().getDestinationImageId());
            managedBlockStorageDiskUtil.updateOldImageAsActive(Snapshot.SnapshotType.PREVIEW, false, newVolume);
            imageDao.save(newVolume.getImage());
            managedBlockStorageDiskUtil.saveDisk(newVolume);
            return null;
        });
    }

    private ManagedBlockStorageDisk initNewVolume(Guid volumeId) {
        ManagedBlockStorageDisk currentActiveVolume =
                (ManagedBlockStorageDisk) diskDao.get(getParameters().getVolumeId());

        currentActiveVolume.setVolumeFormat(VolumeFormat.RAW);
        currentActiveVolume.setImageStatus(ImageStatus.LOCKED);
        currentActiveVolume.setVolumeClassification(VolumeClassification.Volume);
        currentActiveVolume.setCreationDate(new Date());
        currentActiveVolume.setVmSnapshotId(getParameters().getVmSnapshotId());
        currentActiveVolume.setLastModifiedDate(new Date());
        currentActiveVolume.setDiskProfileId(getParameters().getDiskProfileId());
        currentActiveVolume.setImageId(volumeId);
        currentActiveVolume.setParentId(getParameters().getImageId());
        currentActiveVolume.setActive(true);

        return currentActiveVolume;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    protected void endSuccessfully() {
        imagesHandler.updateImageStatus(getParameters().getDestinationImageId(), ImageStatus.OK);
        setSucceeded(true);
    }
}
