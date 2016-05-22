package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.snapshots.CreateSnapshotCommand;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class AddImageFromScratchCommand<T extends AddImageFromScratchParameters> extends CreateSnapshotCommand<T> {

    public AddImageFromScratchCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(getParameters().getMasterVmId());
        getParameters().setCommandType(getActionType());
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
        newDiskImage.setImageStatus(ImageStatus.LOCKED);
        newDiskImage.setVmSnapshotId(getParameters().getVmSnapshotId());
        newDiskImage.setQuotaId(getParameters().getQuotaId());
        newDiskImage.setDiskProfileId(getParameters().getDiskProfileId());

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
        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.CreateImage, getCreateImageVDSCommandParameters());
        if (vdsReturnValue.getSucceeded()) {
            getParameters().setVdsmTaskIds(new ArrayList<>());
            getParameters().getVdsmTaskIds().add(
                    createTask(taskId,
                            vdsReturnValue.getCreationInfo(),
                            getParameters().getParentCommand(),
                            VdcObjectType.Storage,
                            getParameters().getStorageDomainId()));
            getReturnValue().getInternalVdsmTaskIdList().add(getParameters().getVdsmTaskIds().get(0));

            return true;
        }

        return false;
    }

    private CreateImageVDSCommandParameters getCreateImageVDSCommandParameters() {
        DiskImage diskImage = getParameters().getDiskInfo();
        CreateImageVDSCommandParameters parameters =
                new CreateImageVDSCommandParameters(getParameters().getStoragePoolId(),
                        getParameters()
                                .getStorageDomainId(), getImageGroupId(), getParameters().getDiskInfo().getSize(),
                        getParameters().getDiskInfo().getVolumeType(), getParameters().getDiskInfo()
                                .getVolumeFormat(), getDestinationImageId(),
                        getJsonDiskDescription(getParameters().getDiskInfo()));

        if (getStorageDomain().getStorageType().isBlockDomain() &&
                diskImage.getImage().getVolumeType().equals(VolumeType.Sparse) &&
                diskImage.getActualSizeInBytes() != 0) {
            parameters.setImageInitialSizeInBytes(diskImage.getActualSizeInBytes());

        }
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
                DbFacade.getInstance().getDiskImageDynamicDao().remove(getDestinationDiskImage().getImageId());
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
