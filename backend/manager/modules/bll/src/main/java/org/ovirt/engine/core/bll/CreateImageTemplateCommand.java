package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.PostZeroHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.CopyVolumeType;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.VolumeType;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

/**
 * This command responsible to create new Image Template from image.
 */
@InternalCommandAttribute
public class CreateImageTemplateCommand<T extends CreateImageTemplateParameters> extends BaseImagesCommand<T> {
    public CreateImageTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        super.setVmTemplateId(parameters.getVmTemplateId());
        super.setVmTemplateName(parameters.getVmTemplateName());
    }

    @Override
    protected void insertAsyncTaskPlaceHolders() {
        persistAsyncTaskPlaceHolder(VdcActionType.AddVmTemplate);
    }

    @Override
    protected void executeCommand() {
        Guid storagePoolId = getDiskImage().getStoragePoolId() != null ? getDiskImage().getStoragePoolId()
                : Guid.Empty;
        Guid imageGroupId = getDiskImage().getId() != null ? getDiskImage().getId()
                : Guid.Empty;
        Guid snapshotId = getDiskImage().getImageId();
        // Create new image group id and image id:
        Guid destinationImageGroupID = Guid.newGuid();
        setDestinationImageId(Guid.newGuid());
        DiskImage newImage = cloneDiskImage(getDestinationImageId());
        fillVolumeInformation(newImage);

        Guid taskId = getAsyncTaskId();

        VolumeFormat targetFormat = getTargetVolumeFormat(newImage.getVolumeFormat(), newImage.getVolumeType(),
                getParameters().getDestinationStorageDomainId());

        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.CopyImage,
                PostZeroHandler.fixParametersWithPostZero(
                        new CopyImageVDSCommandParameters(storagePoolId, getParameters().getStorageDomainId(),
                                getParameters().getVmId(), imageGroupId, snapshotId, destinationImageGroupID,
                                getDestinationImageId(), StringUtils.defaultString(newImage.getDescription()),
                                getParameters().getDestinationStorageDomainId(), CopyVolumeType.SharedVol,
                                targetFormat, newImage.getVolumeType(), getDiskImage().isWipeAfterDelete(), false)));

        getReturnValue().getInternalVdsmTaskIdList().add(
                createTask(taskId,
                        vdsReturnValue.getCreationInfo(),
                        VdcActionType.AddVmTemplate,
                        VdcObjectType.Storage,
                        getParameters().getStorageDomainId(),
                        getParameters().getDestinationStorageDomainId()));

        newImage.setId(destinationImageGroupID);
        newImage.setDiskAlias(getParameters().getDiskAlias() != null ?
                getParameters().getDiskAlias() : getDiskImage().getDiskAlias());
        newImage.setVmSnapshotId(getParameters().getVmSnapshotId());
        newImage.setQuotaId(getParameters().getQuotaId());
        newImage.setDiskProfileId(getParameters().getDiskProfileId());
        newImage.setParentId(Guid.Empty);
        newImage.setImageTemplateId(Guid.Empty);
        newImage.setStorageIds(new ArrayList<Guid>(Arrays.asList(getParameters().getDestinationStorageDomainId())));
        newImage.setActive(true);
        saveImage(newImage);
        getBaseDiskDao().save(newImage);

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(newImage.getImageId());
        diskDynamic.setactual_size(getDiskImage().getActualSizeInBytes());
        DbFacade.getInstance().getDiskImageDynamicDao().save(diskDynamic);

        setActionReturnValue(newImage);

        // set source image as locked:
        lockImage();
        setSucceeded(true);
    }

    /**
     * Since we are supporting copy/move operations between different storage families (file/block) we have to
     * predetermine the volume format according to the destination storage type, for block domains we cannot use sparse
     * combined with raw so we will change the raw to cow in that case, file domains will have the original format
     * retained
     *
     * TODO: Extract method and unite with getVolumeFormatForDomain() in CopyImageGroupCommand
     */
    private VolumeFormat getTargetVolumeFormat(VolumeFormat volumeFormat, VolumeType volumeType, Guid storageDomainId) {
        if (volumeFormat == VolumeFormat.RAW && volumeType == VolumeType.Sparse) {
            StorageDomainStatic destDomain = getStorageDomainStaticDAO().get(storageDomainId);
            if (destDomain.getStorageType().isBlockDomain()) {
                return VolumeFormat.COW;
            }
        }

        return volumeFormat;
    }

    /**
     * Fill the volume information from the image ancestor (if available, if not then from the father image).
     *
     * @param disk
     *            The disk to fill the volume details in.
     */
    private void fillVolumeInformation(DiskImage disk) {
        DiskImage ancestor = getDiskImageDao().getAncestor(getDiskImage().getImageId());
        if (ancestor == null) {
            log.warn("Can't find ancestor of Disk with ID '{}', using original disk for volume info.",
                    getDiskImage().getImageId());
            ancestor = getDiskImage();
        }
        disk.setvolumeFormat(ancestor.getVolumeFormat());
        disk.setVolumeType(ancestor.getVolumeType());
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.copyImage;
    }

    @Override
    protected void endWithFailure() {
        unLockImage();
        setVmTemplate(DbFacade.getInstance().getVmTemplateDao()
                .get(getVmTemplateId()));
        if (getDestinationDiskImage() != null) {
            revertTasks();
        }
        setSucceeded(true);
    }

    @Override
    protected void revertTasks() {
        Guid destImageId = getDestinationDiskImage().getImageId();
        RemoveImageParameters p =
                new RemoveImageParameters(destImageId);
        p.setEntityInfo(new EntityInfo(VdcObjectType.Disk, destImageId));
        p.setParentParameters(p);
        p.setParentCommand(VdcActionType.RemoveImage);
        VdcReturnValueBase returnValue =
                checkAndPerformRollbackUsingCommand(VdcActionType.RemoveImage, p);
        if (returnValue.getSucceeded()) {
            startPollingAsyncTasks(returnValue.getInternalVdsmTaskIdList());
        }
    }
}
