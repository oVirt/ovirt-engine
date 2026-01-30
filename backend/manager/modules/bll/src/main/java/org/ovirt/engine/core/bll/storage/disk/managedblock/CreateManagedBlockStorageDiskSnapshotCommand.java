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
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateManagedBlockStorageDiskSnapshotParameters;
import org.ovirt.engine.core.common.action.ImagesContainterParametersBase;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.Image;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorage;
import org.ovirt.engine.core.common.businessentities.storage.ManagedBlockStorageDisk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockCommandParameters;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockExecutor;
import org.ovirt.engine.core.common.utils.managedblock.ManagedBlockReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ManagedBlockStorageDao;
import org.ovirt.engine.core.utils.JsonHelper;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@NonTransactiveCommandAttribute
public class CreateManagedBlockStorageDiskSnapshotCommand<T extends CreateManagedBlockStorageDiskSnapshotParameters>
        extends CommandBase<T> {

    @Inject
    private ManagedBlockExecutor managedBlockExecutor;

    @Inject
    private ManagedBlockStorageDao managedBlockStorageDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private ImageDao imageDao;

    @Inject
    private ImagesHandler imagesHandler;

    @Inject
    private ManagedBlockStorageDiskUtil managedBlockStorageDiskUtil;

    public CreateManagedBlockStorageDiskSnapshotCommand(T parameters,
            CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    public CreateManagedBlockStorageDiskSnapshotCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected boolean validate() {
        return true;
    }

    @Override
    protected void executeCommand() {
        ManagedBlockStorage managedBlockStorage = managedBlockStorageDao.get(getParameters().getStorageDomainId());
        List<String> extraParams = new ArrayList<>();
        extraParams.add(getParameters().getVolumeId().toString());
        ManagedBlockReturnValue returnValue;
        Guid snapshotId;

        try {
            ManagedBlockCommandParameters params =
                    new ManagedBlockCommandParameters(JsonHelper.mapToJson(
                            managedBlockStorage.getAllDriverOptions(),
                            false),
                            extraParams,
                            getCorrelationId());
            returnValue =
                    managedBlockExecutor.runCommand(ManagedBlockExecutor.ManagedBlockCommand.CREATE_SNAPSHOT, params);
            snapshotId = Guid.createGuidFromString(returnValue.getOutput());
        } catch (Exception e) {
            log.error("Failed executing snapshot creation", e);
            return;
        }

        if (!returnValue.getSucceed()) {
            return;
        }

        saveSnapshot(snapshotId);

        // To be used later when rolling back
        getParameters().setImageId(snapshotId);

        setSucceeded(true);
    }

    private void saveSnapshot(Guid snapshotId) {
        ManagedBlockStorageDisk disk = (ManagedBlockStorageDisk) diskImageDao.get(getParameters().getVolumeId());
        Image image = imageDao.get(getParameters().getVolumeId());

        TransactionSupport.executeInNewTransaction(() -> {
            // Create image for snapshot
            ManagedBlockStorageDisk newDiskImageForSnapshot = createNewDiskImageForSnapshot(image, disk, snapshotId);
            managedBlockStorageDiskUtil.saveDisk(newDiskImageForSnapshot);

            return null;
        });
    }

    private ManagedBlockStorageDisk createNewDiskImageForSnapshot(Image image, ManagedBlockStorageDisk disk, Guid snapshotId) {
        // Keep the volume as the current snapshot
        image.setId(snapshotId);
        image.setSnapshotId(disk.getVmSnapshotId());
        disk.setVmSnapshotId(getParameters().getVmSnapshotId());
        disk.setLastModified(new Date());
        imageDao.update(disk.getImage());

        // Set previous snapshot as parent
        ManagedBlockStorageDisk snapshotLeaf = (ManagedBlockStorageDisk) imagesHandler.getSnapshotLeaf(disk.getId());
        image.setParentId(snapshotLeaf.getImageId());

        disk.setImage(image);
        disk.setVolumeClassification(VolumeClassification.Snapshot);
        disk.setDiskSnapshot(true);
        disk.setDescription(getParameters().getDescription());
        disk.setCreationDate(new Date());
        disk.setLastModifiedDate(new Date());
        disk.setActive(false);
        disk.setImageStatus(ImageStatus.OK);
        imageDao.save(disk.getImage());

        return disk;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return null;
    }

    @Override
    protected void endWithFailure() {
        if (!Guid.Empty.equals(getParameters().getImageId())) {
            revertSnapshot();
        }
    }

    private void revertSnapshot() {
        log.info("Failure occurred while creating snapshot {}, attempting to remove",
                getParameters().getImageId());
        ImagesContainterParametersBase params = new ImagesContainterParametersBase();
        params.setImageId(getParameters().getImageId());
        params.setImageGroupID(getParameters().getVolumeId());
        params.setEndProcedure(ActionParametersBase.EndProcedure.COMMAND_MANAGED);

        ActionReturnValue returnValue = runInternalAction(ActionType.RemoveManagedBlockStorageSnapshot,
                params,
                cloneContextAndDetachFromParent());

        if (returnValue.getSucceeded()) {
            DiskImage parentImage = diskImageDao.get(getParameters().getVolumeId());
            managedBlockStorageDiskUtil.updateOldImageAsActive(Snapshot.SnapshotType.ACTIVE, true, parentImage);
        }

    }
}
