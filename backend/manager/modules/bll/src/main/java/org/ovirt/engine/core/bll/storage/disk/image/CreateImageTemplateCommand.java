package org.ovirt.engine.core.bll.storage.disk.image;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.InternalCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.PostDeleteActionHandler;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.CreateImageTemplateParameters;
import org.ovirt.engine.core.common.action.RemoveImageParameters;
import org.ovirt.engine.core.common.asynctasks.AsyncTaskType;
import org.ovirt.engine.core.common.asynctasks.EntityInfo;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskImageDynamic;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.businessentities.storage.VolumeType;
import org.ovirt.engine.core.common.vdscommands.CopyImageVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.common.vdscommands.VDSReturnValue;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.BaseDiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskImageDynamicDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VmTemplateDao;

/**
 * This command responsible to create new Image Template from image.
 */
@InternalCommandAttribute
public class CreateImageTemplateCommand<T extends CreateImageTemplateParameters> extends BaseImagesCommand<T> {

    @Inject
    private PostDeleteActionHandler postDeleteActionHandler;
    @Inject
    private BaseDiskDao baseDiskDao;
    @Inject
    private DiskImageDynamicDao diskImageDynamicDao;
    @Inject
    private StorageDomainDao storageDomainDao;
    @Inject
    private StorageDomainStaticDao storageDomainStaticDao;
    @Inject
    private DiskImageDao diskImageDao;
    @Inject
    private VmTemplateDao vmTemplateDao;

    public CreateImageTemplateCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        super.setVmTemplateId(parameters.getVmTemplateId());
        super.setVmTemplateName(parameters.getVmTemplateName());
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

        if (getParameters().getVolumeFormat() == null || getParameters().getVolumeType() == null) {
            // At least one of the volume arguments should be copied from the ancestral image.
            fillVolumeInformation(newImage);
        }
        if (getParameters().getVolumeFormat() != null) {
            newImage.setVolumeFormat(getParameters().getVolumeFormat());
        }
        if (getParameters().getVolumeType() != null) {
            newImage.setVolumeType(getParameters().getVolumeType());
        }

        Guid taskId = persistAsyncTaskPlaceHolder(getParameters().getParentCommand());

        VolumeFormat targetFormat = getTargetVolumeFormat(newImage.getVolumeFormat(), newImage.getVolumeType(),
                getParameters().getDestinationStorageDomainId());

        newImage.setDiskAlias(getParameters().getDiskAlias() != null ?
                getParameters().getDiskAlias() : getDiskImage().getDiskAlias());
        VDSReturnValue vdsReturnValue = runVdsCommand(VDSCommandType.CopyImage,
                postDeleteActionHandler.fixParameters(
                        new CopyImageVDSCommandParameters(storagePoolId, getParameters().getStorageDomainId(),
                                getParameters().getVmId(), imageGroupId, snapshotId, destinationImageGroupID,
                                getDestinationImageId(), imagesHandler.getJsonDiskDescription(newImage),
                                getParameters().getDestinationStorageDomainId(), getParameters().getCopyVolumeType(),
                                targetFormat, newImage.getVolumeType(), getDiskImage().isWipeAfterDelete(),
                                storageDomainDao.get(getParameters().getDestinationStorageDomainId())
                                        .getDiscardAfterDelete(),
                                false)));

        getReturnValue().getInternalVdsmTaskIdList().add(
                createTask(taskId,
                        vdsReturnValue.getCreationInfo(),
                        getParameters().getParentCommand(),
                        VdcObjectType.Storage,
                        getParameters().getStorageDomainId(),
                        getParameters().getDestinationStorageDomainId()));

        newImage.setId(destinationImageGroupID);
        newImage.setDiskDescription(getParameters().getDescription() != null ?
                getParameters().getDescription() : getDiskImage().getDiskDescription());
        newImage.setVmSnapshotId(getParameters().getVmSnapshotId());
        newImage.setQuotaId(getParameters().getQuotaId());
        newImage.setDiskProfileId(getParameters().getDiskProfileId());
        newImage.setParentId(Guid.Empty);
        newImage.setImageTemplateId(Guid.Empty);
        newImage.setStorageIds(new ArrayList<>(Arrays.asList(getParameters().getDestinationStorageDomainId())));
        newImage.setActive(true);
        imagesHandler.saveImage(newImage);
        baseDiskDao.save(newImage);

        DiskImageDynamic diskDynamic = new DiskImageDynamic();
        diskDynamic.setId(newImage.getImageId());
        diskDynamic.setActualSize(getDiskImage().getActualSizeInBytes());
        diskImageDynamicDao.save(diskDynamic);

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
            StorageDomainStatic destDomain = storageDomainStaticDao.get(storageDomainId);
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
        DiskImage ancestor = diskImageDao.getAncestor(getDiskImage().getImageId());
        disk.setVolumeFormat(ancestor.getVolumeFormat());
        disk.setVolumeType(ancestor.getVolumeType());
    }

    @Override
    protected AsyncTaskType getTaskType() {
        return AsyncTaskType.copyImage;
    }

    @Override
    protected void endWithFailure() {
        unLockImage();
        setVmTemplate(vmTemplateDao.get(getVmTemplateId()));
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
        p.setParentCommand(ActionType.RemoveImage);
        ActionReturnValue returnValue =
                checkAndPerformRollbackUsingCommand(ActionType.RemoveImage, p, null);
        if (returnValue.getSucceeded()) {
            startPollingAsyncTasks(returnValue.getInternalVdsmTaskIdList());
        }
    }
}
