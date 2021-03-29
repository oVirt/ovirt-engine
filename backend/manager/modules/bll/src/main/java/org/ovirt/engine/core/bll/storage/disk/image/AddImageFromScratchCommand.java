package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.CreateSnapshotCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.vdscommands.CreateVolumeVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class AddImageFromScratchCommand<T extends AddImageFromScratchParameters> extends CreateSnapshotCommand<T> {

    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;

    @Inject
    private ImagesHandler imagesHandler;

    public AddImageFromScratchCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(getParameters().getMasterVmId());
        getParameters().setCommandType(getActionType());
        setStoragePoolId(getParameters().getStoragePoolId());
    }

    public AddImageFromScratchCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void executeCommand() {
        setImageGroupId(getParameters().getDiskInfo().getId());
        if (Guid.isNullOrEmpty(getDestinationImageId())) {
            setDestinationImageId(Guid.newGuid());
        }

        newDiskImage = new DiskImage();
        newDiskImage.setImageId(getDestinationImageId());
        newDiskImage.setPropagateErrors(getParameters().getDiskInfo().getPropagateErrors());
        newDiskImage.setWipeAfterDelete(getParameters().getDiskInfo().isWipeAfterDelete());
        newDiskImage.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
        newDiskImage.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
        newDiskImage.setShareable(getParameters().getDiskInfo().isShareable());
        newDiskImage.setId(getImageGroupId());
        newDiskImage.setStoragePoolId(getParameters().getStoragePoolId());
        newDiskImage.setStorageIds(new ArrayList<>(Arrays.asList(getParameters().getStorageDomainId())));
        newDiskImage.setSize(getParameters().getDiskInfo().getSize());
        newDiskImage.setVolumeType(getParameters().getDiskInfo().getVolumeType());
        newDiskImage.setVolumeFormat(getParameters().getDiskInfo().getVolumeFormat());
        newDiskImage.setDescription("");
        newDiskImage.setCreationDate(new Date());
        newDiskImage.setLastModified(new Date());
        newDiskImage.setActive(true);
        ImageStatus status = getParameters().getDiskInfo().isManaged() ? ImageStatus.LOCKED : ImageStatus.OK;
        newDiskImage.setImageStatus(status);
        newDiskImage.setVmSnapshotId(getParameters().getVmSnapshotId());
        newDiskImage.setQuotaId(getParameters().getQuotaId());
        newDiskImage.setDiskProfileId(getParameters().getDiskProfileId());
        newDiskImage.setContentType(getParameters().getDiskInfo().getContentType());
        newDiskImage.setBackup(getParameters().getDiskInfo().getBackup());

        TransactionSupport.executeInNewTransaction(() -> {
            if (!getParameters().isShouldRemainIllegalOnFailedExecution()) {
                addDiskImageToDb(newDiskImage, getCompensationContext(), Boolean.TRUE);
            } else {
                addDiskImageToDb(newDiskImage, null, Boolean.TRUE);
                getCompensationContext().snapshotEntityStatus(newDiskImage.getImage(), ImageStatus.ILLEGAL);
            }
            return null;
        });
        freeLock();
        if (getParameters().isShouldRemainIllegalOnFailedExecution()) {
            getReturnValue().setActionReturnValue(newDiskImage);
        }
        processImageInIrs();
        getReturnValue().setActionReturnValue(newDiskImage);
        setSucceeded(true);
    }

    protected boolean processImageInIrs() {
        if (getParameters().getDiskInfo().getDiskStorageType() == DiskStorageType.KUBERNETES) {
            return true;
        }
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.CreateVolume,
                getCreateVolumeVDSCommandParameters());
        if (vdsReturnValue.getSucceeded()) {
            getParameters().setVdsmTaskIds(new ArrayList<>());
            getParameters().getVdsmTaskIds().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
            getTaskIdList().add(getParameters().getVdsmTaskIds().get(0));

            return true;
        }

        return false;
    }

    private Long getInitialSize() {
        DiskImage diskImage = getParameters().getDiskInfo();
        Long initialSize = null;
        if (ImagesHandler.isImageInitialSizeSupported(getStorageDomain().getStorageType()) &&
                diskImage.getImage().getVolumeType().equals(VolumeType.Sparse) &&
                diskImage.getActualSizeInBytes() != 0) {
            initialSize = diskImage.getActualSizeInBytes();
        }

        return initialSize;
    }

    private CreateVolumeVDSCommandParameters getCreateVolumeVDSCommandParameters() {
        CreateVolumeVDSCommandParameters parameters =
                new CreateVolumeVDSCommandParameters(
                        getParameters().getStoragePoolId(),
                        getParameters().getStorageDomainId(),
                        getImageGroupId(),
                        Guid.Empty,
                        getParameters().getDiskInfo().getSize(),
                        getParameters().getDiskInfo().getVolumeType(),
                        getParameters().getDiskInfo().getVolumeFormat(),
                        Guid.Empty,
                        getDestinationImageId(),
                        imagesHandler.getJsonDiskDescription(getParameters().getDiskInfo()),
                        getStoragePool().getCompatibilityVersion(),
                        getParameters().getDiskInfo().getContentType()
                );

        // The initial size of a backup scratch disk shouldn't be overridden.
        parameters.setImageInitialSizeInBytes(
                getParameters().getDiskInfo().getContentType().equals(DiskContentType.BACKUP_SCRATCH)
                        ? getParameters().getDiskInfo().getInitialSizeInBytes()
                        : Optional.ofNullable(getInitialSize()).orElse(0L));
        return parameters;
    }

    @Override
    protected DiskImage getImage() {
        return null;
    }

    @Override
    protected void endWithFailure() {
        if (getDestinationDiskImage() != null) {
            if (getParameters().isShouldRemainIllegalOnFailedExecution()) {
                setImageStatus(ImageStatus.ILLEGAL, getDestinationDiskImage());
            } else {
                diskImageDynamicDao.remove(getDestinationDiskImage().getImageId());
                super.endWithFailure();
            }
        }

        setSucceeded(true);
    }

    @Override
    protected void endSuccessfully() {
        super.endSuccessfully();
        setActionReturnValue(getDestinationDiskImage());
    }
}
