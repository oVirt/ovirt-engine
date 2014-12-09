package org.ovirt.engine.core.bll;

import org.ovirt.engine.core.bll.context.CommandContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddImageFromScratchParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.ImageStatus;
import org.ovirt.engine.core.common.vdscommands.CreateImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.transaction.TransactionMethod;
import org.ovirt.engine.core.utils.transaction.TransactionSupport;

@InternalCommandAttribute
@NonTransactiveCommandAttribute(forceCompensation=true)
public class AddImageFromScratchCommand<T extends AddImageFromScratchParameters> extends CreateSnapshotCommand<T> {

    public AddImageFromScratchCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(getParameters().getMasterVmId());
        getParameters().setCommandType(getActionType());
    }

    protected AddImageFromScratchCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected void insertAsyncTaskPlaceHolders() {
        persistAsyncTaskPlaceHolder(getParameters().getParentCommand());
    }

    @Override
    protected void executeCommand() {
        setImageGroupId(getParameters().getDiskInfo().getId());
        if (Guid.isNullOrEmpty(getDestinationImageId())) {
            setDestinationImageId(Guid.newGuid());
        }

        newDiskImage = new DiskImage();
        newDiskImage.setImageId(getDestinationImageId());
        newDiskImage.setBoot(getParameters().getDiskInfo().isBoot());
        newDiskImage.setDiskInterface(getParameters().getDiskInfo().getDiskInterface());
        newDiskImage.setPropagateErrors(getParameters().getDiskInfo().getPropagateErrors());
        newDiskImage.setWipeAfterDelete(getParameters().getDiskInfo().isWipeAfterDelete());
        newDiskImage.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
        newDiskImage.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
        newDiskImage.setShareable(getParameters().getDiskInfo().isShareable());
        newDiskImage.setId(getImageGroupId());
        newDiskImage.setStoragePoolId(getParameters().getStoragePoolId());
        newDiskImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(getParameters().getStorageDomainId())));
        newDiskImage.setSize(getParameters().getDiskInfo().getSize());
        newDiskImage.setVolumeType(getParameters().getDiskInfo().getVolumeType());
        newDiskImage.setvolumeFormat(getParameters().getDiskInfo().getVolumeFormat());
        newDiskImage.setDescription("");
        newDiskImage.setCreationDate(new Date());
        newDiskImage.setLastModified(new Date());
        newDiskImage.setActive(true);
        newDiskImage.setImageStatus(ImageStatus.LOCKED);
        newDiskImage.setVmSnapshotId(getParameters().getVmSnapshotId());
        newDiskImage.setQuotaId(getParameters().getQuotaId());
        newDiskImage.setDiskProfileId(getParameters().getDiskProfileId());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                if (!getParameters().isShouldRemainIllegalOnFailedExecution()) {
                    addDiskImageToDb(newDiskImage, getCompensationContext());
                } else {
                    addDiskImageToDb(newDiskImage, null);
                    getCompensationContext().snapshotEntityStatus(newDiskImage.getImage(), ImageStatus.ILLEGAL);
                }
                return null;
            }
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
        Guid taskId = getAsyncTaskId();
        String diskDescription = getParameters().getDiskInfo().getDiskDescription();
        VDSReturnValue vdsReturnValue = runVdsCommand(
                        VDSCommandType.CreateImage,
                        new CreateImageVDSCommandParameters(getParameters().getStoragePoolId(), getParameters()
                                .getStorageDomainId(), getImageGroupId(), getParameters().getDiskInfo().getSize(),
                                getParameters().getDiskInfo().getVolumeType(), getParameters().getDiskInfo()
                                        .getVolumeFormat(), getDestinationImageId(), diskDescription != null ? diskDescription : ""));
        if (vdsReturnValue.getSucceeded()) {
            getParameters().setVdsmTaskIds(new ArrayList<Guid>());
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

    }
}
