package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.ConcurrentChildCommandsExecutionCallback;
import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.PostDeleteActionHandler;
import org.ovirt.engine.core.bll.tasks.interfaces.CommandCallback;
import org.ovirt.engine.core.bll.validator.storage.DiskValidator;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CopyImageGroupWithDataCommandParameters;
import org.ovirt.engine.core.common.action.MoveOrCopyImageGroupParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionParametersBase.EndProcedure;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskCreationInfo;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageOperation;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMap;
import org.ovirt.engine.core.common.businessentities.storage.ImageStorageDomainMapId;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.MoveImageGroupVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.ImageDao;
import org.ovirt.engine.core.dao.ImageStorageDomainMapDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;

@InternalCommandAttribute
public class CopyImageGroupCommand<T extends MoveOrCopyImageGroupParameters> extends BaseImagesCommand<T> {

    @Inject
    private PostDeleteActionHandler postDeleteActionHandler;
    @Inject
    private DiskDao diskDao;
    @Inject
    private ImageStorageDomainMapDao imageStorageDomainMapDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private ImageDao imageDao;

    public CopyImageGroupCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
    }

    private DiskImage diskImage;

    @Override
    protected DiskImage getImage() {
        switch (getActionState()) {
        case END_SUCCESS:
        case END_FAILURE:
            if (diskImage == null) {
                List<DiskImage> diskImages =
                        diskImageDao.getAllSnapshotsForImageGroup(getParameters().getImageGroupID());
                diskImage = diskImages.isEmpty() ? null : diskImages.get(0);
            }

            return diskImage;

        default:
            return super.getImage();
        }
    }

    @Override
    protected boolean validate() {
        // Not relevant for import VM/VMTemplate
        if (getParameters().isImportEntity()) {
            return true;
        }

        Guid imageGroupId = Guid.isNullOrEmpty(getParameters().getImageGroupID()) ?
                getImageGroupId() : getParameters().getImageGroupID();
        Disk disk = diskDao.get(imageGroupId);
        if (disk != null) {
            DiskValidator diskValidator = new DiskValidator(disk);
            return validate(diskValidator.validateUnsupportedDiskStorageType(
                    DiskStorageType.LUN, DiskStorageType.CINDER));
        }
        return true;
    }

    @Override
    protected void executeCommand() {
        lockImage();
        if (performStorageOperation()) {
            // Add storage domain in db only if there is new entity in DB.
            if (!shouldUpdateStorageDisk() && getParameters().getAddImageDomainMapping()) {
                imageStorageDomainMapDao.save
                        (new ImageStorageDomainMap(getParameters().getImageId(),
                                getParameters().getStorageDomainId(),
                                getParameters().getQuotaId(),
                                getParameters().getDiskProfileId()));
            }

            setSucceeded(true);
        }
    }

    private boolean isUsingSPDMFlow() {
        return isDataOperationsByHSM() && !getParameters().getUseCopyCollapse()
                && getParameters().getParentCommand() == VdcActionType.MoveOrCopyDisk
                && getParameters().getOperation() == ImageOperation.Move;
    }

    private boolean performStorageOperation() {
        Guid sourceDomainId = getParameters().getSourceDomainId() != null ? getParameters().getSourceDomainId()
                : getDiskImage().getStorageIds().get(0);
        if (isUsingSPDMFlow()) {
            CopyImageGroupWithDataCommandParameters p = new CopyImageGroupWithDataCommandParameters(
                    getStorageDomain().getStoragePoolId(),
                    sourceDomainId,
                    getParameters().getStorageDomainId(),
                    getParameters().getImageGroupID(),
                    getParameters().getImageId(),
                    getParameters().getImageGroupID(),
                    getParameters().getImageId(),
                    getVolumeFormatForDomain(),
                    getParameters().getUseCopyCollapse());
            p.setParentParameters(getParameters());
            p.setParentCommand(getActionType());
            p.setEndProcedure(EndProcedure.COMMAND_MANAGED);
            p.setJobWeight(getParameters().getJobWeight());
            runInternalAction(VdcActionType.CopyImageGroupWithData, p);
            return true;
        } else {
            VDSReturnValue vdsReturnValue;
            Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());

            if (getParameters().getUseCopyCollapse()) {
                vdsReturnValue = runVdsCommand(
                        VDSCommandType.CopyImage,
                        postDeleteActionHandler.fixParameters(
                                new CopyImageVDSCommandParameters(getStorageDomain().getStoragePoolId(),
                                        sourceDomainId,
                                        getParameters().getContainerId(),
                                        getParameters().getImageGroupID(),
                                        getParameters().getImageId(),
                                        getParameters().getDestImageGroupId(),
                                        getParameters().getDestinationImageId(),
                                        "",
                                        getParameters().getStorageDomainId(),
                                        getParameters().getCopyVolumeType(),
                                        getVolumeFormatForDomain(),
                                        getParameters().getVolumeType(),
                                        isWipeAfterDelete(),
                                        getStorageDomain().isDiscardAfterDelete(),
                                        getParameters().getForceOverride())));
            } else {
                vdsReturnValue = runVdsCommand(
                        VDSCommandType.MoveImageGroup,
                        postDeleteActionHandler.fixParameters(
                                new MoveImageGroupVDSCommandParameters(
                                        getDiskImage() != null ? getDiskImage().getStoragePoolId()
                                                : getStorageDomain().getStoragePoolId(),
                                        sourceDomainId,
                                        getDiskImage() != null ?
                                                getDiskImage().getId() : getParameters().getImageGroupID(),
                                        getParameters().getStorageDomainId(),
                                        getParameters().getContainerId(),
                                        ImageOperation.Copy,
                                        isWipeAfterDelete(),
                                        storageDomainDao.get(sourceDomainId).isDiscardAfterDelete(),
                                        getParameters().getForceOverride())));
            }

            if (vdsReturnValue.getSucceeded()) {
                AsyncTaskCreationInfo taskCreationInfo = vdsReturnValue.getCreationInfo();
                getTaskIdList().add(
                        createTask(taskId,
                                taskCreationInfo,
                                getParameters().getParentCommand(),
                                VdcObjectType.Storage,
                                sourceDomainId,
                                getParameters().getStorageDomainId()));
            }

            return vdsReturnValue.getSucceeded();
        }
    }

    @Override
    public CommandCallback getCallback() {
        if (isUsingSPDMFlow()){
            return new ConcurrentChildCommandsExecutionCallback();
        }

        return null;
    }

    private boolean isWipeAfterDelete() {
        return getDestinationDiskImage() != null ? getDestinationDiskImage().isWipeAfterDelete()
                : getParameters().getWipeAfterDelete();
    }

    /**
     * Since we are supporting copy/move operations between different storage families (file/block) we have to
     * predetermine the volume format according to the destination storage type, for block domains we cannot use sparse
     * combined with raw so we will change the raw to cow in that case, file domains will have the original format
     * retained
     */
    private VolumeFormat getVolumeFormatForDomain() {
        if (getParameters().getVolumeFormat() == VolumeFormat.COW) {
            return VolumeFormat.COW;
        }

        StorageDomainStatic destDomain = storageDomainStaticDao.get(getParameters().getStorageDomainId());
        if (destDomain.getStorageType().isBlockDomain() && getParameters().getVolumeType() == VolumeType.Sparse) {
            return VolumeFormat.COW;
        }
        else {
            return VolumeFormat.RAW;
        }
    }

    /**
     * Shareable disk which shared between more than one VM, will be returned more than once when fetching the images by image group
     * since it has multiple VM devices (one for each VM it is attached to) and not because he has snapshots,
     * so the shareable disk needs to be distinct when updating the storage domain.
     * @param snapshots - All the images which related to the image group id
     */
    private static void setSnapshotForShareableDisk(List<DiskImage> snapshots) {
        if (!snapshots.isEmpty() && snapshots.get(0).isShareable()) {
            DiskImage sharedDisk = snapshots.get(0);
            snapshots.clear();
            snapshots.add(sharedDisk);
        }
    }

    @Override
    protected AsyncTaskType getTaskType() {
        if (!isUsingSPDMFlow()) {
            return AsyncTaskType.moveImage;
        }

        return AsyncTaskType.notSupported;
    }

    @Override
    protected void endSuccessfully() {
        if (shouldUpdateStorageDisk()) {
            List<DiskImage> snapshots = diskImageDao
                    .getAllSnapshotsForImageGroup(getParameters().getDestImageGroupId());
            setSnapshotForShareableDisk(snapshots);
            for (DiskImage snapshot : snapshots) {
                imageStorageDomainMapDao.remove
                        (new ImageStorageDomainMapId(snapshot.getImageId(),
                                snapshot.getStorageIds().get(0)));
                imageStorageDomainMapDao.save
                        (new ImageStorageDomainMap(snapshot.getImageId(),
                                getParameters().getStorageDomainId(),
                                getParameters().getQuotaId(),
                                getParameters().getDiskProfileId()));
                setQcowCompatForSnapshot(snapshot, null);
            }
        }
        super.endSuccessfully();
    }

    protected void setQcowCompatForSnapshot(DiskImage snapshot, DiskImage volInfo) {
        DiskImage info = volInfo;
        if (snapshot.getVolumeFormat().equals(VolumeFormat.COW)) {
            try {
                if (info == null) {
                    info = getVolumeInfo(snapshot.getStoragePoolId(),
                            getParameters().getStorageDomainId(),
                            snapshot.getId(),
                            snapshot.getImageId());
                }
                if (info != null) {
                    setQcowCompatByQemuImageInfo(snapshot.getStoragePoolId(),
                            snapshot.getId(),
                            snapshot.getImageId(),
                            getParameters().getStorageDomainId(),
                            snapshot);
                }
                imageDao.update(snapshot.getImage());
            } catch (EngineException e) {
                // Logging only
                log.error("Unable to update the image info for image '{}' (image group: '{}') on domain '{}'",
                        snapshot.getImageId(),
                        snapshot.getId(),
                        getParameters().getStorageDomainId());
            }
        }
    }

    private boolean shouldUpdateStorageDisk() {
        return getParameters().getOperation() == ImageOperation.Move ||
                getParameters().getParentCommand() == VdcActionType.ImportVm;
    }

    @Override
    protected void endWithFailure() {
        if (!getParameters().isImportEntity()) {
            unLockImage();
        }

        if (getParameters().getAddImageDomainMapping()) {
            // remove image-storage mapping
            imageStorageDomainMapDao.remove
                    (new ImageStorageDomainMapId(getParameters().getImageId(),
                            getParameters().getStorageDomainId()));
        }
        revertTasks();
        setSucceeded(true);
    }

    @Override
    protected void revertTasks() {
        if (getParameters().getRevertDbOperationScope() != null) {
            Guid destImageId = getParameters().getDestinationImageId();
            RemoveImageParameters removeImageParams =
                    new RemoveImageParameters(destImageId);
            if (getParameters().getParentCommand() == VdcActionType.AddVmFromSnapshot) {
                removeImageParams.setParentParameters(getParameters());
                removeImageParams.setParentCommand(VdcActionType.CopyImageGroup);
            } else {
                removeImageParams.setParentParameters(removeImageParams);
                removeImageParams.setParentCommand(VdcActionType.RemoveImage);
                removeImageParams.setStorageDomainId(getParameters().getStorageDomainId());
                removeImageParams.setDbOperationScope(getParameters().getRevertDbOperationScope());
                removeImageParams.setShouldLockImage(getParameters().isShouldLockImageOnRevert());
            }
            removeImageParams.setEntityInfo(new EntityInfo(VdcObjectType.Disk, getDestinationImageId()));
            // Setting the image as the monitored entity, so there will not be dependency
            VdcReturnValueBase returnValue =
                    checkAndPerformRollbackUsingCommand(VdcActionType.RemoveImage, removeImageParams, null);
            if (returnValue.getSucceeded()) {
                // Starting to monitor the the tasks - RemoveImage is an internal command
                // which adds the taskId on the internal task ID list
                startPollingAsyncTasks(returnValue.getInternalVdsmTaskIdList());
            }
        }
    }

    @Override
    protected boolean canPerformRollbackUsingCommand(VdcActionType commandType, VdcActionParametersBase params) {
        return diskImageDao.get(getParameters().getDestinationImageId()) != null;
    }

}
