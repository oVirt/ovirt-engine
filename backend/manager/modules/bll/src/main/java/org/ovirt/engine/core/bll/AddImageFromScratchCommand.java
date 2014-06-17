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

        mNewCreatedDiskImage = new DiskImage();
        mNewCreatedDiskImage.setImageId(getDestinationImageId());
        mNewCreatedDiskImage.setBoot(getParameters().getDiskInfo().isBoot());
        mNewCreatedDiskImage.setDiskInterface(getParameters().getDiskInfo().getDiskInterface());
        mNewCreatedDiskImage.setPropagateErrors(getParameters().getDiskInfo().getPropagateErrors());
        mNewCreatedDiskImage.setWipeAfterDelete(getParameters().getDiskInfo().isWipeAfterDelete());
        mNewCreatedDiskImage.setDiskAlias(getParameters().getDiskInfo().getDiskAlias());
        mNewCreatedDiskImage.setDiskDescription(getParameters().getDiskInfo().getDiskDescription());
        mNewCreatedDiskImage.setShareable(getParameters().getDiskInfo().isShareable());
        mNewCreatedDiskImage.setId(getImageGroupId());
        mNewCreatedDiskImage.setStoragePoolId(getParameters().getStoragePoolId());
        mNewCreatedDiskImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(getParameters().getStorageDomainId())));
        mNewCreatedDiskImage.setSize(getParameters().getDiskInfo().getSize());
        mNewCreatedDiskImage.setVolumeType(getParameters().getDiskInfo().getVolumeType());
        mNewCreatedDiskImage.setvolumeFormat(getParameters().getDiskInfo().getVolumeFormat());
        mNewCreatedDiskImage.setDescription("");
        mNewCreatedDiskImage.setCreationDate(new Date());
        mNewCreatedDiskImage.setLastModified(new Date());
        mNewCreatedDiskImage.setActive(true);
        mNewCreatedDiskImage.setImageStatus(ImageStatus.LOCKED);
        mNewCreatedDiskImage.setVmSnapshotId(getParameters().getVmSnapshotId());
        mNewCreatedDiskImage.setQuotaId(getParameters().getQuotaId());

        TransactionSupport.executeInNewTransaction(new TransactionMethod<Void>() {
            @Override
            public Void runInTransaction() {
                if (!getParameters().isShouldRemainIllegalOnFailedExecution()) {
                    addDiskImageToDb(mNewCreatedDiskImage, getCompensationContext());
                } else {
                    addDiskImageToDb(mNewCreatedDiskImage, null);
                    getCompensationContext().snapshotEntityStatus(mNewCreatedDiskImage.getImage(), ImageStatus.ILLEGAL);
                }
                return null;
            }
        });
        freeLock();
        if (getParameters().isShouldRemainIllegalOnFailedExecution()) {
            getReturnValue().setActionReturnValue(mNewCreatedDiskImage);
        }
        processImageInIrs();
        getReturnValue().setActionReturnValue(mNewCreatedDiskImage);
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
                setImageStatus(ImageStatus.ILLEGAL);
            } else {
                DbFacade.getInstance().getDiskImageDynamicDao().remove(getDestinationDiskImage().getImageId());
                super.endWithFailure();
            }
        }

    }
}
